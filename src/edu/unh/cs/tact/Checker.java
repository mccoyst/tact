// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import static java.util.Collections.*;
import java.lang.ref.*;

class Checker{
	private static Map<Object, WeakReference<Thread>> owners =
		synchronizedMap(new WeakHashMap<Object, WeakReference<Thread>>());

	public static void check(Object o){
		if(o == null)
			return;

		Thread ct = Thread.currentThread();
		WeakReference<Thread> ref = owners.get(o);

		if(ref == null){
			owners.put(o, new WeakReference<Thread>(ct));
//			System.err.printf("OK claim \"%s\" -> %s\n", o, ct);
			return;
		}

		Thread owner = ref.get();
		if(owner == null){
			owners.put(o, new WeakReference<Thread>(ct));
//			System.err.printf("OK re-thread \"%s\" -> %s\n", o, ct);
		}

		if(owner.equals(ct)){
//			System.err.printf("OK access (%s -> %s)\n", o, ct);
			return;
		}

		throw new IllegalAccessError(String.format("BAD access (%s -> %s)\n", o, ct));
	}

	public static void release(Object o){
		if(o == null)
			return;

		Thread ct = Thread.currentThread();
		WeakReference<Thread> ref = owners.get(o);
		if(ref == null)
			throw new IllegalAccessError(
				String.format("BAD release-unowned (%s <- %s)\n", o, ct));

		Thread owner = ref.get();
		if(owner == null){
//			System.err.printf("OK release-again (%s <- %s)\n", o, ct);
			return;
		}

		if(owner.equals(ct)){
			owners.remove(o);
//			System.err.printf("OK release (%s <- %s)\n", o, ct);
			return;
		}

		throw new IllegalAccessError(String.format("BAD release (%s <- %s)\n", o, ct));
	}
}
