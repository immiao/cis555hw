package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.HttpServlet;

import edu.upenn.cis455.webserver.*;

public class HttpParser {
	private InputStream is;
	private String rootDir;
	private HttpServer.Handler m_handler;

	// Response Initial Line
	static private byte[] http10ok200 = "HTTP/1.0 200 OK\r\n".getBytes();
	static private byte[] http10error400 = "HTTP/1.0 400 Bad Request\r\n".getBytes();
	static private byte[] http10error404 = "HTTP/1.0 404 Not Found\r\n".getBytes();
	static private byte[] http10error501 = "HTTP/1.0 501 Not Implemented\r\n".getBytes();
	static private byte[] http11ok200 = "HTTP/1.1 200 OK\r\n".getBytes();
	static private byte[] http11error304 = "HTTP/1.1 304 Not Modified\r\n".getBytes();
	static private byte[] http11error400 = "HTTP/1.1 400 Bad Request\r\n".getBytes();
	static private byte[] http11error404 = "HTTP/1.1 404 Not Found\r\n".getBytes();
	static private byte[] http11error412 = "HTTP/1.1 412 Precondition Failed\r\n".getBytes();
	static private byte[] http11error501 = "HTTP/1.1 501 Not Implemented\r\n".getBytes();
	// Initial Line
	private String method = null;
	private URI uri = null;
	private String dir = null;
	private String protocol = null;
	private String query = null;
	private HashMap<String, Vector<String>> paramsMap = new HashMap<String, Vector<String>>();

	// Header
	private HashMap<String, Vector<String>> headerMap = new HashMap<String, Vector<String>>();

	// hard-coded variables
	private final String serverName = "CIS-555 Web Server";

	// socket info
	private String m_localAddr;
	private int m_localPort;
	private String m_remoteAddr;
	private int m_remotePort;
	
	// servlet
	private HashMap<String, HttpServlet> m_servlets;

	private boolean ParseInitialLine(String[] initialLine) {
		if (initialLine.length != 3)
			return false;
		method = initialLine[0];

		// // handle absolute URL
		// if (initialLine[1].contains("http:/"))
		// initialLine[1] = initialLine[1].substring("http:/".length());
		//
		// File file = new File(initialLine[1]);
		// try {
		// dir = file.getCanonicalPath();
		//
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		System.out.println(initialLine[1]);
		try {
			uri = new URI(initialLine[1]);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dir = uri.getPath();
		query = uri.getQuery();

		// parse query string
		if (query != null) {
			String[] pairArr = query.split("&");
			for (String p : pairArr) {
				String[] keyValue = p.split("=");
				if (keyValue.length != 2)
					return false;
				else {
					if (paramsMap.containsKey(keyValue[0]))
						paramsMap.get(keyValue[0]).add(keyValue[1]);
					else {
						Vector<String> v = new Vector<String>();
						v.add(keyValue[1]);
						paramsMap.put(keyValue[0], v);
					}
				}
			}
		}
		// // test params
		// for (String k : paramsMap.keySet()) {
		// System.out.println(k + " : " + paramsMap.get(k).get(0));
		// }
		protocol = initialLine[2];
		return true;
	}

	private boolean ParseHeader(BufferedReader in) {
		try {
			String line = in.readLine();
			while (!line.isEmpty()) {
				String[] headerline = line.split(":", 2);
				if (headerline.length < 2)
					return false;
				String str = headerline[0].trim().toLowerCase();
				if (headerMap.containsKey(str)) {
					headerMap.get(str).add(headerline[1].trim());
				} else {
					Vector<String> v = new Vector<String>();
					v.add(headerline[1].trim());
					headerMap.put(str, v);
				}
				line = in.readLine();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// initialized function is executed in the main thread
	public HttpParser(InputStream is, String rootDir, HttpServer.Handler h, String localAddr, int localPort,
			String remoteAddr, int remotePort, HashMap<String, HttpServlet> servlet) {
		this.is = is;
		this.rootDir = rootDir;
		this.m_handler = h;
		this.m_localAddr = localAddr;
		this.m_localPort = localPort;
		this.m_remoteAddr = remoteAddr;
		this.m_remotePort = remotePort;
		this.m_servlets = servlet;
		
		MyHttpServletResponse.status.put(100, "Continue");
		MyHttpServletResponse.status.put(101, "Switching Protocols");
		MyHttpServletResponse.status.put(200, "OK");
		MyHttpServletResponse.status.put(201, "Created");
		MyHttpServletResponse.status.put(202, "Accepted");
		MyHttpServletResponse.status.put(203, "Non-Authoritative Information");
		MyHttpServletResponse.status.put(204, "No Content");
		MyHttpServletResponse.status.put(205, "Reset Content");
		MyHttpServletResponse.status.put(206, "Partial Content");
		MyHttpServletResponse.status.put(300, "Multiple Choices");
		MyHttpServletResponse.status.put(301, "Moved Permanently");
		MyHttpServletResponse.status.put(302, "Found");
		MyHttpServletResponse.status.put(303, "See Other");
		MyHttpServletResponse.status.put(304, "Not Modified");
		MyHttpServletResponse.status.put(305, "Use Proxy");
		MyHttpServletResponse.status.put(307, "Temporary Redirect");
		MyHttpServletResponse.status.put(400, "Bad Request");
		MyHttpServletResponse.status.put(401, "Unauthorized");
		MyHttpServletResponse.status.put(402, "Payment Required");
		MyHttpServletResponse.status.put(403, "Forbidden");
		MyHttpServletResponse.status.put(404, "Not Found");
		MyHttpServletResponse.status.put(405, "Method Not Allowed");
		MyHttpServletResponse.status.put(406, "Not Acceptable");
		MyHttpServletResponse.status.put(407, "Proxy Authentication Required");
		MyHttpServletResponse.status.put(408, "Request Timeout");
		MyHttpServletResponse.status.put(409, "Conflict");
		MyHttpServletResponse.status.put(410, "Gone");
		MyHttpServletResponse.status.put(411, "Length Required");
		MyHttpServletResponse.status.put(412, "Precondition Failed");
		MyHttpServletResponse.status.put(413, "Request Entity Too Large");
		MyHttpServletResponse.status.put(414, "Request-URI Too Long");
		MyHttpServletResponse.status.put(415, "Unsupported Media Type");
		MyHttpServletResponse.status.put(416, "Requested Range Not Satisfiable");
		MyHttpServletResponse.status.put(417, "Expectation Failed");		
		MyHttpServletResponse.status.put(500, "Internal Server Error");
		MyHttpServletResponse.status.put(501, "Not Implemented");
		MyHttpServletResponse.status.put(502, "Bad Gateway");
		MyHttpServletResponse.status.put(503, "Service Unavailable");
		MyHttpServletResponse.status.put(504, "Gateway Timeout");
		MyHttpServletResponse.status.put(505, "HTTP Version Not Supported");
		
		MyHttpServletResponse.format.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	public String GetExtensionHeader(String path) {
		int i = path.lastIndexOf('.');
		String ext = path.substring(i + 1).toLowerCase();
		String header = new String();
		if (ext.equals("jpg"))
			header += "Content-Type : image/jpeg";
		else if (ext.equals("gif"))
			header += "Content-Type : image/gif";
		else if (ext.equals("png"))
			header += "Content-Type : image/png";
		else if (ext.equals("txt"))
			header += "Content-Type : text/plain";
		else if (ext.equals("html"))
			header += "Content-Type : text/html";
		return header;
	}

	public void GetResult(OutputStream os) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		BufferedReader in = new BufferedReader(reader);

		byte[] initialLineByte = null;
		byte[] headerByte = null;
		byte[] bodyByte = null;
		String servletName = null;

		try {
			if (!ParseInitialLine(in.readLine().split(" "))) {
				initialLineByte = http10error400;
				headerByte = "\r\n".getBytes();
			}
			// HTTP/1.0
			else {
				// judge if there's any servlet matched
				if (m_handler != null) {
					for (Map.Entry<String, String> entry : m_handler.m_servletMapping.entrySet()) {
						String uri = entry.getValue();
						int length = uri.length();
						//
						if (uri.charAt(length - 1) == '*') {
							uri = uri.substring(0, length - 2);
							if (dir.startsWith(uri))
								servletName = entry.getKey();
						} else {
							if (dir.equals(uri))
								servletName = entry.getKey();
						}
					}
					if (servletName != null)
						System.out.println("MATCHED: " + servletName);
				}

				if (protocol.equals("HTTP/1.0")) {
					String path = new String(rootDir + dir);
					File file = new File(path);

					headerByte = "\r\n".getBytes();
					if (method.equals("GET")) {
						initialLineByte = http10ok200;
						if (dir.equals("/shutdown")) {
							HttpServer.shutdown();
						} else if (dir.equals("/control")) {
							if (servletName == null) {
								String result = new String("<html><body><b>Author: Kaixiang Miao (miaok)<\b>\n\n");
								String[] threadState = HttpServer.threadPool.getstates();

								int i = 0;
								for (String s : threadState) {
									result += "<p>Thread " + i + ":" + s + "</p>\n";
									i++;
								}
								result += "<form action=\"http://localhost:" + HttpServer.port
										+ "/shutdown\"><input type=\"submit\" value=\"Shut Down\" /></form>";
								result += "</body></html>";
								bodyByte = result.getBytes();
							} else {
								// output error log
							}

						} else {
							if (servletName != null) {

							} else if (file.isDirectory()) {
								File[] files = file.listFiles();
								String result = new String("*****This is a directory.*****\n\n");
								for (File f : files) {
									result += f.getName() + "\n";
								}
								bodyByte = result.getBytes();
							} else {
								String header = new String();
								header += GetExtensionHeader(file.getAbsolutePath());
								if (header.length() != 0) {
									header += "\r\n";
									header += "Content-Length:" + file.length() + "\r\n\r\n";
								}
								headerByte = header.getBytes();
								bodyByte = new byte[(int) file.length()];
								FileInputStream fis = new FileInputStream(file);
								fis.read(bodyByte);
								fis.close();
							}
						}
					} else if (method.equals("HEAD")) {
						FileInputStream fis = new FileInputStream(file);
						initialLineByte = http10ok200;
					} else if (method.equals("POST")) {
						initialLineByte = http10error501;
					} else
						initialLineByte = http10error400;
				}
				// HTTP/1.1
				else if (protocol.equals("HTTP/1.1")) {
					String headerStr = new String();
					// date
					Date date = new Date();
					SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm:ss z");
					format.setTimeZone(TimeZone.getTimeZone("GMT"));

					headerStr += "Date: " + format.format(date) + "\r\n";

					if (!ParseHeader(in)) {
						initialLineByte = http11error400;
					} else {
						// check if HOST header exists
						if (!headerMap.containsKey("host")) {
							initialLineByte = http11error400;
						} else {
							// handle absolute URL
							String prefix0 = "/" + headerMap.get("host");
							String prefix1 = "/" + headerMap.get("host").get(0).split(":")[0];
							if (dir.contains(prefix0))
								dir = dir.substring(prefix0.length());
							else if (dir.contains(prefix1))
								dir = dir.substring(prefix1.length());

							String path = new String(rootDir + dir);
							File file = new File(path);
							if (method.equals("GET")) {
								initialLineByte = http11ok200;

								if (dir.equals("/shutdown")) {
									HttpServer.shutdown();
								} else if (dir.equals("/control")) {
									if (servletName == null) {
										String result = new String(
												"<html><body><p><b>Author: Kaixiang Miao (miaok)</b></p>\n\n");
										String[] threadState = HttpServer.threadPool.getstates();

										int i = 0;
										for (String s : threadState) {
											result += "<p>Thread " + i + ":" + s + "</p>\n";
											i++;
										}
										result += "<a href=\"/shutdown\"><button>Shut Down</button></a>";
										result += "</body></html>";
										bodyByte = result.getBytes();
									} else {

									}
								} else {
									if (servletName != null) {
										
										MyHttpServletRequest req = new MyHttpServletRequest(in, method, headerMap,
												paramsMap, serverName, protocol, null, null, null,
												query, uri.toString(), null, null, m_localAddr, m_localPort, m_remoteAddr, m_remotePort);
										MyHttpServletResponse resp = new MyHttpServletResponse(os, 8192);
										resp.addDateHeader("Date", date.getTime());
										resp.setStatus(200);
										HttpServlet s = m_servlets.get(servletName);
										s.service(req, resp);
										resp.flushBuffer();
										return;
									} else if (file.isDirectory()) {
										File[] files = file.listFiles();
										String result = new String("*****This is a directory.*****\n\n");
										for (File f : files) {
											result += f.getName() + "\n";
										}
										bodyByte = result.getBytes();
									} else { // add if-modified-since here
										String modifiedStr = headerMap.get("if-modified-since").get(0);
										String unmodifiedStr = headerMap.get("if-unmodified-since").get(0);
										SimpleDateFormat format0 = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm:ss z");
										SimpleDateFormat format1 = new SimpleDateFormat("EEEE, d-MMM-yy, hh:mm:ss z");
										SimpleDateFormat format2 = new SimpleDateFormat("EEE MMM d hh:mm:ss yyyy");

										Date modifiedDate = new Date(file.lastModified());
										String header = GetExtensionHeader(file.getAbsolutePath());
										if (header.length() != 0) {
											headerStr += header + "\r\n";
											headerStr += "Content-Length:" + file.length() + "\r\n";
										}
										if (modifiedStr != null) {
											Date mydate = null;
											try {
												mydate = format0.parse(modifiedStr);
											} catch (Exception e0) {
												try {
													mydate = format1.parse(modifiedStr);
												} catch (Exception e1) {
													mydate = format2.parse(modifiedStr);
												}
											}

											if (modifiedDate.after(mydate)) {
												bodyByte = new byte[(int) file.length()];
												FileInputStream fis = new FileInputStream(file);
												fis.read(bodyByte);
												fis.close();
											} else
												initialLineByte = http11error304;
										} else if (unmodifiedStr != null) {
											Date mydate = format0.parse(unmodifiedStr);
											if (mydate == null)
												mydate = format1.parse(unmodifiedStr);
											if (mydate == null)
												mydate = format2.parse(unmodifiedStr);

											if (modifiedDate.before(mydate)) {
												bodyByte = new byte[(int) file.length()];
												FileInputStream fis = new FileInputStream(file);
												fis.read(bodyByte);
												fis.close();
											} else
												initialLineByte = http11error412;
										} else {
											bodyByte = new byte[(int) file.length()];
											FileInputStream fis = new FileInputStream(file);
											fis.read(bodyByte);
											fis.close();
										}
									}
								}
							} else if (method.equals("HEAD")) {
								FileInputStream fis = new FileInputStream(file);
								initialLineByte = http11ok200;
							} else if (method.equals("POST")) {
								initialLineByte = http11error501;
							} else
								initialLineByte = http11error400;
						}
					}
					headerByte = (headerStr + "\r\n").getBytes();
				} else {
					initialLineByte = http10error400;
					headerByte = "\r\n".getBytes();
				}
			}

		} catch (Exception e) {
			initialLineByte = (protocol + " 404 Not Found\r\n").getBytes();
			headerByte = "\r\n".getBytes();
		}
		int initialLineLength = 0;
		int headerLength = 0;
		int bodyLength = 0;

		if (initialLineByte != null)
			initialLineLength = initialLineByte.length;
		if (headerByte != null)
			headerLength = headerByte.length;
		if (bodyByte != null)
			bodyLength = bodyByte.length;

		byte[] result = new byte[initialLineLength + headerLength + bodyLength];
		int offset = 0;

		if (initialLineByte != null) {
			// System.out.println("initial line");
			System.arraycopy(initialLineByte, 0, result, 0, initialLineByte.length);
			offset += initialLineByte.length;
		}
		if (headerByte != null) {
			// System.out.println("header");
			System.arraycopy(headerByte, 0, result, offset, headerByte.length);
			offset += headerByte.length;
		}

		if (bodyByte != null) {
			// System.out.println("body");
			System.arraycopy(bodyByte, 0, result, offset, bodyByte.length);
		}

		os.write(result);
	}
}
