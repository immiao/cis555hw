package edu.upenn.cis455.crawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class SimpleHttpRequest {
	private String m_method;
	private String m_URI;
	private String m_protocol;
	private HashMap<String, Vector<String>> m_header = new HashMap<String, Vector<String>>();

	public SimpleHttpRequest(String method, String URI, String protocol) {
		m_method = method;
		if (URI.isEmpty())
			m_URI = "/";
		else
			m_URI = URI;
		m_protocol = protocol;
	}

	public void addHeader(String key, String val) {
		Vector<String> v = m_header.get(key.toLowerCase());
		if (v == null) {
			v = new Vector<String>();
			v.add(val);
			m_header.put(key, v);
		} else {
			v.add(val);
		}
	}
	
	public String getHeader(String key) {
		Vector<String> v = m_header.get(key);
		if (v == null)
			return null;
		return v.get(0);
	}
	
	public String getHost() {
		Vector<String> v = m_header.get("host");
		// should not be null
		if (v == null)
			return null;
		return v.get(0);
	}
	
	public String getRequestLine() {
		return m_method + " " + m_URI + " " + m_protocol + "\r\n";
	}
	
	public String getRawString() {
		String result = new String();
		result += getRequestLine();
		for (Map.Entry<String, Vector<String>> entry : m_header.entrySet()) {
			String key = entry.getKey();
			Vector<String> vals = entry.getValue();
			for (String val : vals) {
				result += key + ":" + val + "\r\n";
			}
		}
		result += "\r\n";
		return result;
	}
	
	public String getMethod() {
		return m_method;
	}
}
