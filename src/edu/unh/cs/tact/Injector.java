// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import java.util.*;
import org.apache.bcel.*;
import org.apache.bcel.generic.*;

import static edu.unh.cs.tact.Util.*;

class Injector{
	public final ConstantPoolGen cp;
	public final InstructionFactory f;
	public final MethodGen mg;
	public final InstructionList list;
	public final InstructionHandle begin, end;

	public Injector(ConstantPoolGen cp, InstructionFactory f, MethodGen mg){

		this.f = notNull(f, "f");
		this.mg = notNull(mg, "mg");
		this.list = mg.getInstructionList();
		this.begin = list.getStart();
		this.end = list.getEnd();
		this.cp = notNull(cp, "cp");
	}

	public boolean insertChecks(){
		boolean changed = false;
		InstructionHandle end = this.end.getNext();
		for(InstructionHandle h = begin; h != end; h = h.getNext()){
			Instruction code = h.getInstruction();
			if(code instanceof PUTFIELD || code instanceof PUTSTATIC){
				FieldInstruction pf = (FieldInstruction)code;
				int fsize = pf.getType(cp).getSize();
				if(fsize == 2){
					insertCheck64(h);
				}else if(fsize == 1){
					insertCheck32(h);
				}else{
					assert false : "A different size of field???";
				}
				//TODO: insertCheck(h.getPrev()); for trivial assignments
				changed = true;
			}else if(isArrayStore(code)){
				ArrayInstruction pa = (ArrayInstruction)code;
				int esize = pa.getType(cp).getSize();
				if(esize == 2){
					insertArrayCheck64(h);
				}else if(esize == 1){
					insertArrayCheck32(h);
				}else{
					assert false : "A different size of element???";
				}
				changed = true;
			}/*else if(code instanceof NEW){
				insertCheck(h.getNext());
				changed = true;
			}*/
		}

		if(changed)
			mg.setMaxStack();

		return changed;
	}

	private void insertCheck32(InstructionHandle pf){
		list.insert(pf, new SWAP());
		list.insert(pf, f.createDup(1));
		insertCheckCall(pf);
		list.insert(pf, new SWAP());
	}

	private void insertCheck64(InstructionHandle pf){
		list.insert(pf, new DUP2_X1());
		list.insert(pf, new POP2());
		list.insert(pf, new DUP_X2());
		insertCheckCall(pf);
	}

	private boolean isArrayStore(Instruction h){
		return h instanceof AASTORE
			|| h instanceof BASTORE
			|| h instanceof CASTORE
			|| h instanceof DASTORE
			|| h instanceof FASTORE
			|| h instanceof IASTORE
			|| h instanceof LASTORE
			|| h instanceof SASTORE
			;
	}

	private void insertArrayCheck32(InstructionHandle pa){
		list.insert(pa, new DUP2_X1());
		list.insert(pa, new POP2());
		list.insert(pa, new DUP_X2());
		insertCheckCall(pa);
	}

	private void insertArrayCheck64(InstructionHandle pa){
		list.insert(pa, new DUP2_X2());
		list.insert(pa, new POP2());
		list.insert(pa, new DUP2());
		list.insert(pa, new POP());
		insertCheckCall(pa);
		list.insert(pa, new DUP2_X2());
		list.insert(pa, new POP2());
	}

/*
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
*/

	private void insertCheckCall(InstructionHandle h){
		list.insert(
			h,
			f.createInvoke(
				"edu.unh.cs.tact.Checker",
				"check",
				Type.VOID,
				new Type[]{ Type.OBJECT },
				Constants.INVOKESTATIC
			)
		);
	}
}
