package edu.upenn.cis455.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.tidy.AttVal;
import org.w3c.tidy.Node;
import org.w3c.tidy.Tidy;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.webserver.utils.MyDbEnv;
import edu.upenn.cis455.webserver.utils.PageInfo;

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

	private void hostVisitDelay(String host) {
		// is it necessary
		// synchronized (m_crawler.m_hostLastVisit) {
		Long lastVisit = m_crawler.m_hostLastVisit.get(host);

		if (lastVisit == null) {
			Long i = new Date().getTime();
			m_crawler.m_hostLastVisit.put(host, i);
			System.out.println(host);
		} else {
			RobotsTxtInfo info = m_crawler.m_robotsTxt.get(host);
			if (info == null)
				return;
			Integer delay = info.getCrawlDelay("cis455crawler");
			if (delay == null)
				delay = info.getCrawlDelay("*");
			if (delay == null)
				return;

			Long crt = new Date().getTime();
			Long duration = crt - lastVisit;

			//System.out.println(duration);
			if (delay * 1000 < duration) {
				m_crawler.m_hostLastVisit.put(host, crt);
				return;
			}
			try {
				TimeUnit.SECONDS.sleep(delay - duration / 1000);
				// can not use crt!!
				m_crawler.m_hostLastVisit.put(host, new Date().getTime());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// }
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

			if (!uri.isEmpty()) {
				// we should have already got the robots.txt if the host has
				RobotsTxtInfo info = m_crawler.m_robotsTxt.get(host);
				if (info != null) {
					if (info.isDisallowedURI("cis455crawler", uri) && !info.isAllowedURI("cis455crawler", uri)) {
						System.out.println(m_URL + ": Disallowed Link");
						return;
					}
				}
			}

			if (!m_crawler.m_robotsTxt.containsKey("host")) {
				SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", uri, "HTTP/1.1", m_URL);
				getRequest.addHeader("host", host);
				getRequest.addHeader("user-agent", "cis455crawler");
				SimpleHttpResponse getResponse = client.execute(getRequest);
				if (getResponse == null)
					return;
			}

			// head request
			SimpleHttpRequest headRequest = new SimpleHttpRequest("HEAD", uri, "HTTP/1.1", m_URL);
			headRequest.addHeader("host", host);
			headRequest.addHeader("user-agent", "cis455crawler");
			// crawl delay
			//hostVisitDelay(host);
			// System.out.println(m_URL + ": HEAD");
			SimpleHttpResponse headResponse = client.execute(headRequest);
			if (headResponse == null) {
				System.out.println(m_URL + ": Invalid HTTP Response");
				return;
			}
			String type = headResponse.getContentType();

			// type test && size test
			if (isTypeWanted(type)) {
				int length = headResponse.getContentLength();
				if (headResponse.getStatusCode() != 200) {
					System.out.println(m_URL + ": " + headResponse.getStatus());
				} else if (length <= 0) {
					System.out.println(m_URL + ": Invalid Size (<= 0)");
				} else if (length < m_crawler.m_size) {
					PageInfo pageInfo = m_crawler.m_dbEnv.getPageInfo(m_URL);
					long lastModified = headResponse.getLastModified();
					String content = null;

					// if not modified, retrieve the content from database
					if (pageInfo != null && lastModified != -1 && lastModified <= pageInfo.getLastModified()) {
						System.out.println(m_URL + ": Not Modified");
						content = m_crawler.m_dbEnv.getPageContent(m_URL);
					} else { // else, send GET request to retrieve the data
						// System.out.println(lastModified);
						// System.out.println(pageInfo.getLastModified());
						// get request

						SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", uri, "HTTP/1.1", m_URL);
						getRequest.addHeader("host", host);
						getRequest.addHeader("user-agent", "cis455crawler");
						// crawl delay
						//hostVisitDelay(host);
						SimpleHttpResponse getResponse = client.execute(getRequest);
						if (getResponse == null)
							return;

						content = getResponse.getContent();
						// System.out.println(content);
						// FileWriter writer=new FileWriter("output.txt");
						// writer.write(content);
						// writer.close();

						// MD5 content-seen test
						boolean isInsertSucceed = m_crawler.m_dbEnv.insertContent(content);
						if (isInsertSucceed) {
							// lastModified should be equal to
							// getRequest.getLastModified()
							PageInfo info = new PageInfo(lastModified);
							m_crawler.m_dbEnv.insertPage(m_URL, content, info);
							System.out.println(m_URL + ": Downloading");
						} else {
							System.out
									.println(m_URL + ": Content-Seen Test Failed. Same content already exists in DB.");
						}
					}

					// original wrong content test, directly compare the URL
					// content to the retrieved content
					// if (!content.equals(m_crawler.m_dbEnv.getFile(m_URL))) {
					// //System.out.println(m_URL + ": Inserting");
					// m_crawler.m_dbEnv.insertFile(m_URL, content);
					// System.out.println(m_URL + ": Downloading");
					// //System.out.println(content);
					// //System.out.println("---------------------------------");
					// //System.out.println(m_crawler.m_dbEnv.getFile(m_URL));
					// } else {
					// System.out.println(m_URL + ": Not Modified");
					// }

					// test which has robots.txt
					// CrawlerRunnable r = new CrawlerRunnable(m_URL + "/" +
					// "robots.txt", m_crawler);
					// m_crawler.m_pool.execute(r);

					// parse HTML
					if (type.contains("text/html")) {
						Document doc = Jsoup.parse(content);
						Elements link = doc.select("[href]");
						for (Element l : link) {
							String newURLstr = m_URL + l.attr("href");

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
								m_crawler.m_robotsTxt.put(host, info);
							}

							CrawlerRunnable task = new CrawlerRunnable(newURLstr, m_crawler);
							m_crawler.m_pool.execute(task);
						}
					}
				} else {
					System.out.println(m_URL + ": Size Too Big: ");
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
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
