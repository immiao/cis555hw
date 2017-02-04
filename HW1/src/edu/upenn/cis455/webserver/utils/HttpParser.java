package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpParser {
	private InputStream is;
	private String rootDir;
	
	// Response Initial Line
	static private String ok200 = " 200 OK\r\n\r\n";
	static private String error404 = " 404 Not Found\r\n\r\n";
	// Initial Line
	private String method;
	private String dir;
	private String version;
	
	public boolean ParseInitialLine(String[] initialLine) {
		if (initialLine.length != 3)
			return false;
		method = initialLine[0];
		File file = new File(initialLine[1]);
		try {
			dir  = file.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		version = initialLine[2];
		return true;
	}
	
	public HttpParser(InputStream is, String rootDir) {
		this.is = is;
		this.rootDir = rootDir;
	}
	
	public byte[] GetResult() {
    	InputStreamReader reader = new InputStreamReader(is);
    	BufferedReader in = new BufferedReader(reader);

    	try {
    		String[] initialLine = in.readLine().split(" ");
//    		for (String s : initialLine) {
//    			System.out.println(s);
//    		}
    		
        	if (!ParseInitialLine(initialLine))
        		return (version + " 400 Bad Request\r\n\r\n").getBytes();
        	
        	String path = new String(rootDir + dir);
        	File file = new File(path);
        	System.out.println(path);
        	//System.out.println(method);
        	if (method.equals("GET")) {
	        	if (file.isDirectory()) {
	        		System.out.println("Directory");
	        		File[] files = file.listFiles();
	        		String result = new String("*****This is a directory.*****\n\n");
	        		for (File f : files) {
	        			result += f.getName() + "\n";
	        		}
		        	byte[] b0 = (version + ok200).getBytes();
		        	byte[] b1 = result.getBytes();
			    	byte[] b = new byte[b0.length + b1.length];
			    	
			    	System.arraycopy(b0, 0, b, 0, b0.length);
			    	System.arraycopy(b1, 0, b, b0.length, b1.length);
			    	
			    	return b;
	        	}
	        	else {
		        	byte[] b0 = (version + ok200).getBytes();
		        	byte[] b1 = new byte[(int)file.length()];
		        	
			    	FileInputStream fis = new FileInputStream(file);
			    	System.out.println("HERE");
			    	fis.read(b1);
			    	fis.close();
			    	
			    	byte[] b = new byte[b0.length + b1.length];
			    	
			    	System.arraycopy(b0, 0, b, 0, b0.length);
			    	System.arraycopy(b1, 0, b, b0.length, b1.length);
			    	
			    	return b;
	        	}
        	}
        	else if (method.equals("HEAD")) {
		    	FileInputStream fis = new FileInputStream(file);
		    	return (version + ok200).getBytes();
        	}
        	else
        		return (version + error404).getBytes();
	    	
    	} catch (Exception e) {
    		return (version + error404).getBytes();
    	}
    	//return "HTTP/1.1 200 OK\r\n\r\n<html><body>Hello world!</body></html>\n".getBytes();
    	
//    	if (request.equals("GET / HTTP/1.1")) {
//    		os.write("HTTP/1.1 200 OK\n\n<html><body>Hello world!</body></html>\n".getBytes());
//    	}
//    	else 
//    		out.println("HTTP/1.1 500 Error\n\nNot understood: \"" +request+"\"");
	}
}
