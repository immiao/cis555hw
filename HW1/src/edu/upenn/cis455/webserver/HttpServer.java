package edu.upenn.cis455.webserver;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.upenn.cis455.webserver.utils.*;
import java.io.*;
public class HttpServer {
  static private boolean isShutDown = false;
  static public ThreadPool threadPool = new ThreadPool(4, 100);
  static ServerSocket serverSocket;
  public static int port;
  
  public static void main(String args[]){
    int waitingNum = 100;
    String rootDir;
    try {
	    if (args.length == 0) {
	    	//System.out.println("***Author: Kaixiang Miao (miaok)");
	    	serverSocket = new ServerSocket(8080);
	    	port = 8080;
	    	rootDir = new String("/");
	    }
	    else {
	    	port = Integer.parseInt(args[0]);
	    	serverSocket = new ServerSocket(port);
	    	if (args.length == 1)
	    		rootDir = new String("/");
	    	else
	    		rootDir = args[1];
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
