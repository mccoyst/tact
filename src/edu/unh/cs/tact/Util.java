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

	/** rootCause returns the original cause of a Throwable,
	however deep it is. E.g. rootCause(new RuntimeException(new RuntimeException(new IllegalAccessError("bleh"))))
	will return the innermost IllegalAccessError.
	*/
	public static Throwable rootCause(Throwable t){
		while(t.getCause() != null)
			t = t.getCause();
		return t;
	}
}
