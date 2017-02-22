package edu.upenn.cis455.webserver.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class MyHttpSession implements HttpSession {
	
	private HashMap<String, Object> m_attributes = new HashMap<String, Object>();
	private String m_id;
	private boolean m_isValid;
	private Date m_creationTime;
	private Date m_lastAccessedTime;
	private MyServletContext m_servletContext;
	private int m_maxInactiveInterval = -1;
	
	public MyHttpSession(String id, MyServletContext context) {
		m_id = id;
		m_isValid = true;
		m_creationTime = new Date();
		m_lastAccessedTime = m_creationTime;
		m_servletContext = context;
	}
	
	public void access() {
		m_lastAccessedTime = new Date();
		if (m_maxInactiveInterval != -1 && m_lastAccessedTime.getTime() - m_creationTime.getTime() > m_maxInactiveInterval)
			m_isValid = false;
	}
	
	public boolean isValid() {
		return m_isValid;
	}
	@Override
	public Object getAttribute(String name) {
		return m_attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		Set<String> keys = m_attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public long getCreationTime() {
		return m_creationTime.getTime();
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public long getLastAccessedTime() {
		return m_lastAccessedTime.getTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		return m_maxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		return m_servletContext;
	}

	@Override
	public HttpSessionContext getSessionContext() {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public Object getValue(String arg0) {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public String[] getValueNames() {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public void invalidate() {
		m_isValid = false;
	}

	@Override
	public boolean isNew() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void putValue(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		return; // deprecated
	}

	@Override
	public void removeAttribute(String name) {
		m_attributes.remove(name);
	}

	@Override
	public void removeValue(String arg0) {
		// TODO Auto-generated method stub
		return; // deprecated
	}

	@Override
	public void setAttribute(String name, Object value) {
		m_attributes.put(name, value);
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		m_maxInactiveInterval = interval;
	}

}
