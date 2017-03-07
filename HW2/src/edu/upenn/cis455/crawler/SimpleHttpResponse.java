package edu.upenn.cis455.crawler;

import java.util.HashMap;
import java.util.Vector;

public class SimpleHttpResponse {
	private String m_protocol;
	private int m_statusCode;
	private String m_statusMsg;
	private String m_content;
	private HashMap<String, Vector<String>> m_header;
	public SimpleHttpResponse(String protocol, int code, String msg) {
		m_protocol = protocol;
		m_statusCode = code;
		m_statusMsg = msg;
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
	
	public int getContentLength() {
		Vector<String> v = m_header.get("content-length");
		if (v == null)
			return -1; // Chunked
		return Integer.parseInt(v.get(0));
	}
	
	public void setContent(String content) {
		m_content = content;
	}
	
	public String getContent() {
		return m_content;
	}
	
	public String getContentType() {
		Vector<String> v = m_header.get("content-type");
		if (v == null)
			return null;
		return v.get(0);
	}
}
