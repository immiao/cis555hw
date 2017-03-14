package test.edu.upenn.cis455;

import edu.upenn.cis455.crawler.XPathCrawler;
import junit.framework.TestCase;

public class CrawlerTest0 extends TestCase {
	XPathCrawler m_crawler;
	protected void setUp() {
		m_crawler = XPathCrawler.f.getCrawler();
		m_crawler.run("http://crawltest.cis.upenn.edu/", "database", 1000);
	}
	
	public void testPoolSize() {
		assertEquals(m_crawler.m_pool.nTotalThreads, 100);
	}
}
