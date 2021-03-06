package edu.upenn.cis455.webserver;

import java.net.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.webserver.utils.*;
import java.io.*;

public class HttpServer {
	public static HashMap<String, String> account = new HashMap<String, String>();
	public static MyDbEnv dbEnv = new MyDbEnv();
	
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	public static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("display-name") == 0) {
				m_state = 5;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20; // 20 is init params
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21; // 21 is init params
			} else if (qName.compareTo("url-pattern") == 0) {
				m_state = 12; 
			}
		}

		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servletClass.put(m_servletName, value);
				m_state = 0;
			} else if (m_state == 5) {
				m_displayName = value;
				m_state = 0;
			} else if (m_state == 10 || m_state == 20) {
				m_paramName = value;
			} else if (m_state == 11) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 21) {
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String, String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String, String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			}
			else if (m_state == 12) {
				if (m_servletName == null) {
					System.err.println("Url pattern '" + value + "' without servlet name");
					System.exit(-1);
				}
				// get servlet name by url
				HashSet<String> s = m_servletMapping.get(m_servletName);
				
				if (s != null)
					s.add(value);
				else {
					s = new HashSet<String>();
					s.add(value);
					m_servletMapping.put(m_servletName, s);
				}
				m_servletName = null;
				m_state = 0;
			}
		}

		private int m_state = 0;
		public String m_servletName;
		public String m_paramName;
		public String m_displayName;
		public HashMap<String, String> m_servletClass = new HashMap<String, String>();
		public HashMap<String, String> m_contextParams = new HashMap<String, String>();
		public HashMap<String, HashMap<String, String>> m_servletParams = new HashMap<String, HashMap<String, String>>();
		public HashMap<String, HashSet<String>> m_servletMapping = new HashMap<String, HashSet<String>>();
	}

	private static HashMap<String, HttpServlet> createServlets(Handler h, MyServletContext c) throws Exception {
		HashMap<String, HttpServlet> servlets = new HashMap<String, HttpServlet>();
		for (String servletName : h.m_servletClass.keySet()) {
			
			String className = h.m_servletClass.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String, String> servletParams = h.m_servletParams.get(servletName);
			MyServletConfig config = new MyServletConfig(servletName, c, servletParams);
			servlet.init(config);
			servlets.put(servletName, servlet);
			//System.out.println(servletName);
		}
		return servlets;
	}

	static private boolean isShutDown = false;
	static public ThreadPool threadPool = new ThreadPool(1, 1000);
	static ServerSocket serverSocket;
	public static int port;
	
	// servlet
	public static HashMap<String, HttpServlet> m_servlets = null;
	// session map
	public static HashMap<String, MyHttpSession> m_sessionMap = new HashMap<String, MyHttpSession>();
	
	public static void main(String args[]) {
		String rootDir = null;
		String webXmlPath = null;
		Handler h = null;
		MyServletContext c = null;
		
		try {
			if (args.length == 0) {
				// System.out.println("***Author: Kaixiang Miao (miaok)");
				serverSocket = new ServerSocket(8080);
				port = 8080;
				rootDir = new String("/");
			} else if (args.length == 1) {
				port = Integer.parseInt(args[0]);
				serverSocket = new ServerSocket(port);
				rootDir = new String("/");
			} else if (args.length == 2) {
				port = Integer.parseInt(args[0]);
				serverSocket = new ServerSocket(port);
				rootDir = args[1];
			} else {
				port = Integer.parseInt(args[0]);
				serverSocket = new ServerSocket(port);
				rootDir = args[1];
				webXmlPath = args[2];
				h = parseWebdotxml(webXmlPath);
				c = new MyServletContext(h.m_contextParams, webXmlPath, h.m_displayName);
				m_servlets = createServlets(h, c);
				// setup database
				dbEnv.setup(new File(h.m_contextParams.get("BDBstore")), false);
			}
//			BufferedReader b = new BufferedReader(new FileReader("ttt.txt"));
//			String s = b.readLine();
			while (!isShutDown) {
				Socket socket = serverSocket.accept();
				// socket.
				if (socket == null)
					System.out.println("NULL");
				HttpResponseRunnable task = new HttpResponseRunnable(socket, rootDir, h, m_servlets, c);
				threadPool.execute(task);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void shutdown() {

		threadPool.shutdown();
		isShutDown = true;
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
