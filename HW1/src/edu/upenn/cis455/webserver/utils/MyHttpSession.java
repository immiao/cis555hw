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
	
	private HashMap<String, Object> m_attributes;
	private String m_id;
	private boolean m_isValid;
	
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
		// TODO Auto-generated method stub
		String stringDate = "1 Jan 1970, 00:00:00 GMT";
		SimpleDateFormat f = new SimpleDateFormat("d MMM yyyy, hh:mm:ss z");
		Date d = null;
		try {
		    d = f.parse(stringDate);
		} catch (ParseException e) {
		    e.printStackTrace();
		}
		Date crtDate = new Date();
		return crtDate.getTime() - d.getTime();
	}

	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ServletContext getServletContext() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub

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
	public void setMaxInactiveInterval(int arg0) {
		// TODO Auto-generated method stub

	}

}
