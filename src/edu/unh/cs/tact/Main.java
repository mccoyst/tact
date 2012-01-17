// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

class Main{
	int someField;

	public static void main(String[] args) throws Exception{
		Main man = new Main();
		man.someField = 666;

		JavaClass jc = new ClassParser(args[0]).parse();

		ClassGen cg = new ClassGen(jc);
		InstructionFactory f = new InstructionFactory(cg);

		for(Method m : jc.getMethods()){
			Code c = m.getCode();
			if(c == null)
				continue;
			byte[] bc = m.getCode().getCode();
			InstructionList il = new InstructionList(bc);

			for(InstructionHandle h : iter(il)){
				if(h.getInstruction() instanceof PUTFIELD)
					il.insert(h,
						f.createInvoke("edu.unh.cs.tact.Checker",
							"check",
							Type.OBJECT,
							new Type[]{ Type.OBJECT },
							Constants.INVOKESTATIC
						));
			}

			c.setCode(il.getByteCode());
			System.out.println(c);
		}

		System.out.println(jc);
	}

	static Iterable<InstructionHandle> iter(final InstructionList il){
		return new Iterable<InstructionHandle>(){
			@SuppressWarnings("unchecked")
			public Iterator<InstructionHandle> iterator(){
				// Why does this warn?
				// http://commons.apache.org/bcel/apidocs/org/apache/bcel/generic/InstructionList.html#iterator()
				return il.iterator();
			}
		};
	}
}
