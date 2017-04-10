package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.webserver.utils.ChannelInfo;
import edu.upenn.cis455.webserver.utils.MyDbEnv;
import edu.upenn.cis455.webserver.utils.PageInfo;

public class ParserBolt implements IRichBolt {

	private OutputCollector collector;

	@Override
	public String getExecutorId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(Tuple input) {
		// TODO Auto-generated method stub
		String strURL = (String)input.getValues().get(0);
		SimpleHttpResponse headResponse = (SimpleHttpResponse)input.getValues().get(1);
		String type = headResponse.getContentType();
		SimpleHttpClient client = new SimpleHttpClient();
		
		try {
			URL url = new URL(strURL);
			String protocol = url.getProtocol();
			String host = url.getHost();
			String uri = url.getPath();
			int port = url.getPort();
			if (port != -1)
				host += ":" + port;
			
			PageInfo pageInfo = XPathCrawler.m_dbEnv.getPageInfo(strURL);
			long lastModified = headResponse.getLastModified();
			String content = null;

			// if not modified, retrieve the content from database
			if (pageInfo != null && lastModified != -1 && lastModified <= pageInfo.getLastModified()) {
				System.out.println(strURL + ": Not Modified");
				content = XPathCrawler.m_dbEnv.getPageContent(strURL);
			} else { // else, send GET request to retrieve the data
				SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", uri, "HTTP/1.1", strURL);
				getRequest.addHeader("host", host);
				getRequest.addHeader("user-agent", "cis455crawler");
				// crawl delay
				XPathCrawler.hostVisitDelay(host);
				SimpleHttpResponse getResponse = client.execute(getRequest);
				if (getResponse == null)
					return;

				content = getResponse.getContent();
				
				// MD5 content-seen test
				boolean isInsertSucceed = XPathCrawler.m_dbEnv.insertContent(content);
				if (isInsertSucceed) {
					// lastModified should be equal to
					// getRequest.getLastModified()
					PageInfo info = new PageInfo(lastModified);
					Date date = new Date();
					info.m_crawledDate = date.getTime();
					info.m_URL = strURL;
					XPathCrawler.m_dbEnv.insertPage(strURL, content, info);
					System.out.println(strURL + ": Downloading");
				} else {
					System.out.println(strURL + ": Content-Seen Test Failed. Same content already exists in DB.");
				}
			}
			
			// add documents to channel
			Document doc = Jsoup.parse(content, "", Parser.xmlParser());
			boolean[] b = XPathCrawler.m_xpathEngine.evaluate(doc);
			for (int i = 0; i < b.length; i++) {
				if (b[i]) {
					System.out.println("MATCHED: " + strURL);
					ChannelInfo channel = XPathCrawler.m_channelInfos.get(i);
					XPathCrawler.m_dbEnv.addUrlToChannel(channel.name, strURL);
					break;
				}
			}
			
			if (type.contains("text/html")) {
				//Document doc = Jsoup.parse(content);
				Elements link = doc.select("[href]");
				for (Element l : link) {
					String newURLstr = strURL + l.attr("href");

					URL newURL = new URL(newURLstr);
					String newHost = newURL.getHost();

					// if it's new host, GET robots.txt
					if (!newHost.equals(host)) {
						int newPort = newURL.getPort();
						if (newPort != -1)
							newHost += ":" + newPort;
						SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", "/robots.txt", "HTTP/1.1",
								"http://" + newHost + "robots.txt");
						getRequest.addHeader("host", newHost);
						getRequest.addHeader("user-agent", "cis455crawler");
						SimpleHttpResponse getResponse = client.execute(getRequest);
						String robotContent = getResponse.getContent();
						RobotsTxtInfo info = new RobotsTxtInfo(robotContent);
						XPathCrawler.m_robotsTxt.put(host, info);
					}
					collector.emit(new Values<Object>(newURLstr));
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.collector = collector;
	}

	@Override
	public void setRouter(IStreamRouter router) {
		// TODO Auto-generated method stub
		collector.setRouter(router);
	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

}
