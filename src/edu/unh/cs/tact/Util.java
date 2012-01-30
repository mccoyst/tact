// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

class Util{
	private Util(){}

	/** notNull returns t if it is not null,
	and throws an IllegalArgumentException if it is.
	*/
	public static <T> T notNull(T t, String name){
		if(t == null)
			throw new IllegalArgumentException("\"" + name + "\" must be non-null");

		return t;
	}
}
