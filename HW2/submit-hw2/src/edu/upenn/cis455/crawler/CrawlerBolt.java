package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import edu.upenn.cis455.webserver.utils.PageInfo;

public class CrawlerBolt implements IRichBolt {

	//static HashMap<String, RobotsTxtInfo> m_robotsTxt = new HashMap<String, RobotsTxtInfo>();
	private OutputCollector collector;
	private int maxFileSize;
	
	private boolean isTypeWanted(String type) {
		return type.contains("text/html") || type.contains("text/xml") || type.contains("application/xml")
				|| type.contains("+xml");
	}
	
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
		String strURL = (String)input.getValues().get(0);
		SimpleHttpClient client = new SimpleHttpClient();
		try {
			URL url = new URL(strURL);
			
			String protocol = url.getProtocol();
			String host = url.getHost();
			String uri = url.getPath();
			int port = url.getPort();
			if (port != -1)
				host += ":" + port;

			if (!uri.isEmpty()) {
				// we should have already got the robots.txt if the host has
				RobotsTxtInfo info = XPathCrawler.m_robotsTxt.get(host);
				if (info != null) {
					if (info.isDisallowedURI("cis455crawler", uri) && !info.isAllowedURI("cis455crawler", uri)) {
						System.out.println(strURL + ": Disallowed Link");
						return;
					}
				}
			}
			
			if (!XPathCrawler.m_robotsTxt.containsKey("host")) {
				SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", uri, "HTTP/1.1", strURL);
				getRequest.addHeader("host", host);
				getRequest.addHeader("user-agent", "cis455crawler");
				SimpleHttpResponse getResponse = client.execute(getRequest);
				if (getResponse == null)
					return;
			}
			
			// head request
			SimpleHttpRequest headRequest = new SimpleHttpRequest("HEAD", uri, "HTTP/1.1", strURL);
			headRequest.addHeader("host", host);
			headRequest.addHeader("user-agent", "cis455crawler");
			// crawl delay
			//XPathCrawler.hostVisitDelay(host);
			// System.out.println(m_URL + ": HEAD");
			SimpleHttpResponse headResponse = client.execute(headRequest);
			if (headResponse == null) {
				System.out.println(strURL + ": Invalid HTTP Response");
				return;
			}
			String type = headResponse.getContentType();
			
			if (isTypeWanted(type)) {
				int length = headResponse.getContentLength();
				if (headResponse.getStatusCode() != 200) {
					System.out.println(strURL + ": " + headResponse.getStatus());
				} else if (length <= 0) {
					System.out.println(strURL + ": Invalid Size (<= 0)");
				} else if (length < maxFileSize) {
					collector.emit(new Values<Object>(strURL, headResponse));
				} else {
					System.out.println(strURL + ": Size Too Big");
				}
			} else {
				System.out.println(strURL + ": Content Type Not Match");
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		this.collector = collector;
		this.maxFileSize = Integer.parseInt(stormConf.get("size"));
	}

	@Override
	public void setRouter(IStreamRouter router) {
		this.collector.setRouter(router);
	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

}
