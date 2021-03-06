package edu.upenn.cis455.webserver.utils;

public class TaskBlockingQueue {
	
	static class Node {
		Runnable item;
		Node next;
		Node(Runnable task) { item = task; next = null; }
	}
	
	private int capacity;
	private int count;
	private Node preHead, tail;
	
	public TaskBlockingQueue(int capacity) {
		if (capacity <= 0) throw new IllegalArgumentException();
		this.capacity = capacity;
		
		preHead = new Node(null);
		tail = preHead;
	}
	
	public boolean add(Runnable task) {
		if (task == null) throw new NullPointerException();
		if (count == capacity) return false;
		
		Node node = new Node(task);
		
		tail = tail.next = node;
		count++;
		
		return true;
	}
	
	public Runnable poll() {
		if (count == 0) return null;
		
		Node head = preHead.next;
		preHead.next = preHead; // help GC?
		preHead = head;
		
		count--;
		return head.item;
	}
	
	public boolean empty() {
		if (count > 0) return false;
		return true;
	}
	
	public int getCount() {
		return count;
	}
}
