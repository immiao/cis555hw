package edu.upenn.cis455.webserver;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import edu.upenn.cis455.webserver.utils.*;
import java.io.*;
class HttpServer {
  
  public static void main(String args[]) throws Exception {
    ServerSocket serverSocket;
    String rootDir;
    ThreadPool threadPool = new ThreadPool(4, 100);
    
//    String s = "Hello World! ";
//    byte data[] = s.getBytes();
//    Path p = Paths.get("../logfile11.txt");
//
//    try (OutputStream out = new BufferedOutputStream(
//      Files.newOutputStream(p))) {
//      out.write(data, 0, data.length);
//    } catch (IOException x) {
//      System.err.println(x);
//    }
    
    if (args.length == 0) {
    	System.out.println("***Author: Kaixiang Miao (miaok)");
    	serverSocket = new ServerSocket(8080);
    	rootDir = new String("");
    }
    else {
    	serverSocket = new ServerSocket(Integer.parseInt(args[0]));
    	if (args.length == 1)
    		rootDir = new String("");
    	else
    		rootDir = args[1];
    }
    
    while (true) {
    	Socket socket = serverSocket.accept();

    	//socket.
    	if (socket == null) System.out.println("NULL");
    	HttpResponseRunnable task = new HttpResponseRunnable(socket, rootDir);
    	
    	threadPool.execute(task);
    }

  }
  
}
