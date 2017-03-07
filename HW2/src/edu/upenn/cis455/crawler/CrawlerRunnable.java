package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Queue;

import org.w3c.tidy.Tidy;

public class CrawlerRunnable implements Runnable {
	private String m_URL;
	private Queue<Runnable> m_taskQueue;
	public CrawlerRunnable(String url, Queue<Runnable> queue) {
		m_URL = url;
		m_taskQueue = queue;
	}
	
	private boolean isTypeWanted(String type) {
		return type.equals("text/html") || type.equals("text/xml") || type.equals("application/xml") || type.endsWith("+xml");
	}
	
	@Override
	public void run() {
		SimpleHttpClient client = new SimpleHttpClient();
		
		URL url = null;
		try {
			url = new URL(m_URL);
			// get the host header
			String host = url.getHost();
			int port = url.getPort();
			if (port != -1)
				host += ":" + port;
			
			SimpleHttpRequest request = new SimpleHttpRequest("GET", url.getPath(), "HTTP/1.1");
			request.addHeader("host", host);
			SimpleHttpResponse response = client.execute(request);
			
			String content = response.getContent();
			String type = response.getContentType();
			if (isTypeWanted(type) && !content.equals(XPathCrawler.dbEnv.getFile(m_URL))) {
				XPathCrawler.dbEnv.insertFile(m_URL, response.getContent());
				Tidy tidy = new Tidy();
				StringReader reader = new StringReader(content);
				tidy.parse(reader, System.out);
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

}
