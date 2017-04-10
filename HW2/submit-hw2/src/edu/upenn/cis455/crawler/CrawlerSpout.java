package edu.upenn.cis455.crawler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;

public class CrawlerSpout implements IRichSpout {
	
	SpoutOutputCollector collector;
	String executorId = UUID.randomUUID().toString();
	
	@Override
	public String getExecutorId() {
		return executorId;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		//declarer.declare(new Fields("word"));
	}

	@Override
	public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;
        String startURL = config.get("startURL");
        XPathCrawler.m_queue.add(startURL);
        
        // fetch the robots.txt for the start URL
		try {
			URL url = new URL(startURL);
			String host = url.getHost();
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
			XPathCrawler.m_robotsTxt.put(host, info);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public void nextTuple() {
		if (!XPathCrawler.m_queue.isEmpty()) {
			String url = XPathCrawler.m_queue.poll();
			this.collector.emit(new Values<Object>(url));
		}
	}

	@Override
	public void setRouter(IStreamRouter router) {
		collector.setRouter(router);
	}

}
