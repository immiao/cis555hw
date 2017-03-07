package edu.upenn.cis455.crawler;

import java.io.File;
import java.util.concurrent.LinkedBlockingQueue;

import edu.upenn.cis455.webserver.utils.MyDbEnv;

public class XPathCrawler {
	public static MyDbEnv dbEnv = new MyDbEnv();
	public static String m_startURL = null;
	public static String m_dbEnvDir = null;
	public static int m_size = 0;
	public static XPathCrawlerFactory f = new XPathCrawlerFactory();
	public static LinkedBlockingQueue<Runnable> q = new LinkedBlockingQueue<Runnable>(1000); // capability
	public static CrawlerThreadPool pool = new CrawlerThreadPool(100, q);
	
	public static void main(String args[]) {
		if (args.length < 3) {
			System.out.println("Crawler's Arguments don't match");
			return;
		} else {
			m_startURL = args[0];
			m_dbEnvDir = args[1];
			m_size = Integer.parseInt(args[2]) * 1024;
		}
		
		dbEnv.setup(new File(m_dbEnvDir), false);
		XPathCrawler c = f.getCrawler();
		c.run();
	}
	
	public void run() {
		CrawlerRunnable r = new CrawlerRunnable(m_startURL, q);
		pool.execute(r);
	}

}
