package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import edu.upenn.cis455.webserver.*;

public class HttpParser {
	private InputStream is;
	private String rootDir;
	
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
	private String method;
	private String dir;
	private String version;
	
	// Header
	private HashMap<String, String> headerMap;
	
	public boolean ParseInitialLine(String[] initialLine) {
		if (initialLine.length != 3)
			return false;
		method = initialLine[0];
		
		// handle absolute URL
		if (initialLine[1].contains("http:/"))
			initialLine[1] = initialLine[1].substring("http:/".length());
		
		File file = new File(initialLine[1]);
		try {
			dir  = file.getCanonicalPath();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		version = initialLine[2];
		
		//System.out.println(method + " " + dir + " " + version);
		return true;
	}
	
	public boolean ParseHeader(BufferedReader in) {
		try {
			String line = in.readLine();
			while (!line.isEmpty()) {
				String[] headerline = line.split(":", 2);
				if (headerline.length < 2)
					return false;
				headerMap.put(headerline[0].trim().toLowerCase(), headerline[1].trim());
				line = in.readLine();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	// initialized function is executed in the main thread
	public HttpParser(InputStream is, String rootDir) {
		this.is = is;
		this.rootDir = rootDir;
		
		headerMap = new HashMap<String, String>();
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
	
	public byte[] GetResult() {
    	InputStreamReader reader = new InputStreamReader(is);
    	BufferedReader in = new BufferedReader(reader);

    	byte[] initialLineByte = null;
    	byte[] headerByte = null;
    	byte[] bodyByte = null;
    	
    	try {
        	if (!ParseInitialLine(in.readLine().split(" "))) {
        		initialLineByte = http10error400;
        		headerByte = "\r\n".getBytes();
        		
        		
        	} 
        	// HTTP/1.0
        	else if (version.equals("HTTP/1.0")) {
            	String path = new String(rootDir + dir);
            	File file = new File(path);
            	
            	headerByte = "\r\n".getBytes();
	        	if (method.equals("GET")) {
	        		initialLineByte = http10ok200;
	        		if (dir.equals("/shutdown")) {
	            		HttpServer.shutdown();
	            	}
	        		else if (dir.equals("/control")) {
	        			String result = new String("<html><body><b>Author: Kaixiang Miao (miaok)<\b>\n\n");
	        			String[] threadState = HttpServer.threadPool.getstates();
	        			
	        			int i = 0;
	        			for (String s : threadState) {
	        				result += "<p>Thread " + i + ":" + s + "</p>\n";
	        				i++;
	        			}
	        			result += "<form action=\"http://localhost:" + HttpServer.port + "/shutdown\"><input type=\"submit\" value=\"Shut Down\" /></form>";
	        			result += "</body></html>";
	        			bodyByte = result.getBytes();
	        		}
	        		else if (file.isDirectory()) {
		        		File[] files = file.listFiles();
		        		String result = new String("*****This is a directory.*****\n\n");
		        		for (File f : files) {
		        			result += f.getName() + "\n";
		        		}
			        	bodyByte = result.getBytes();				    	
		        	}
		        	else {
		        		String header = new String();
		        		header += GetExtensionHeader(file.getAbsolutePath());
		        		if (header.length() != 0) {
		        			header += "\r\n";
		        			header += "Content-Length:" + file.length() + "\r\n\r\n";
		        		}
		        		headerByte = header.getBytes();
			        	bodyByte = new byte[(int)file.length()];
				    	FileInputStream fis = new FileInputStream(file);
				    	fis.read(bodyByte);
				    	fis.close();
		        	}
	        	}
	        	else if (method.equals("HEAD")) {
			    	FileInputStream fis = new FileInputStream(file);
			    	initialLineByte = http10ok200;
	        	}
	        	else if (method.equals("POST"))
	        		initialLineByte = http10error501;
	        	else
	        		initialLineByte = http10error400;
        	}
        	// HTTP/1.1
        	else if (version.equals("HTTP/1.1")) {
        		String headerStr = new String();
        		// date
        		Date date = new Date();
        		SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm:ss z");
        		format.setTimeZone(TimeZone.getTimeZone("GMT"));

        		headerStr += "Date: " + format.format(date) + "\r\n";
        		
        		if (!ParseHeader(in)) {
        			initialLineByte = http11error400;
        		}
        		else {
            		// check if HOST header exists
            		if (!headerMap.containsKey("host")) {
            			initialLineByte = http11error400;
            		}
            		else {
                		// handle absolute URL
                		String prefix0 = "/" + headerMap.get("host");
                		String prefix1 = "/" + headerMap.get("host").split(":")[0];   		
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
        	            	}
        	        		else if (dir.equals("/control")) {
        	        			String result = new String("<html><body><p><b>Author: Kaixiang Miao (miaok)</b></p>\n\n");
        	        			String[] threadState = HttpServer.threadPool.getstates();
        	        			
        	        			int i = 0;
        	        			for (String s : threadState) {
        	        				result += "<p>Thread " + i + ":" + s + "</p>\n";
        	        				i++;
        	        			}
        	        			result += "<a href=\"/shutdown\"><button>Shut Down</button></a>";
        	        			result += "</body></html>";
        	        			bodyByte = result.getBytes();
        	        		}
        	        		else if (file.isDirectory()) {
        		        		File[] files = file.listFiles();
        		        		String result = new String("*****This is a directory.*****\n\n");
        		        		for (File f : files) {
        		        			result += f.getName() + "\n";
        		        		}
        			        	bodyByte = result.getBytes();
        		        	}
        		        	else { // add if-modified-since here
        		        		String modifiedStr = headerMap.get("if-modified-since");
        		        		String unmodifiedStr = headerMap.get("if-unmodified-since");
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
                		        		bodyByte = new byte[(int)file.length()];
                				    	FileInputStream fis = new FileInputStream(file);
                				    	fis.read(bodyByte);
                				    	fis.close();
        		        			}
        		        			else
        		        				initialLineByte = http11error304;
        		        		}
        		        		else if (unmodifiedStr != null) {
        		        			Date mydate = format0.parse(unmodifiedStr);
        		        			if (mydate == null) mydate = format1.parse(unmodifiedStr);
        		        			if (mydate == null) mydate = format2.parse(unmodifiedStr);
        		        			
        		        			if (modifiedDate.before(mydate)) {
                		        		bodyByte = new byte[(int)file.length()];
                				    	FileInputStream fis = new FileInputStream(file);
                				    	fis.read(bodyByte);
                				    	fis.close();
        		        			}
        		        			else
        		        				initialLineByte = http11error412;
        		        		}
        		        		else {
            		        		bodyByte = new byte[(int)file.length()];
            				    	FileInputStream fis = new FileInputStream(file);
            				    	fis.read(bodyByte);
            				    	fis.close();
        		        		}
        		        	}
        	        	}
        	        	else if (method.equals("HEAD")) {
        			    	FileInputStream fis = new FileInputStream(file);
        			    	initialLineByte = http11ok200;
        	        	}
        	        	else if (method.equals("POST")) {
        	        		initialLineByte = http11error501;
        	        	}
        	        	else
        	        		initialLineByte = http11error400;
            		}
        		}    			
        		headerByte = (headerStr + "\r\n").getBytes();
        	}
        	else {
        		initialLineByte = http10error400;
        		headerByte = "\r\n".getBytes();
        	}
	    	
    	} catch (Exception e) {
    		initialLineByte = (version + " 404 Not Found\r\n").getBytes();
    		headerByte = "\r\n".getBytes();
    	} 
    	int initialLineLength = 0;
    	int headerLength = 0;
    	int bodyLength = 0;
    	
    	if (initialLineByte != null) initialLineLength = initialLineByte.length;
    	if (headerByte != null) headerLength = headerByte.length;
    	if (bodyByte != null) bodyLength = bodyByte.length;
    	
    	byte[] result = new byte[initialLineLength + headerLength + bodyLength];
    	int offset = 0;
    	
    	if (initialLineByte != null) {
    		//System.out.println("initial line");
	    	System.arraycopy(initialLineByte, 0, result, 0, initialLineByte.length);
	    	offset += initialLineByte.length;
    	}
    	if (headerByte != null) {
    		//System.out.println("header");
    		System.arraycopy(headerByte, 0, result, offset, headerByte.length);
    		offset += headerByte.length;
    	}
    	
    	if (bodyByte != null) {
    		//System.out.println("body");
    		System.arraycopy(bodyByte, 0, result, offset, bodyByte.length);
    	}
    	
    	return result;
	}
}
