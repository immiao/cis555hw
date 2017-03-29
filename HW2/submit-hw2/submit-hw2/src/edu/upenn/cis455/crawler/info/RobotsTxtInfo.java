package edu.upenn.cis455.crawler.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RobotsTxtInfo {

	private HashMap<String, ArrayList<String>> disallowedLinks;
	private HashMap<String, ArrayList<String>> allowedLinks;

	private HashMap<String, Integer> crawlDelays;
	private ArrayList<String> sitemapLinks;
	private ArrayList<String> userAgents;
	private Scanner scanner;

	public RobotsTxtInfo(String content) {
		disallowedLinks = new HashMap<String, ArrayList<String>>();
		allowedLinks = new HashMap<String, ArrayList<String>>();
		crawlDelays = new HashMap<String, Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
		
		scanner = new Scanner(content);
		String userAgent = null;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.isEmpty() || line.charAt(0) == '#')
				continue;
			String[] tokens = line.split(":", 2);
			if (tokens.length != 2)
				return;
			String title = tokens[0].trim().toLowerCase();
			String value = tokens[1].trim();
			if (title.equals("user-agent")) {
				addUserAgent(value);
				userAgent = value;
			} else if (title.equals("allow")) {
				addAllowedLink(userAgent, value);
			} else if (title.equals("disallow")) {
				addDisallowedLink(userAgent, value);
			} else if (title.equals("crawl-delay")) {
				addCrawlDelay(userAgent, Integer.parseInt(value));
			} else if (title.equals("sitemap")) {
				addSitemapLink(value);
			}
		}
		scanner.close();
	}

	public RobotsTxtInfo() {
		disallowedLinks = new HashMap<String, ArrayList<String>>();
		allowedLinks = new HashMap<String, ArrayList<String>>();
		crawlDelays = new HashMap<String, Integer>();
		sitemapLinks = new ArrayList<String>();
		userAgents = new ArrayList<String>();
	}

	public void addDisallowedLink(String key, String value) {
		if (!disallowedLinks.containsKey(key)) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		} else {
			ArrayList<String> temp = disallowedLinks.get(key);
			if (temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			disallowedLinks.put(key, temp);
		}
	}

	public void addAllowedLink(String key, String value) {
		if (!allowedLinks.containsKey(key)) {
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		} else {
			ArrayList<String> temp = allowedLinks.get(key);
			if (temp == null)
				temp = new ArrayList<String>();
			temp.add(value);
			allowedLinks.put(key, temp);
		}
	}

	public void addCrawlDelay(String key, Integer value) {
		crawlDelays.put(key, value);
	}

	public void addSitemapLink(String val) {
		if (!sitemapLinks.contains(val))
			sitemapLinks.add(val);
	}

	public void addUserAgent(String key) {
		userAgents.add(key);
	}

	public boolean containsUserAgent(String key) {
		return userAgents.contains(key);
	}

	public ArrayList<String> getDisallowedLinks(String key) {
		return disallowedLinks.get(key);
	}

	public ArrayList<String> getAllowedLinks(String key) {
		return allowedLinks.get(key);
	}

	public Integer getCrawlDelay(String key) {
		return crawlDelays.get(key);
	}

	public void print() {
		for (String userAgent : userAgents) {
			System.out.println("User-Agent: " + userAgent);
			ArrayList<String> dlinks = disallowedLinks.get(userAgent);
			if (dlinks != null)
				for (String dl : dlinks)
					System.out.println("Disallow: " + dl);
			ArrayList<String> alinks = allowedLinks.get(userAgent);
			if (alinks != null)
				for (String al : alinks)
					System.out.println("Allow: " + al);
			if (crawlDelays.containsKey(userAgent))
				System.out.println("Crawl-Delay: " + crawlDelays.get(userAgent));
			System.out.println();
		}
		if (sitemapLinks.size() > 0) {
			System.out.println("# SiteMap Links");
			for (String sitemap : sitemapLinks)
				System.out.println(sitemap);
		}
	}

	public boolean crawlContainAgent(String key) {
		return crawlDelays.containsKey(key);
	}
	
	public boolean isDisallowedURI(String agent, String URI) {
		ArrayList<String> list = disallowedLinks.get(agent);
		if (list == null)
			return false;
		else {
			for (String l : list) {
				if (URI.contains(l))
					return true;
			}
		}
		return false;
	}
	
	public boolean isAllowedURI(String agent, String URI) {
		ArrayList<String> list = allowedLinks.get(agent);
		if (list == null)
			return false;
		else {
			for (String l : list) {
				if (URI.contains(l))
					return true;
			}
		}
		return false;
	}
}
