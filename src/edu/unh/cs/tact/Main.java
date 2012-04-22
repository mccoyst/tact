// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.io.*;
import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.*;

class Main{
	static boolean loud = false;

	public static void main(String[] args) throws Exception{
		ArrayList<String> classes = new ArrayList<String>();
		for(String arg : args){
			if(arg.equals("-break")){
				BreakMe.codeToBreak();
				return;
			}else if(arg.equals("-work")){
				BreakMe.codeThatWorks();
				return;
			}else if(arg.equals("-pi")){
				BreakMe.pi();
				return;
			}else if(arg.equals("-loud")){
				loud = true;
				continue;
			}
			classes.add(arg);
			preload(arg);
		}

		for(String cf : classes)
			inject(cf);
	}

	private static void preload(String fname) throws Exception{
		Repository.addClass(load(fname));
	}

	private static void inject(String fname) throws Exception{
		JavaClass jc = load(fname);
		if(jc.isInterface())
			return;

		Method[] methods = jc.getMethods();
		ConstantPoolGen cp = new ConstantPoolGen(jc.getConstantPool());
		InstructionFactory insf = new InstructionFactory(cp);

		for(int i = 0; i < methods.length; i++){
			if(methods[i].isNative() || methods[i].isAbstract())
				continue;

			if(loud)
				System.err.printf("Injecting %s.%s\n", jc.getClassName(), methods[i]);

			MethodGen mg = new MethodGen(methods[i], jc.getClassName(), cp);
			boolean changed = new Injector(cp, insf, mg).insertChecks();
			methods[i] = mg.getMethod();

			if(loud && changed){
				System.out.println(methods[i]);
				System.out.println(methods[i].getCode());
			}
		}

		jc.setConstantPool(cp.getFinalConstantPool());
		jc.dump(fname);
	}

	private static JavaClass load(String fname) throws Exception{
		InputStream file = null;
		try{
			file = new BufferedInputStream(new FileInputStream(fname));
			return new ClassParser(file, fname).parse();
		}catch(FileNotFoundException e){
			System.err.printf("I failed to open \"%s\": %s\n",
				fname, e.getLocalizedMessage());
			System.exit(1);
		}finally{
			if(file != null) file.close();
		}
		throw new AssertionError("Shouldn't get here");
	}
}
