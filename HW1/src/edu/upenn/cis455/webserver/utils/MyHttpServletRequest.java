package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.Principal;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MyHttpServletRequest implements HttpServletRequest {

	private MyHttpSession m_session = null;
	private String m_method;
	private HashMap<String, Vector<String>> m_headerMap;
	private HashMap<String, Object> m_attributes;
	private HashMap<String, Vector<String>> m_params;
	private BufferedReader m_in;
	private String m_charset = "ISO-8859-1";
	private String m_serverName;
	private String m_protocol;
	private String m_contextPath;
	private String m_servletPath;
	private String m_pathInfo;
	private String m_queryString;
	private String m_requestURI;
	private StringBuffer m_requestURL;
	private Locale m_locale = null;
	private String m_localAddr;
	private int m_localPort;
	private String m_remoteAddr;
	private int m_remotePort;
	private Vector<Cookie> m_cookies = new Vector<Cookie>();

	// date
	private String m_dateKey = null;
	private Date m_dateValue = null;
	
	private MyServletContext m_servletContext;
	boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}

	public MyHttpServletRequest(BufferedReader in, String method, HashMap<String, Vector<String>> headerMap,
			HashMap<String, Vector<String>> params, String name, String protocol, String contextPath,
			String servletPath, String pathInfo, String queryString, String requestURI, String requestURL, 
			String localAddr, int localPort, String remoteAddr, int remotePort, MyServletContext context,
			MyHttpSession session) {
		m_in = in;

		m_method = method;
		m_headerMap = headerMap;
		m_params = params;
		m_serverName = name;
		m_protocol = protocol;
		m_contextPath = contextPath;
		m_servletPath = servletPath;
		m_pathInfo = pathInfo;
		m_queryString = queryString;
		m_requestURI = requestURI;
		m_requestURL = new StringBuffer(requestURL);
		m_localAddr = localAddr;
		m_localPort = localPort;
		m_remoteAddr = remoteAddr;
		m_remotePort = remotePort;

		m_servletContext = context;
		m_session = session;
		
		for (String key : headerMap.keySet()) {
			if (key.equals("cookie")) {
				Vector<String> value = headerMap.get(key);
				for (String s : value) {
					//System.out.println(s);
					String[] pair = s.split(";");
					for (String p : pair) {
						String[] nameValue = p.split("=");
						Cookie cookie = new Cookie(nameValue[0].trim(), nameValue[1]);
						m_cookies.add(cookie);
					}
				}
			}
		}
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
		return m_localAddr;
	}

	@Override
	public String getLocalName() {
		return m_serverName;
	}

	@Override
	public int getLocalPort() {
		return m_localPort;
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
		return m_remoteAddr;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		return m_remotePort;
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
		return m_localPort;
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
		return m_cookies.toArray(new Cookie[0]);
	}

	@Override
	public long getDateHeader(String key) {
		if (m_dateKey != null && m_dateKey.equals(key))
			return m_dateValue.getTime();
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
		if (m_headerMap.containsKey(name))
			return m_headerMap.get(name).elements();
		return null;
	}

	@Override
	public int getIntHeader(String name) {
		if (m_headerMap.containsKey(name))
			return Integer.parseInt(m_headerMap.get(name).get(0));
		return -1;
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
		return m_requestURI;
	}

	@Override
	public StringBuffer getRequestURL() {
		return m_requestURL;
	}

	@Override
	public String getRequestedSessionId() {
		if (m_headerMap.containsKey("jsession-id"))
			return m_headerMap.get("jsession-id").get(0);
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
			if (!hasSession()) {
				m_session = new MyHttpSession(UUID.randomUUID().toString(), m_servletContext);
			}
		} else {
			if (!hasSession()) {
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
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// TODO Auto-generated method stub
		return false; // deprecated
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		if (getSession(false) == null)
			return false;
		return true;
	}

	@Override
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false; // return false
	}

}
