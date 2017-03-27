package edu.upenn.cis455.webserver.utils;

import java.io.Serializable;
import java.util.HashMap;

public class UserInfo implements Serializable {
	public String psw = null;
	public HashMap<String, String> channel = new HashMap<String, String>(); 
}