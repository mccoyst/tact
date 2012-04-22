// Copyright © 2012 Steve McCoy under the MIT license.

package edu.unh.cs.pulses;

/** A pulse that changes state automatically. Changes are random and based on two values: the average amount of time a pulse remains cold and the average amount of time a pulse remains hot. This implementation relies on a java.util.Timer started as a deamon thread.
* @see java.util.Timer
*/
public class AutomaticPulse extends Pulse{
	private java.util.Timer changer;
	private int coldDel, hotDel;

	/** Task for changing from cold to hot and hot to cold */
	private class TransitionTask extends java.util.TimerTask{
		public void run(){
			orderChange();
		}
	}

	/** Creates an automatic pulse. In the absence of other interaction, this pulse will remain cold coldDelay seconds on average before it tries to become hot, and will remain hot hotDelay on average before it tries to become cold. (Actual cold and hot times may be longer if the pulse has to wait for the transition to be approved by the policy.)
	* @param view the pulse view
	* @param coldDelay the average amount of time in the cold state (in seconds)
	* @param hotDelay the average amount of time in the hot state (in seconds)
	*/
	public AutomaticPulse(PulseView view, int coldDelay, int hotDelay){
		super(view);
		coldDel = coldDelay*1000;
		hotDel = hotDelay*1000;
		changer = new java.util.Timer(true);
		changer.schedule(new TransitionTask(), coldDel);
	}

	/** Requests a state change. If a future automatic request was scheduled, it is as if this request had just happened (i.e., it will not happen again).
	*/
	public synchronized void orderChange(){
		int delay = isHot() ? coldDel : hotDel;
		reschedule(delay);
		super.orderChange();
	}

	/** Completes a state change. The delay before the next change is recomputed, whether the last change occured automatically or not.
	* @see Pulse#orderChange()
	* @see Pulse#isHot()
	*/
	protected synchronized void completeChange(){
		super.completeChange();
		int delay = isHot() ? hotDel : coldDel;
		reschedule(delay);
	}

	/** Resets the timer. This cannot possibly be the only way to get this effect.
	*/
	private void reschedule(int delay){
		changer.cancel();
		changer = new java.util.Timer(true);
		changer.schedule(new TransitionTask(), delay);
	}
}
