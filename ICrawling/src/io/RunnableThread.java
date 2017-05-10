package io;

public class RunnableThread implements Runnable {

	Thread runner;
	public RunnableThread() {
	}
	public RunnableThread(String threadName) {
		runner = new Thread(this, threadName); // (1) Create a new thread.
		System.out.println(runner.getName());
		runner.start(); // (2) Start the thread.
	}
	public void run() {
		//Display info about this particular thread
		System.out.println(Thread.currentThread());
	}
}