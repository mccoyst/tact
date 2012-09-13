/*
	Modified by Scott Cypher
	UNH SURF
	Mentor / Original Creator of File: Prof Hatcher
	07/01/2012

	Pi_Serial.java
		Calculates the value of pi using multiple threads
*/
package edu.unh.cs.tact_progs;

import edu.unh.cs.tact.*;

public class Pi_Parallel extends Thread{

	//class variables
	final static int INTERVALS = 2147483647;
	final static int NUMTHREADS = 8;
	@GuardedBy("this") static double totSum;

	//instance variables
	int id;
	
	public Pi_Parallel(int i){
		id = i;
	}
	
	public void run(){
		int loc_id = id;

		int sectionSize = INTERVALS/NUMTHREADS;
		int lowerBound = loc_id*sectionSize;
		
		int upperBound;

		if( loc_id == (NUMTHREADS - 1)){
			upperBound = INTERVALS;
		}
		else{
			upperBound = lowerBound+sectionSize;
		}		

		double width = 1.0/(double)INTERVALS;
		double x = width*((double)(loc_id*sectionSize) + 0.5);
		double sum = 0.0;

		for(int i = lowerBound; i < upperBound; i++){
			sum += 4.0/(1.0 + x*x);
			x += width;
		}

		sum *= width;

		synchronized(Pi_Parallel.class){
			totSum += sum;
		}
	}

	public static void main(String args[]){
		Checker.init();
		Pi_Parallel[] PiThrArr = new Pi_Parallel[NUMTHREADS];

		for(int i = 0; i < NUMTHREADS; i++){
			Pi_Parallel pi = new Pi_Parallel(i);
			Checker.releaseAndStart(pi);
			PiThrArr[i] = pi;
		}

		for(Pi_Parallel piThread : PiThrArr){
			try{
				piThread.join();
			}
			catch(InterruptedException ie){
				System.out.println("ERROR: thread was interrupted! Terminating program!");
				return;
			}
		}
		synchronized(Pi_Parallel.class){
			System.out.println("Estimation of pi is " + totSum);
		}
	}
}
