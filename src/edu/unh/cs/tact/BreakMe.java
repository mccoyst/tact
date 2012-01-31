// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.concurrent.*;

class BreakMe implements Runnable{
	public int someField;

	public static void codeToBreak(){
		BreakMe bm = new BreakMe();
		bm.someField = 666;

		new Thread(bm, "that's broken!").start();
	}

	public static void codeThatWorks(){
		BreakMe bm = new BreakMe();

		Thread t = new Thread(bm, "that's good!");
		t.start();
		try{
			t.join();
		}catch(InterruptedException e){
			// TODO: whatever
			System.err.println("Join was interrupted, try again");
		}

		bm.someField = 666;
	}

	public void run(){
		someField = 42;
		Checker.release(this);
	}
}
