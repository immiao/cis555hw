package edu.upenn.cis455.crawler;

import java.util.AbstractQueue;
import java.util.Queue;


public class CrawlerThreadPool {
	public final int nTotalThreads;
	private final CrawlerThread[] threads;
	// use Queue interface for scalability
	private Queue<Runnable> queue;
	
	public CrawlerThreadPool(int totalThreads, Queue<Runnable> queue) {
		nTotalThreads = totalThreads;
		this.queue = queue;
		threads = new CrawlerThread[nTotalThreads];
		
		for (int i = 0; i < nTotalThreads; i++) {
			threads[i] = new CrawlerThread(queue);
			threads[i].start();
		}
	}
	
	public void execute(Runnable task) {
		synchronized (queue) {
			if (queue.add(task))
				;//System.out.println("Task added successfully!");
			else
				;//System.out.println("Fail to add task!");
			queue.notify();
		}
	}
		
	public void shutdown() {
		for (int i = 0; i < nTotalThreads; i++) {
			threads[i].interrupt();
		}
	}
	
	public String[] getstates() {
		String[] s = new String[nTotalThreads];
		
		for (int i = 0; i < nTotalThreads; i++) {
			s[i] = new String(threads[i].getState().name());
			//System.out.println(s[i]);
		}
		return s;
	}
}
