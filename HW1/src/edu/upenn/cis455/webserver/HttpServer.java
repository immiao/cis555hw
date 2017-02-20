package edu.upenn.cis455.webserver;

import java.net.*;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import edu.upenn.cis455.webserver.utils.*;
import java.io.*;

public class HttpServer {
	static class Handler extends DefaultHandler {
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 10) ? 11 : 21;
			}
		}

		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
				m_servlets.put(m_servletName, value);
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
		}

		private int m_state = 0;
		private String m_servletName;
		private String m_paramName;
		HashMap<String, String> m_servlets = new HashMap<String, String>();
		HashMap<String, String> m_contextParams = new HashMap<String, String>();
		HashMap<String, HashMap<String, String>> m_servletParams = new HashMap<String, HashMap<String, String>>();
	}

	private static HashMap<String, HttpServlet> createServlets(Handler h, FakeContext fc) throws Exception {
		HashMap<String, HttpServlet> servlets = new HashMap<String, HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			FakeConfig config = new FakeConfig(servletName, fc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String, String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

	private static FakeContext createContext(Handler h) {
		FakeContext fc = new FakeContext();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}

	static private boolean isShutDown = false;
	static public ThreadPool threadPool = new ThreadPool(100, 1000);
	static ServerSocket serverSocket;
	public static int port;

	public static void main(String args[]) {
		String rootDir = null;
		String webXmlPath = null;

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
			}

			while (!isShutDown) {
				Socket socket = serverSocket.accept();
				// socket.
				if (socket == null)
					System.out.println("NULL");
				HttpResponseRunnable task = new HttpResponseRunnable(socket, rootDir);

				threadPool.execute(task);
			}
		} catch (Exception e) {
			// System.out.println("SHUTDOWN");
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
