package edu.upenn.cis455.webserver.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class UserInfo implements Serializable {
	public String psw = null;
	public ArrayList<String> owned = new ArrayList<String>(); 
	public ArrayList<String> subscribed = new ArrayList<String>(); 
}