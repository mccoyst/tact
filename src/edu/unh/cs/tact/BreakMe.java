// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

class BreakMe{
	public int someField;

	public static void codeToBreak(){
		BreakMe bm = new BreakMe();
		bm.someField = 666;
	}
}
