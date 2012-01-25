// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.bcel.verifier.*;

class Main{
	public static void main(String[] args) throws Exception{
		BreakMe.codeToBreak();

		JavaClass jc = new ClassParser(args[0]).parse();

		ClassGen cg = new ClassGen(jc);
		InstructionFactory f = new InstructionFactory(cg);

		Method[] oldMethods = jc.getMethods();
		Method[] newMethods = new Method[oldMethods.length];

		for(int i = 0; i < oldMethods.length; i++){
			Method m = oldMethods[i];
			Code c = m.getCode();
			if(c == null){
				newMethods[i] = m;
				continue;
			}

			boolean changed = false;
			MethodGen mg = new MethodGen(m, jc.getClassName(), f.getConstantPool());

			byte[] bc = c.getCode();
			InstructionList il = new InstructionList(bc);

			for(InstructionHandle h = il.getStart(); h != null; h = h.getNext()){
				if(h.getInstruction() instanceof PUTFIELD){
					InstructionHandle ch = il.insert(
						h.getPrev(), // Insert the check before the previous push
						f.createInvoke("edu.unh.cs.tact.Checker",
							"check",
							Type.VOID,
							new Type[]{ Type.OBJECT },
							Constants.INVOKESTATIC
					));
					il.insert(
						ch,
						f.createDup(1) // it's a ref, hope 1 is good enough
					);
					changed = true;
				}
			}

			il.setPositions();
			mg.setInstructionList(il);
			mg.setMaxStack();
			Method n = mg.getMethod();
			newMethods[i] = n;

			il.dispose(); // Necessary, he sadly said.

			if(changed)
				System.out.println(n.getCode());
		}

		cg.setMethods(newMethods);

		JavaClass njc = cg.getJavaClass();
		njc.dump(args[0]);
	}
}
