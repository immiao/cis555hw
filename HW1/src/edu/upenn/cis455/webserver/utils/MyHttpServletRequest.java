package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyHttpServletRequest implements HttpServletRequest {
	
	private MyHttpSession m_session;
	private String m_method;
	private HashMap<String, Vector<String>> m_headerMap;
	private HashMap<String, Object> m_attributes;
	private HashMap<String, Vector<String>> m_params;
	private BufferedReader m_in;
	private String m_charset = "ISO-8859-1";
	private String m_serverName;
	private int m_serverPort;
	private String m_protocol;
	private String m_contextPath;
	private String m_servletPath;
	private String m_pathInfo;
	private String m_queryString;
	private String m_requestURI;
	private String m_requestURL;
	private Locale m_locale;
	
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
	
	public MyHttpServletRequest(BufferedReader in, String method, HashMap<String, Vector<String>> headerMap, HashMap<String, Vector<String>> params, 
			String name, int port, String protocol, String contextPath, String servletPath, String pathInfo, String queryString,
			String requestURI, String requestURL, Locale locale) {
		m_in = in;
		m_method = method;
		m_headerMap = headerMap;
		m_params = params;
		m_serverName = name;
		m_serverPort = port;
		m_protocol = protocol;
		m_contextPath = contextPath;
		m_servletPath = servletPath;
		m_pathInfo = pathInfo;
		m_queryString = queryString;
		m_requestURI = requestURI;
		m_requestURL = requestURL;
		m_locale = locale;
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
	public String getCharacterEncoding() {
		return m_charset;
	}

	@Override
	public int getContentLength() {
		return Integer.parseInt(m_headerMap.get("content-length").get(0));
	}

	@Override
	public String getContentType() {
		return m_headerMap.get("content-type").get(0);
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		return m_serverName;
	}

	@Override
	public int getLocalPort() {
		return m_serverPort;
	}

	@Override
	public Locale getLocale() {
		return m_locale;
	}

	@Override
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public String getParameter(String name) {
		return m_params.get(name).get(0);
	}

	@Override
	public Map getParameterMap() {
		return m_params;
	}

	@Override
	public Enumeration getParameterNames() {
		Set<String> keys = m_params.keySet();
		Vector<String> params = new Vector<String>(keys);
		return params.elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		return m_params.get(name).toArray(new String[0]);
	}

	@Override
	public String getProtocol() {
		return m_protocol;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return m_in;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null; // deprecated
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return m_serverName;
	}

	@Override
	public int getServerPort() {
		return m_serverPort;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String name) {
		m_attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		m_attributes.put(name, value);
	}

	@Override
	public void setCharacterEncoding(String charset) throws UnsupportedEncodingException {
		m_charset = charset;
	}

	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}

	@Override
	public String getContextPath() {
		return m_contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		return m_headerMap.get(name).get(0);
	}

	@Override
	public Enumeration getHeaderNames() {
		Set<String> keys = m_headerMap.keySet();
		Vector<String> headerName = new Vector<String>(keys);
		return headerName.elements();
	}

	@Override
	public Enumeration getHeaders(String name) {
		return m_headerMap.get(name).elements();
	}

	@Override
	public int getIntHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getMethod() {
		return m_method;
	}

	@Override
	public String getPathInfo() {
		return m_pathInfo;
	}

	@Override
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		return m_queryString;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		return m_servletPath;
	}

	@Override
	public HttpSession getSession() {
		return getSession(true);
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (create) {
			if (! hasSession()) {
				m_session = new MyHttpSession();
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		return m_session;
	}

	@Override
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null; // return null
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false; // deprecated
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false; // return false
	}

}
