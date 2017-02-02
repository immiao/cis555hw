package edu.upenn.cis455.webserver.utils;

public class HttpResponseThread extends Thread{
	
	private TaskBlockingQueue queue;
	
	public HttpResponseThread(TaskBlockingQueue queue) {
		this.queue = queue;
	}
	
	public void run() {
		while (true) {
			synchronized (queue) {
				while (!queue.empty()) {
					try {
						queue.wait();
					} catch (InterruptedException e) {
						System.out.println(e.getMessage());
					}
				}
				
				queue.poll().run();
				System.out.println("Task polled");
			}
		}
	}
}
