// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.junit.*;
import static org.junit.Assert.*;

public class TestChecker{
	@Test public void skipNull(){
		Checker.check(null);
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
}
