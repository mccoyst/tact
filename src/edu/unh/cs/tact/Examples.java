// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

class Examples{
	public static void callChain(){
		Integer o = 42;
		ident(ident(o));
	}

	public static <T> T ident(T o){
		return o;
	}
}
