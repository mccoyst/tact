// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;
import java.lang.ref.*;

public class ThreadMapTest{
	@Test public void assumptions(){
		Object o = new Object();
		WeakReference<Object> r1 = new WeakReference<Object>(o);
		WeakReference<Object> r2 = new WeakReference<Object>(o);
		//assertEquals(r1, r2); Nope
		ThreadMap.equal(r1, r2);
		//assertEquals(r1.hashCode(), r2.hashCode()); Nope
		assertEquals(ThreadMap.hash(r1), ThreadMap.hash(r2));
	}

	@Test public void create(){
		new ThreadMap();
	}

	@Test public void assign(){
		Map<Object, WeakReference<Thread>> m = new ThreadMap();
	}

	@Test public void put(){
		ThreadMap m = new ThreadMap();
		Object o = new Object();
		m.put(o, new WeakReference<Thread>(Thread.currentThread()));
	}

	@Test public void get(){
		ThreadMap m = new ThreadMap();
		Object o = new Object();
		m.put(o, new WeakReference<Thread>(Thread.currentThread()));
		assertNotNull(m.get(o));
		assertNotNull(m.get(o).get());
	}
}
