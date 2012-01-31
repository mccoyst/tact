// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.generic.*;

import static edu.unh.cs.tact.Util.*;

class BasicBlock{
	public final InstructionFactory f;
	public final InstructionList list;
	public final InstructionHandle begin, end;

	public BasicBlock(InstructionFactory f, InstructionList list,
		InstructionHandle begin, InstructionHandle end){

		this.f = notNull(f, "f");
		this.list = notNull(list, "list");
		this.begin = notNull(begin, "begin");
		this.end = notNull(end, "end");
	}

	public boolean insertChecks(){
		boolean changed = false;
		InstructionHandle end = this.end.getNext();
		for(InstructionHandle h = begin; h != end; h = h.getNext()){
			Instruction code = h.getInstruction();
			if(code instanceof PUTFIELD){
				insertCheck(h.getPrev());
				changed = true;
			}else if(code instanceof NEW){
				insertCheck(h.getNext());
				changed = true;
			}
		}

		return changed;
	}

	private void insertCheck(InstructionHandle h){
		InstructionHandle chk = list.insert(
			h,
			f.createInvoke(
				"edu.unh.cs.tact.Checker",
				"check",
				Type.VOID,
				new Type[]{ Type.OBJECT },
				Constants.INVOKESTATIC
			));
		list.insert(
			chk,
			f.createDup(1) // it's a ref, hope 1 is good enough
		);
	}
}
