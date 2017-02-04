package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HttpResponseRunnable implements Runnable{
	private Socket socket;
	private String rootDir;
	private HttpParser httpParser;
	
	public HttpResponseRunnable(Socket socket, String rootDir) {
		this.socket = socket;
		this.rootDir = rootDir;
		try {
			httpParser = new HttpParser(socket.getInputStream(), rootDir);
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void run() {
		try {
			OutputStream os = socket.getOutputStream();
			socket.setSoTimeout(5000); // does it work?
			os.write(httpParser.GetResult());
			os.flush();
			os.close();
	    	//out.
		} catch (SocketTimeoutException e) {
			System.out.println("TIMEOUT");
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
		
		try {
			socket.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	
	}
}
