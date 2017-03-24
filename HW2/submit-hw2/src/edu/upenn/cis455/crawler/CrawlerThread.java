package edu.upenn.cis455.crawler;

import java.util.Queue;


public class CrawlerThread extends Thread{
	private Queue<Runnable> queue;
	
	public CrawlerThread(Queue<Runnable> queue) {
		this.queue = queue;
	}
	
	public void run() {
		try {
			while (true) {
				synchronized (queue) {
					while (queue.isEmpty()) {
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
