package edu.upenn.cis455.crawler;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.webserver.utils.MyDbEnv;

public class XPathCrawler {
	public static XPathCrawlerFactory f = new XPathCrawlerFactory();
	public LinkedBlockingQueue<Runnable> m_queue = new LinkedBlockingQueue<Runnable>(1000); // capability
	public CrawlerThreadPool m_pool = new CrawlerThreadPool(100, m_queue);
	public MyDbEnv m_dbEnv = new MyDbEnv();
	public int m_size = 0;
	
	public static void main(String args[]) {
		if (args.length < 3) {
			System.out.println("Crawler's Arguments don't match");
			return;
		} else {
			XPathCrawler c = f.getCrawler();
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
//		m_dbEnv.insertFile("abc", "test");
//		m_dbEnv.insertFile("abc", "test1");
//		System.out.println(m_dbEnv.getFile("abc"));
		CrawlerRunnable r = new CrawlerRunnable(startURL, this);
		m_pool.execute(r);
	}

}
