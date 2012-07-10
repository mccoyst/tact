// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;
import java.lang.ref.*;

public class ThreadMapTest{
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
