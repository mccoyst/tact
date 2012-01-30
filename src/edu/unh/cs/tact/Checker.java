// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;

class Checker{
	private static Map<Object, Thread> owners = new HashMap<Object, Thread>();

	public static synchronized void check(Object o){
		Thread ct = Thread.currentThread();

		if(!owners.containsKey(o)){
			owners.put(o, ct);
			System.err.printf("Added %s -> %s\n", o, ct);
			return;
		}

		if(owners.get(o).equals(ct)){
			System.err.printf("OK access (%s -> %s)\n", o, ct);
			return;
		}

		throw new IllegalAccessError(String.format("BAD access (%s -> %s)\n", o, ct));
	}
}
