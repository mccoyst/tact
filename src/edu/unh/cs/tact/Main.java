// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.apache.bcel.classfile.*;

class Main{
	public static void main(String[] args) throws Exception{
		JavaClass jc = new ClassParser(args[0]).parse();

		System.out.println(jc);
		printCode(jc.getMethods());
	}

	public static void printCode(Method[] methods) {
		for(int i=0; i < methods.length; i++) {
			System.out.println(methods[i]);

			Code code = methods[i].getCode();
			if(code != null)
				System.out.println(code);
		}
	}
}
