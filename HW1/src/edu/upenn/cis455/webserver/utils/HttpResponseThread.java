package edu.upenn.cis455.webserver.utils;

public class HttpResponseThread extends Thread{
	
	private TaskBlockingQueue queue;
	
	public HttpResponseThread(TaskBlockingQueue queue) {
		this.queue = queue;
	}
	
	public void run() {
		try {
			while (true) {
				synchronized (queue) {
					while (queue.empty()) {
						queue.wait();
					}
					queue.poll().run();
				}
			}
		} catch (InterruptedException e) {
			//System.out.println(e.getMessage());
			//System.out.println("shutdown1");
		}
		
	}
	
}
