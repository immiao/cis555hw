package edu.upenn.cis455.webserver;

import java.net.*;
import edu.upenn.cis455.webserver.utils.*;
import java.io.*;
public class HttpServer {
  static private boolean isShutDown = false;
  static public ThreadPool threadPool = new ThreadPool(100, 1000);
  static ServerSocket serverSocket;
  public static int port;
  
  public static void main(String args[]){
    String rootDir;
    try {
	    if (args.length == 0) {
	    	//System.out.println("***Author: Kaixiang Miao (miaok)");
	    	serverSocket = new ServerSocket(8080);
	    	port = 8080;
	    	rootDir = new String("/");
	    }
	    else if (args.length < 3){
	    	port = Integer.parseInt(args[0]);
	    	serverSocket = new ServerSocket(port);
	    	if (args.length == 1)
	    		rootDir = new String("/");
	    	else // args.length == 2
	    		rootDir = args[1];
	    }
	    else {
	    	rootDir = new String("/"); 
	    }
	    
	    while (!isShutDown) {
	    	Socket socket = serverSocket.accept();
	    	//socket.
	    	if (socket == null) System.out.println("NULL");
	    	HttpResponseRunnable task = new HttpResponseRunnable(socket, rootDir);
	    	
	    	threadPool.execute(task);
	    }
    } catch (Exception e) {
    	//System.out.println("SHUTDOWN");
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
