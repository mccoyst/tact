// Copyright © 2012 Steve McCoy under the MIT license.

package edu.unh.cs.pulses;

import java.util.Set;
import java.util.HashSet;

/** A policy on how pulses transition from cold to hot and hot to cold. This class offers several static methods to build different policies.
* @author Steve McCoy
*/
class Policy{
	/** The set of pulses currently registered with this policy. This set is synchronized and can be modified by different threads concurrently (but caution must be exercised when using iterators; see synchronizedSet for details).
	* @see java.util.Collections#synchronizedSet(java.util.Set)
	*/
	protected Set<Pulse> registered;

	/** Cleans up dead pulses every two minutes. */
	private java.util.Timer cleaner;

	/** Builds a new policy. This policy nevers prevents a pulse from transitioning from cold to hot or from hot to cold. However, it does throw an exception if a pulse that is not registered with it attempts to use it.
	* @see #getVacuousInstance()
	*/
	protected Policy(){
		registered = java.util.Collections.synchronizedSet(new HashSet<Pulse>());
		cleaner = new java.util.Timer(true);
		cleaner.scheduleAtFixedRate(
			new java.util.TimerTask(){
				public void run(){
					synchronized(Policy.this){
						for(Pulse p : registered)
							if(!p.isAlive()) deRegister(p);
					}
				}
			}, 120000, 120000
		);
	}

	/** Registers a pulse with this policy. Registration allows the pulse to request state changes. Pulses must be in a cold state to register. Pulses should deregister if they stop using this policy. As a safeguard, dead pulses are automatically deregistered every minute.
	* @param p the pulse that is registering
	* @throws IllegalStateException if the pulse is hot or is already registered with this policy
	* @see #deRegister(pulses.Pulse)
	*/
	public void register(Pulse p){
		synchronized(p){
			if(p.isHot())
				throw new IllegalStateException("Hot pulses cannot be registered with this policy");
			if(!registered.add(p))
				throw new IllegalStateException("That pulse is already registered with this policy");
		}
	}

	/** Deregisters a pulse. The pulse must be currently registered with this policy.
	* @throws IllegalStateException if the pulse is not registered with this policy
	*/
	public void deRegister(Pulse p){
		if(!registered.remove(p))
			throw new IllegalStateException("That pulse is not registered with this policy");
	}

	/** Requests a state change. This method should be called by registered pulses to indicate their intent to change state. It is, potentially, a blocking method: the pulse thread will be suspended if the policy does not allow the transition to occur immediately. Policy implementations should throw an InterruptedException if a pulse is interrupted while being suspended by the policy. It is the responsibility of the policy to decide what to do with a hot pulse that requests to become hot or a cold pulse that requests to become cold (the request can be ignored or an IllegalStateException  can be thrown). Policy implementations can use super.setHot to check that the pulse is registered with this policy (this is exactly what the implementation of this method does).
	* @param p the pulse
	* @param hot the desired new stat: true for hot and false for cold
	* @throws IllegalStateException if the pulse is not registered by the policy
	*/
	public void setHot(Pulse p, boolean hot) throws InterruptedException{
		if(!registered.contains(p))
			throw new IllegalStateException("That pulse is not registered with this policy");
	}

	/** A vacuous policy. This policy allows all transitions immediately and its setHot method is never blocking. Requests from cold to cold or hot to hot are ignored.
	* @return a vacuous policy, which allows all transitions immediately
	*/
	public static Policy getVacuousInstance(){
		return new Policy();
	}

	/** A mutual exclusion policy. This policy only allows one hot pulse at most among the registered pulses. A request to become hot will block if there is already a hot pulse registered with this policy. Requests to become cold are never blocking.
	* @return a mutual exclusion policy, which only allows at most one hot pulse at a time
	*/
	public static Policy getMutexInstance(){
		return getRangeInstance(0,1);
	}

	/** An upper bound policy. This policy only allows limit hot pulses at most among the registered pulses. A request to become hot will block if there is already limit hot pulses registered with this policy. Requests to become cold are never blocking. Note that if limit == 1, an upper bound policy is equivalent to a mutual exclusion policy.
	* @param limit the maximum number of pulses hot simultaneously
	* @return an upper bound policy, which only allows at most limit hot pulses at a time
	* @throws IllegalArgumentException if limit < 1
	* @see #getMutexInstance()
	*/
	public static Policy getUpperBoundInstance(int limit){
		if(limit < 1)
			throw new IllegalArgumentException("UpperBound policies require a limit ≥ 1");
		return getRangeInstance(0,limit);
	}

	/** A range policy. This policy only allows a number of hot pulses between lowLimit and highLimit (bounds included). Since pulses start cold, the number of hot pulses may initially be smaller than lowLimit. While this is true, cold pulses can become hot but no hot pulse can become cold again. Once the number of hot pulses reaches the desired range, the policy will ensure that it remains in that range by preventing cold to hot or hot to cold transitions appropriately. The only exception to this rule is the case lowLimit == highLimit, for which this policy behaves exactly like an exact policy (see getExactInstance for details). Note that if lowLimit == 0 and highLimit >  lowLimit, this policy is equivalent to an upper bound policy.
	* @param lowLimit the minimum number of pulses hot simultaneously
	* @param highLimit the maximum number of pulses hot simultaneously
	* @return a range policy, which only allows a number of hot pulses between lowLimit and highLimit (bounds included)
	* @throws IllegalArgumentException if lowLimit < 0 or highLimit < lowLimit
	* @see #getUpperBoundInstance(int)
	* @see #getExactInstance(int)
	*/
	public static Policy getRangeInstance(final int lowLimit, final int highLimit){
		if(lowLimit < 0)
			throw new IllegalArgumentException("Range policies require a lowLimit ≥ 0");
		if(highLimit < lowLimit)
			throw new IllegalArgumentException("Range policies require a highLimit ≥ lowLimit");
		if(lowLimit == highLimit)
			return getExactInstance(lowLimit);

		return new Policy(){
			/** Count of hot pulses. */
			private int hots;
			/** Atomic accessor. */
			private synchronized int hots(){
				return hots;
			}
			/** Mutex for hot transitions. */
			Object hotex = new Object();
			/** Mutex for cold transitions. */
			Object cotex = new Object();

			public void setHot(Pulse p, boolean hot) throws InterruptedException{
				super.setHot(p,hot);
				if(hot) setToHot(p);
				else setToCold(p);
			}
			/** Permit pulse to set to hot under the following conditions:
			* p.isHot(),
			* hots < lowLimit,
			* hots < highLimit
			*/
			private synchronized void setToHot(Pulse p) throws InterruptedException{
				if(p.isHot()) return;
				if(hots < lowLimit || hots < highLimit){
					++hots;
					notifyAll(); // forgetting this added a couple of days to this assignment
					return;
				}
				while(hots() == highLimit) wait();
				++hots;
				notifyAll();
			}
			/** Permit pulse to set to cold under the following conditions:
			* !p.isHot(),
			* hots > lowLimit
			*/
			private synchronized void setToCold(Pulse p) throws InterruptedException{
				if(!p.isHot()) return;
				if(hots > lowLimit){
					--hots;
					notifyAll(); // ditto
					return;
				}
				while(hots() <= lowLimit) wait();
				--hots;
				notifyAll();
			}
		};
	}

	/** An exact policy. This policy only allows a number of hot pulses exactly equal to count. Since pulses start cold, the number of hot pulses is initially smaller than count. While this is true, cold pulses can become hot but no hot pulse can become cold again. Once the number of hot pulses reaches the desired value, the policy will ensure that it remains equal to this value by preventing cold to hot or hot to cold transitions appropriately.

Note that, after the desired value is reached, it takes two pulses to change state at the same time to maintain the number of hot pulses equal to count (one hot that becomes cold and one cold that becomes hot). Since pulses are independent threads and modify their states autonomously, it is not possible to change the state of two pulses in one atomic step. Therefore, this policy allows the number of hot pulses to temporarily fall to count - 1 between the hot to cold transition of one pulse and the cold to hot transition of another. However, the policy allows a hot to cold transition only when there is another pulse already requesting a cold to hot transition. In other words, the number of hot pulses always moves back from count - 1 to count almost immediately. This implementation never allows the number of hot pulses to be larger than count. 
	* @param count the desired exact number of pulses hot simultaneously
	* @return an exact policy, which only allows a number of hot pulses exactly equal to count
	* @throws IllegalArgumentException if count < 1
	*/
	public static Policy getExactInstance(int count){
		if(count < 1)
			throw new IllegalArgumentException("Exact policies require a count ≥ 1");
		throw new UnsupportedOperationException("Couldn't think of a quick/easy way to do this");
	}

	/** Builds a policy by name. This is a convenience method that tries to build a policy from a name, using the other static methods. Specifically, it calls the static method getnameInstance with the supplied int parameters, if such a method can be found. If the name does not correspond to an existing method or corresponds to a method with a different number of parameters, this method returns null
	*/
	public static Policy getSomeInstance(String name, int... params){
		// could use reflection, but there aren't _that_ many methods right now
		if(name.equals("Vacuous"))
			return getVacuousInstance();
		else if(name.equals("Mutex"))
			return getMutexInstance();
		else if(name.equals("UpperBound"))
			return (params.length == 1) ? getUpperBoundInstance(params[0]) : null;
		else if(name.equals("Range"))
			return (params.length == 2) ? getRangeInstance(params[0], params[1]) : null;
		else if(name.equals("Exact"))
			return (params.length == 1) ? getExactInstance(params[0]) : null;
		return null;
	}
}
