package edu.upenn.cis455.webserver.utils;

public class ThreadPool {

	private final int nTotalThreads;
	private final HttpResponseThread[] threads;
	private TaskBlockingQueue queue;
	
	public ThreadPool(int totalThreads, int capability) {
		nTotalThreads = totalThreads;
		queue = new TaskBlockingQueue(capability);
		threads = new HttpResponseThread[nTotalThreads];
		
		for (int i = 0; i < nTotalThreads; i++) {
			threads[i] = new HttpResponseThread(queue);
			threads[i].start();
		}
	}
	
	public void execute(Runnable task) {
		synchronized (queue) {
			if (queue.add(task))
				System.out.println("Task added successfully!");
			else
				System.out.println("Fail to add task!");
			queue.notify();
		}
	}
}
