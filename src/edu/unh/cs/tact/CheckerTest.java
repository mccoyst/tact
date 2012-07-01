// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.junit.*;
import static org.junit.Assert.*;

public class CheckerTest{
	@Test public void skipNull(){
		Checker.check(null);
	}

	@Test public void goodSameThread(){
		Object o = new Object();
		Checker.check(o);
		Checker.check(o);
	}

	@Test public void goodRuntimeGuard(){
		final Object o = new Object();
		final String s = "I'm a guard";
		Checker.guardBy(o, s);
		Thread t = new Thread(new Runnable(){
			public void run(){
				synchronized(s){
					Checker.check(o);
				}
			}
		});
		t.start();
		try{
			t.join();
		}catch(InterruptedException e){
			// hrm
			throw new RuntimeException(e);
		}
	}

	@Test(expected=IllegalAccessError.class)
	public void extraRuntimeGuard(){
		Object o = new Object();
		String s = "I'm a guard";
		Checker.guardBy(o, s);
		Checker.guardBy(o, "I'm different!");
	}

	@Test public void goodStaticRuntimeGuard(){
		final Object o = new Object();
		Checker.guardBy(o, CheckerTest.class);
		doInAnotherThread(new Runnable(){
			public void run(){
				staticCheck(o);
			}
		});
	}

	private synchronized static void staticCheck(Object o){
		Checker.check(o);
	}

	private void doInAnotherThread(Runnable r){
		Thread t = new Thread(r);
		t.start();
		try{
			t.join();
		}catch(InterruptedException e){
			// hrm
			throw new RuntimeException(e);
		}
	}

	private static class ThisDummy{
		// tact won't actually inject this class; these are for illustration
		@GuardedBy("this") int n;
		@GuardedBy("edu.unh.cs.tact.CheckerTest.ThisDummy.slock") int m;
		static final Object slock = new Object();
	}

	@Test public void goodSimpleThis(){
		ThisDummy d = new ThisDummy();
		synchronized(d){
			Checker.guardByThis(d);
		}
	}

	@Test(expected=IllegalAccessError.class)
	public void badSimpleThis(){
		ThisDummy d = new ThisDummy();
		Checker.guardByThis(d);
	}

	@Test public void goodSimpleStatic(){
		ThisDummy d = new ThisDummy();
		synchronized(ThisDummy.slock){
			Checker.guardByStatic(d, ThisDummy.class.getName()+".slock");
		}
	}

	@Test(expected=IllegalAccessError.class)
	public void badSimpleStatic(){
		ThisDummy d = new ThisDummy();
		Checker.guardByStatic(d, ThisDummy.class.getName()+".slock");
	}

	@Test(expected=ClassNotFoundException.class)
	public void badClass() throws Throwable{
		try{
			ThisDummy d = new ThisDummy();
			Checker.guardByStatic(d, "i.dont.exist.Okay.fine");
		}catch(RuntimeException r){
			if(r.getCause() != null)
				throw r.getCause();
			throw r;
		}
	}
}
