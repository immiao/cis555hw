package edu.upenn.cis455.webserver;

import java.net.*;
import edu.upenn.cis455.webserver.utils.*;

class HttpServer {
  
  public static void main(String args[]) throws Exception {
    ServerSocket serverSocket = new ServerSocket(8080);
    ThreadPool threadPool = new ThreadPool(4, 100);
    
    while (true) {
    	Socket socket = serverSocket.accept();
    	if (socket == null) System.out.println("NULL");
    	HttpResponseRunnable task = new HttpResponseRunnable(socket);
    	
    	threadPool.execute(task);
    }
  }
  
}
