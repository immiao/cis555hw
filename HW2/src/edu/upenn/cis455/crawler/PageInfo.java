package edu.upenn.cis455.crawler;

import java.io.Serializable;

public class PageInfo implements Serializable {
	/**
	 * 
	 */
	//private static final long serialVersionUID = 1L;
	private long m_lastModified;
	
	PageInfo(long lastModified) {
		m_lastModified = lastModified;
	}
	
	public long getLastModified() {
		return m_lastModified;
	}
}
