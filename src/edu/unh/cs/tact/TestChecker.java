// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.junit.*;
import static org.junit.Assert.*;

public class TestChecker{
	@Test public void skipNull(){
		Checker.check(null);
	}

	@Test public void goodSameThread(){
		Object o = new Object();
		Checker.check(o);
		Checker.check(o);
	}

	@Test public void goodRuntimeGuard(){
		Object o = new Object();
		String s = "I'm a guard";
		Checker.guardBy(o, s);
		synchronized(s){
			Checker.check(o);
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
		Object o = new Object();
		Checker.guardBy(o, TestChecker.class);
		staticCheck(o);
	}

	private synchronized static void staticCheck(Object o){
		Checker.check(o);
	}

	private static class ThisDummy{
		// tact won't actually inject this class; these are for illustration
		@GuardedBy("this") int n;
	}

	@Test public void simpleThis(){
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
}
