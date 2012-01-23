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
					il.insert(
						h.getPrev(), // Insert the check before the previous push
						f.createInvoke("edu.unh.cs.tact.Checker",
							"check",
							Type.OBJECT,
							new Type[]{ Type.OBJECT },
							Constants.INVOKESTATIC
						));
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

		Verifier v = VerifierFactory.getVerifier(njc.getClassName());

		for(int i = 0; i < newMethods.length; i++){
			VerificationResult vr = v.doPass3b(i);
			if(vr.getStatus() != VerificationResult.VERIFIED_OK){
				System.err.printf("Verification failed: %s\n", vr.getMessage());
				System.exit(1);
			}
		}

		njc.dump(args[0]);
	}
}
