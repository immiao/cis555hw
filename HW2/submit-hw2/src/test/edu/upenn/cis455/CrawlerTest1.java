package test.edu.upenn.cis455;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.After;
import org.junit.Before;

import com.sleepycat.je.DatabaseException;

import edu.upenn.cis455.crawler.XPathCrawler;
import edu.upenn.cis455.webserver.utils.MyDbEnv;
import junit.framework.TestCase;


public class CrawlerTest1 extends TestCase {
	XPathCrawler m_crawler;
	protected void setUp() {
		m_crawler = XPathCrawler.f.getCrawler();
		m_crawler.run("http://crawltest.cis.upenn.edu/", "database", 1000);
	}
	
	public void testFileSize() {
		assertEquals(m_crawler.m_size, 1000);
	}
}
