package edu.upenn.cis455.webserver.utils;

import java.io.Serializable;
import java.util.ArrayList;

public class ChannelInfo implements Serializable {
	public String userName;
	public String name;
	public String xpath;
	public ArrayList<String> url = new ArrayList<String>();
	public ArrayList<String> subscribedUserName = new ArrayList<String>();
	
	public ChannelInfo(ChannelInfo other) {
		userName = new String(other.userName);
		name = new String(other.name);
		xpath = new String(other.xpath);
		url = new ArrayList<String>(other.url);
		subscribedUserName = new ArrayList<String>(other.subscribedUserName);
	}
	
	public ChannelInfo() {
		
	}
}
