// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import java.lang.ref.*;

public class ThreadMap implements Map<Object, WeakReference<Thread>>{
	public WeakReference<Thread> put(Object key, WeakReference<Thread> value){
		Bucket b = lookup(key);
		if(b == null){
			b = new Bucket();
			attach(key, b);
		}
		return b.add(key, value);
	}

	public WeakReference<Thread> get(Object key){
		Bucket b = lookup(key);
		if(b == null)
			return null;
		return b.get(key);
	}

	public WeakReference<Thread> remove(Object key){
		Bucket b = lookup(key);
		if(b == null)
			return null;
		return b.remove(key);
	}

	private void attach(Object key, Bucket b){
		table[System.identityHashCode(key) % table.length] = b;
	}

	private Bucket lookup(Object key){
		return table[System.identityHashCode(key) % table.length];
	}

	private final Bucket[] table = new Bucket[4096];

	private static class Bucket{
		List<Entry> entries = new ArrayList<Entry>();

		public WeakReference<Thread> add(Object key, WeakReference<Thread> value){
			Entry e = find(key);
			if(e != null)
				return e.value;
			entries.add(new Entry(key, value));
			return null;
		}

		public WeakReference<Thread> get(Object key){
			Entry e = find(key);
			if(e != null)
				return e.value;
			return null;
		}

		public WeakReference<Thread> remove(Object key){
			Entry e = find(key);
			if(e == null)
				return null;
			entries.remove(e);
			return e.value;
		}

		private Entry find(Object key){
			purgeDead();
			for(Entry e : entries)
				if(e.key.get() == key) return e;
			return null;
		}

		private void purgeDead(){
			List<Entry> dead = new ArrayList<Entry>();
			for(Entry e : entries)
				if(e.key.get() == null)
					dead.add(e);
			entries.removeAll(dead);
		}
	}

	private static class Entry{
		public final WeakReference<Object> key;
		public final WeakReference<Thread> value;

		public Entry(Object key, WeakReference<Thread> value){
			this.key = new WeakReference<Object>(key);
			this.value = value;
		}
	}


	// Junk below

	public void clear(){
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key){
		throw new UnsupportedOperationException();
	}

	public boolean containsValue(Object value){
		throw new UnsupportedOperationException();
	}

	public Set<Map.Entry<Object,WeakReference<Thread>>> entrySet(){
		throw new UnsupportedOperationException();
	}

	public boolean equals(Object o){
		throw new UnsupportedOperationException();
	}

	public int hashCode(){
		throw new UnsupportedOperationException();
	}

	public boolean isEmpty(){
		throw new UnsupportedOperationException();
	}

	public Set<Object> keySet(){
		throw new UnsupportedOperationException();
	}

	public void putAll(Map<? extends Object, ? extends WeakReference<Thread>> m){
		throw new UnsupportedOperationException();
	}

	public int size(){
		throw new UnsupportedOperationException();
	}

	public Collection<WeakReference<Thread>> values(){
		throw new UnsupportedOperationException();
	}
}
