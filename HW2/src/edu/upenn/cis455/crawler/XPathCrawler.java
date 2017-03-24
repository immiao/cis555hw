package edu.upenn.cis455.crawler;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepycat.je.DatabaseException;
import com.sun.org.apache.xerces.internal.util.URI;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.webserver.utils.MyDbEnv;
import test.edu.upenn.cis.stormlite.PrintBolt;
import test.edu.upenn.cis.stormlite.WordCounter;
import test.edu.upenn.cis.stormlite.WordSpout;

public class XPathCrawler {
	public static XPathCrawlerFactory f = new XPathCrawlerFactory();
	// public LinkedBlockingQueue<Runnable> m_queue = new
	// LinkedBlockingQueue<Runnable>(1000); // capability
	// public CrawlerThreadPool m_pool = new CrawlerThreadPool(100, m_queue);
	// public MyDbEnv m_dbEnv = new MyDbEnv();
	public int m_size = 0;
	// public HashMap<String, RobotsTxtInfo> m_robotsTxt = new HashMap<String,
	// RobotsTxtInfo>();
	// public HashMap<String, Long> m_hostLastVisit = new HashMap<String,
	// Long>();

	public static HashMap<String, RobotsTxtInfo> m_robotsTxt = new HashMap<String, RobotsTxtInfo>();
	public static MyDbEnv m_dbEnv = new MyDbEnv();
	public static Queue<String> m_queue = new LinkedList<String>();
	public static HashMap<String, Long> m_hostLastVisit = new HashMap<String, Long>();

	public static void main(String args[])
			throws NumberFormatException, InterruptedException, DatabaseException, NoSuchAlgorithmException {
		if (args.length < 3) {
			System.out.println("Crawler's Arguments don't match");
			return;
		} else {
			XPathCrawler c = f.getCrawler();
			if (args[0].charAt(args[0].length() - 1) != '/')
				args[0] += '/';
			c.run(args[0], args[1], Integer.parseInt(args[2]) * 1024 * 1024);
		}
	}

	static public void hostVisitDelay(String host) {
		// is it necessary
		Long crt = null;
		Long duration = null;
		Integer delay = null;
		synchronized (m_hostLastVisit) {
			Long lastVisit = m_hostLastVisit.get(host);

			if (lastVisit == null) {
				Long i = new Date().getTime();
				m_hostLastVisit.put(host, i);
				System.out.println(host);
				return;
			} else {
				RobotsTxtInfo info = m_robotsTxt.get(host);
				if (info == null)
					return;
				delay = info.getCrawlDelay("cis455crawler");
				if (delay == null)
					delay = info.getCrawlDelay("*");
				if (delay == null)
					return;

				crt = new Date().getTime();
				duration = crt - lastVisit;

				// System.out.println(duration);
				if (delay * 1000 < duration) {
					m_hostLastVisit.put(host, crt);
					return;
				} else {
					m_hostLastVisit.put(host, lastVisit + delay * 1000);
				}
			}
		}
		try {

			TimeUnit.SECONDS.sleep(delay - duration / 1000);

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// }
	}
	// MS1 run
	// public void run(String startURL, String dbEnvDir, int size) {
	// m_size = size;
	// try {
	// m_dbEnv.setup(new File(dbEnvDir), false);
	// } catch (DatabaseException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (NoSuchAlgorithmException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//// HashMap<String, Integer> m = new HashMap<String, Integer>();
	//// Integer test = m.get("sdsa");
	//// if (test == null)
	//// System.out.println("NULL");
	//// URL url = null;
	//// URI abc = null;
	//// abc.
	//// try {
	//// url = new URL("http://abc.com/abc/index.html");
	//// } catch (MalformedURLException e) {
	//// // TODO Auto-generated catch block
	//// e.printStackTrace();
	//// }
	//// System.out.println(url.getHost());
	//// m_dbEnv.insertFile("abc", "test");
	//// m_dbEnv.insertFile("abc", "test1");
	//// System.out.println(m_dbEnv.getFile("abc"));
	// try {
	// //URL test = new URL("http://abc.com//misc//index.html");
	// //System.out.println(test.getPath());
	// URL url = new URL(startURL);
	// String host = url.getHost();
	// String uri = url.getPath();
	// int port = url.getPort();
	// if (port != -1)
	// host += ":" + port;
	//
	// SimpleHttpClient client = new SimpleHttpClient();
	// SimpleHttpRequest getRequest = new SimpleHttpRequest("GET",
	// "/robots.txt", "HTTP/1.1", "http://" + host + "robots.txt");
	// getRequest.addHeader("host", host);
	// getRequest.addHeader("user-agent", "cis455crawler");
	// SimpleHttpResponse getResponse = client.execute(getRequest);
	// String content = getResponse.getContent();
	// RobotsTxtInfo info = new RobotsTxtInfo(content);
	// m_robotsTxt.put(host, info);
	// CrawlerRunnable r = new CrawlerRunnable(startURL, this);
	// m_pool.execute(r);
	// } catch (MalformedURLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// }

	private static final String CRAWLER_SPOUT = "CRAWLER_SPOUT";
	private static final String CRAWLER_BOLT = "CRAWLER_BOLT";
	private static final String PARSER_BOLT = "PARSER_BOLT";
	private static final String FILTER_BOLT = "FILTER_BOLT";

	// this is for MS2
	public void run(String startURL, String dbEnvDir, int size)
			throws InterruptedException, DatabaseException, NoSuchAlgorithmException {
		m_dbEnv.setup(new File(dbEnvDir), false);

		Config config = new Config();
		config.put("startURL", startURL);
		config.put("dbEnvDir", dbEnvDir);
		config.put("size", Integer.toString(size));

		CrawlerSpout crawlerSpout = new CrawlerSpout();
		CrawlerBolt crawlerBolt = new CrawlerBolt();
		ParserBolt parserBolt = new ParserBolt();
		FilterBolt filterBolt = new FilterBolt();

		// wordSpout ==> countBolt ==> MongoInsertBolt
		TopologyBuilder builder = new TopologyBuilder();

		// Only one source ("spout") for the words
		builder.setSpout(CRAWLER_SPOUT, crawlerSpout, 1);

		// Four parallel word counters, each of which gets specific words
		builder.setBolt(CRAWLER_BOLT, crawlerBolt, 4).shuffleGrouping(CRAWLER_SPOUT);

		// A single printer bolt (and officially we round-robin)
		builder.setBolt(PARSER_BOLT, parserBolt, 4).shuffleGrouping(CRAWLER_BOLT);

		builder.setBolt(FILTER_BOLT, filterBolt, 4).shuffleGrouping(PARSER_BOLT);

		LocalCluster cluster = new LocalCluster();
		Topology topo = builder.createTopology();

		ObjectMapper mapper = new ObjectMapper();
		try {
			String str = mapper.writeValueAsString(topo);

			System.out.println("The StormLite topology is:\n" + str);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		cluster.submitTopology("test", config, builder.createTopology());
		Thread.sleep(300000);
		cluster.killTopology("test");
		cluster.shutdown();
		System.exit(0);
	}
}
