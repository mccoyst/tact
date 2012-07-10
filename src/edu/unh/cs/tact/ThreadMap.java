// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import java.lang.ref.*;

public class ThreadMap extends AbstractMap<Object, WeakReference<Thread>>{
	private static class Entry extends
		AbstractMap.SimpleEntry<WeakReference<Object>, WeakReference<Thread>>{
		public Entry(Object key, WeakReference<Thread> value){
			super(new WeakReference<Object>(key), value);
		}
	}

	final List<Entry> entries = new ArrayList<Entry>();

	public Set<Map.Entry<Object, WeakReference<Thread>>> entrySet(){
		return new AbstractSet<Map.Entry<Object, WeakReference<Thread>>>(){

			public Iterator<Map.Entry<Object,WeakReference<Thread>>> iterator(){
				final Iterator<Entry> i = entries.iterator();
				return new Iterator<Map.Entry<Object,WeakReference<Thread>>>(){
					public boolean hasNext(){
						return i.hasNext();
					}
					public Map.Entry<Object, WeakReference<Thread>> next(){
						//TODO: clear out empty ref entries
						Entry e = i.next();
						return new AbstractMap.SimpleEntry<Object, WeakReference<Thread>>(
							e.getKey().get(), e.getValue());
					}
					public void remove(){
						i.remove();
					}
				};
			}

			public int size(){
				//TODO: clean up before counting
				return entries.size();
			}

			public boolean add(Map.Entry<Object, WeakReference<Thread>> e){
				for(Entry entry : entries){
					if(entry.getKey().get() == e.getKey())
						return false;
				}
				entries.add(new Entry(e.getKey(), e.getValue()));
				return true;
			}
		};
	}

	public WeakReference<Thread> put(Object o, WeakReference<Thread> t){
		WeakReference<Thread> old = get(o);
		if(old != null)
			return old;
		entrySet().add(new AbstractMap.SimpleEntry<Object,WeakReference<Thread>>(o, t));
		return t;
	}


	public static boolean equal(WeakReference<Object> r1, WeakReference<Object> r2){
		try{
			return r1.get().equals(r2.get());
		}catch(NullPointerException e){
			return false;
		}
	}

	public static int hash(WeakReference<Object> r){
		try{
			return r.get().hashCode();
		}catch(NullPointerException e){
			return 0;
		}
	}
}
