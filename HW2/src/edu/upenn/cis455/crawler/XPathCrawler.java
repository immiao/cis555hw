package edu.upenn.cis455.crawler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.sleepycat.je.DatabaseException;
import com.sun.org.apache.xerces.internal.util.URI;

import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.webserver.utils.MyDbEnv;

public class XPathCrawler {
	public static XPathCrawlerFactory f = new XPathCrawlerFactory();
	public LinkedBlockingQueue<Runnable> m_queue = new LinkedBlockingQueue<Runnable>(1000); // capability
	public CrawlerThreadPool m_pool = new CrawlerThreadPool(100, m_queue);
	public MyDbEnv m_dbEnv = new MyDbEnv();
	public int m_size = 0;
	public HashMap<String, RobotsTxtInfo> m_robotsTxt = new HashMap<String, RobotsTxtInfo>();
	public HashMap<String, Long> m_hostLastVisit = new HashMap<String, Long>();
	
	public static void main(String args[]) {
		if (args.length < 3) {
			System.out.println("Crawler's Arguments don't match");
			return;
		} else {
			XPathCrawler c = f.getCrawler();
			if (args[0].charAt(args[0].length() - 1) != '/')
				args[0] += '/';
			c.run(args[0], args[1], Integer.parseInt(args[2]) * 1024);
		}
	}
	
	public void run(String startURL, String dbEnvDir, int size) {
		m_size = size;
		try {
			m_dbEnv.setup(new File(dbEnvDir), false);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		HashMap<String, Integer> m = new HashMap<String, Integer>();
//		Integer test = m.get("sdsa");
//		if (test == null)
//			System.out.println("NULL");
//		URL url = null;
//		URI abc = null;
//		abc.
//		try {
//			url = new URL("http://abc.com/abc/index.html");
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(url.getHost());
//		m_dbEnv.insertFile("abc", "test");
//		m_dbEnv.insertFile("abc", "test1");
//		System.out.println(m_dbEnv.getFile("abc"));
		try {
			//URL test = new URL("http://abc.com//misc//index.html");
			//System.out.println(test.getPath());
			URL url = new URL(startURL);
			String host = url.getHost();
			String uri = url.getPath();
			int port = url.getPort();
			if (port != -1)
				host += ":" + port;
			
			SimpleHttpClient client = new SimpleHttpClient();
			SimpleHttpRequest getRequest = new SimpleHttpRequest("GET", "/robots.txt", "HTTP/1.1", "http://" + host + "robots.txt");
			getRequest.addHeader("host", host);
			getRequest.addHeader("user-agent", "cis455crawler");
			SimpleHttpResponse getResponse = client.execute(getRequest);
			String content = getResponse.getContent();
			RobotsTxtInfo info = new RobotsTxtInfo(content);
			m_robotsTxt.put(host, info);
			CrawlerRunnable r = new CrawlerRunnable(startURL, this);
			m_pool.execute(r);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
