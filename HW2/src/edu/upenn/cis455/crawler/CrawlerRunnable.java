package edu.upenn.cis455.crawler;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.tidy.AttVal;
import org.w3c.tidy.Node;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.webserver.utils.MyDbEnv;

public class CrawlerRunnable implements Runnable {
	private String m_URL;
	private XPathCrawler m_crawler;

	public CrawlerRunnable(String url, XPathCrawler crawler) {
		m_URL = url;
		m_crawler = crawler;
	}

	private boolean isTypeWanted(String type) {
		return type.contains("text/html") || type.contains("text/xml") || type.contains("application/xml")
				|| type.contains("+xml");
	}

	@Override
	public void run() {
		SimpleHttpClient client = new SimpleHttpClient();

		URL url = null;
		try {
			url = new URL(m_URL);
			// get the host header
			String protocol = url.getProtocol();
			String host = url.getHost();
			String uri = url.getPath();
			int port = url.getPort();
			if (port != -1)
				host += ":" + port;

			// head request
			SimpleHttpRequest headRequest = new SimpleHttpRequest("HEAD", uri, "HTTP/1.1");
			headRequest.addHeader("host", host);
			SimpleHttpResponse headResponse = client.execute(headRequest);
			String type = headResponse.getContentType();
			// type test && size test
			if (isTypeWanted(type)) {
				int length = headResponse.getContentLength();
				if (headResponse.getStatusCode() != 200) {
					System.out.println(m_URL + ": " + headResponse.getStatus());
				} else if (length <= 0) {
					System.out.println(m_URL + ": Invalid Size (<= 0)");
				} else if (length < m_crawler.m_size) {
					// get request
					SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", uri, "HTTP/1.1");
					getRequest.addHeader("host", host);
					
					//System.out.println(m_URL + ": Current");
					SimpleHttpResponse getResponse = client.execute(getRequest);

					String content = getResponse.getContent();
					//System.out.println(content);
//					FileWriter writer=new FileWriter("output.txt");
//					writer.write(content);
//					writer.close();
					
					// MD5 content-seen test
					boolean isInsertSucceed = m_crawler.m_dbEnv.insertContent(content);
					if (isInsertSucceed) {
						m_crawler.m_dbEnv.insertFile(m_URL, content);
						System.out.println(m_URL + ": Downloading");
					} else {
						System.out.println(m_URL + ": Not Modified");
					}
					
//					if (!content.equals(m_crawler.m_dbEnv.getFile(m_URL))) {
//						//System.out.println(m_URL + ": Inserting");
//						m_crawler.m_dbEnv.insertFile(m_URL, content);
//						System.out.println(m_URL + ": Downloading");
//						//System.out.println(content);
//						//System.out.println("---------------------------------");
//						//System.out.println(m_crawler.m_dbEnv.getFile(m_URL));
//					} else {
//						System.out.println(m_URL + ": Not Modified");
//					}

					// parse HTML
					if (type.contains("text/html")) {
						Document doc = Jsoup.parse(content);
						Elements link = doc.select("[href]");
						for (Element l : link) {
							String newURL = m_URL + "/" + l.attr("href");
							CrawlerRunnable task = new CrawlerRunnable(newURL, m_crawler);
							m_crawler.m_pool.execute(task);
						}
					}
				} else {
					System.out.println(m_URL + ": Size Too Big");
				}
			} else {
				System.out.println(m_URL + ": Content Type Not Match");
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException");
		}

	}

}
