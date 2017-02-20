package edu.upenn.cis455.webserver.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class MyServletConfig implements ServletConfig {
	private String m_name;
	private MyServletContext m_context;
	private HashMap<String, String> m_initParams;
	
	public MyServletConfig(String name, MyServletContext context, HashMap<String, String> initParams) {
		m_name = name;
		m_context = context;
		m_initParams = initParams;
	}
	
	@Override
	public String getInitParameter(String name) {
		return m_initParams.get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		Set<String> keys = m_initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public ServletContext getServletContext() {
		return m_context;
	}

	@Override
	public String getServletName() {
		return m_name;
	}

}
