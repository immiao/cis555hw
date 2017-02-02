package edu.upenn.cis455.webserver.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HttpResponseRunnable implements Runnable{
	private Socket socket;
	
	public HttpResponseRunnable(Socket socket) {
		this.socket = socket;
	}
	
	public void run() {
		try {
	    	InputStreamReader reader = new InputStreamReader(socket.getInputStream());
	    	BufferedReader in = new BufferedReader(reader);
	    	PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	    	
	    	String request = in.readLine();
	    	if (request.equals("GET / HTTP/1.1"))
	    		out.println("HTTP/1.1 200 OK\n\n<html><body>Hello world!</body></html>\n");
	    	else
	    		out.println("HTTP/1.1 500 Error\n\nNot understood: \"" +request+"\"");
	    	socket.close();
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
