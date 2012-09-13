package edu.unh.cs.tact_progs;

public class Main{
	public static void main(String[] args){
		if(args.length == 0){
			System.err.println("Please specify a program.");
			System.exit(1);
		}

		if(args[0].equals("pi-parallel")){
			Pi_Parallel.main(args);
			return;
		}
		if(args[0].equals("pi-parallel-jit")){
			Pi_Parallel_JIT.main(args);
			return;
		}
		if(args[0].equals("test2")){
			test2.main(args);
			return;
		}
	}
}
