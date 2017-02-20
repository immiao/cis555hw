package edu.upenn.cis455.webserver.utils;

import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import edu.upenn.cis455.webserver.HttpServer;

public class HttpResponseRunnable implements Runnable{
	private Socket m_socket;
	private HttpParser m_httpParser;
	
	public HttpResponseRunnable(Socket socket, String rootDir, HttpServer.Handler h) {
		m_socket = socket;
		try {
			m_httpParser = new HttpParser(socket.getInputStream(), rootDir, h);
		} catch(Exception e) {
			//System.out.println(e.getMessage());
		}
	}
	
	public void run() {
		try {
			OutputStream os = m_socket.getOutputStream();
			m_socket.setSoTimeout(5000); // does it work?
			os.write(m_httpParser.GetResult());
			os.flush();
			os.close();
	    	//out.
		} catch (SocketTimeoutException e) {
			//System.out.println("TIMEOUT");
		} catch(Exception e) {
			//System.out.println(e.getMessage());
		}
		
		try {
			m_socket.close();
		} catch(Exception e) {
			//System.out.println(e.getMessage());
		}
	}
}
