package edu.upenn.cis455.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;
import java.util.Queue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis455.webserver.utils.MyDbEnv;
import edu.upenn.cis455.webserver.utils.PageInfo;

public class FilterBolt implements IRichBolt {
	
	@Override
	public String getExecutorId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(Tuple input) {
		String strURL = (String) input.getValues().get(0);
		synchronized (XPathCrawler.m_queue) {
			XPathCrawler.m_queue.add(strURL);
		}
	}

	@Override
	public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
		
	}

	@Override
	public void setRouter(IStreamRouter router) {
		// TODO Auto-generated method stub

	}

	@Override
	public Fields getSchema() {
		// TODO Auto-generated method stub
		return null;
	}

}
