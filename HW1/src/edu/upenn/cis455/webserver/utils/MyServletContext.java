package edu.upenn.cis455.webserver.utils;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class MyServletContext implements ServletContext {
	private HashMap<String, Object> attributes;
	private HashMap<String, String> initParams;
	private String contextPath;
	private String displayName;

	MyServletContext(HashMap<String, String> initParams, String contextPath, String displayName) {
		this.initParams = initParams;
		this.contextPath = contextPath;
		this.displayName = displayName;
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null; // used for multiple web applications
	}

	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public int getMajorVersion() {
		return 2;
	}

	@Override
	public String getMimeType(String arg0) {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public int getMinorVersion() {
		return 4;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public String getRealPath(String path) {
		return contextPath + path;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public Set getResourcePaths(String arg0) {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public String getServerInfo() {
		return "JavaServer Web Dev Kit/" + getMajorVersion() + "." + getMinorVersion();
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public String getServletContextName() {
		return displayName;
	}

	@Override
	public Enumeration getServletNames() {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public Enumeration getServlets() {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public void log(String arg0) {
		// TODO Auto-generated method stub
		return; // return
	}

	@Override
	public void log(Exception arg0, String arg1) {
		// TODO Auto-generated method stub
		return; // return
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		// TODO Auto-generated method stub
		return; // return
	}

	@Override
	public void removeAttribute(String name) {
		// TODO Auto-generated method stub
		attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object object) {
		// TODO Auto-generated method stub
		attributes.put(name, object);
	}

}
