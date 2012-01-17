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

		Method[] oldMethods = jc.getMethods();
		Method[] newMethods = new Method[oldMethods.length];

		for(int i = 0; i < oldMethods.length; i++){
			Method m = oldMethods[i];
			Code c = m.getCode();
			if(c == null){
				newMethods[i] = m;
				continue;
			}

			MethodGen mg = new MethodGen(m, jc.getClassName(), f.getConstantPool());

			byte[] bc = c.getCode();
			InstructionList il = new InstructionList(bc);

			for(InstructionHandle h = il.getStart(); h != null; h = h.getNext()){
				if(h.getInstruction() instanceof PUTFIELD)
					il.insert(h,
						f.createInvoke("edu.unh.cs.tact.Checker",
							"check",
							Type.OBJECT,
							new Type[]{ Type.OBJECT },
							Constants.INVOKESTATIC
						));
			}

			il.setPositions();
			mg.setInstructionList(il);
			mg.setMaxStack();
			Method n = mg.getMethod();
			newMethods[i] = n;

			il.dispose(); // Necessary, he sadly said.
			
			System.out.println(n.getCode());
		}

		jc.setMethods(newMethods);
		jc.dump(args[0]);

		System.out.println(jc);
	}
}
