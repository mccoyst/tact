// Time-stamp: <27 Feb 2008 at 17:25:00 by charpov on copland.cs.unh.edu>

package edu.unh.cs.pulses;

/** A "pulsating thread".  A pulse is a thread with a graphical
 * representation.  It is pulsating while running and freezes when
 * suspended.  A pulse has two primary states, cold and hot, and a
 * different shape and color for each state so cold pulses can be distinguished
 * from hot pulses.  Pulses rely on a policy object to decide which
 * pulses are allowed to transition from cold to hot or from hot to
 * cold.  When a transition is not immediately allowed by the policy,
 * the pulse thread is suspended.
 *
 * <p>Each pulse is associated with a view, which is responsible for
 * the graphical reprsentation of the pulse (i.e., current size,
 * current color and current shape).  The view also andles mouse
 * events.  To keep the mechanism simple, each mouse button is
 * associated with at most one handler.  These handlers are created as
 * runnable objects and connected to the view using the
 * <code>setClickHandler</code> methods.
 *
 * <p>Pulses offer a public method <code>orderChange</code> to request
 * that the pulse changes from cold to hot or from hot to cold.  This
 * method is thread safe.  In particular, it can be called from a
 * timer thread (to make pulses change automatically) or from a mouse
 * event handler (to use mouse clicks to request changes).
 *
 * @author  Michel Charpentier
 * @version 2.0, 02/27/08
 * @see Policy
 * @see PulseView
 * @see <a href="Pulse.java">Pulse.java</a>
 */
public class Pulse extends Thread {

  private static final int STATES_PER_SECOND = 20;

  private final PulseView view;
  private float sts;
  private Policy policy;
  private boolean isHot, mustChange; // false initially
  private float period, phi;

  /** Public constructor.  This creates a new pulse with the
   * associated view.  By default, this pulse uses a vacuous policy,
   * changes size 20 times per second and has a period of 1 second.
   *
   * @param view the associated pulse view
   * @see Policy#getVacuousInstance
   */
  public Pulse (PulseView view) {
    if (view == null)
      throw new NullPointerException("view cannot be null");
    this.view = view;
    this.sts = STATES_PER_SECOND;
    period = 1;
    phi = (float)Math.PI / 2;
    view.setPhase(phi);
    policy = Policy.getVacuousInstance();
    policy.register(this);
  }

  /** The pulse type.  By overriding this method, pulses can have
   * different types so they are handled differently by various
   * policies.  This method returns 0.
   *
   * @return 0
   */
  public int getType () {
    return 0;
  }

  /** Sets the pulse policy.  This method registers the pulse with the
   * new policy and unregisters it from the old policy.  Once
   * the pulse has started, the policy cannot be changed.
   *
   * @param policy the policy
   * @throws IllegalStateException if the pulse is currently running
   */
  public void setPolicy (Policy policy) {
    if (isAlive())
      throw new IllegalStateException("cannot change policy on active pulses");
    if (policy == null)
      throw new IllegalArgumentException("policy cannot be null");
    this.policy.deRegister(this);
    this.policy = policy;
    this.policy.register(this);
  }

  /** Sets the number of times a pulses changes size per second.  Once
   * the pulse has started, this parameter cannot be changed.
   *
   * @param sts the number of times the size of the pulsating thread
   * changes per second
   * @throws IllegalStateException if the pulse is currently running
   * @throws IllegalArgumentException if the size change rate is
   * smaller than 1 or larger than 1000
   */
  public void setSTS (double sts) {
    if (isAlive())
      throw new IllegalStateException("cannot change policy on active pulses");
    if (sts > 1000 || sts < 1)
      throw new IllegalArgumentException("size change rate is too high");
    this.sts = (float)sts;
  }

  /** Current state of the pulse.
   *
   * @return true if the pulse is hot, false if it is cold
   */
  public synchronized boolean isHot () {
    return isHot;
  }

  /** Current pulse period.
   *
   * @return pulse period
   */
  public synchronized float getPeriod () {
    return period;
  }

  /** Order a transition from cold to hot or hot to cold.  This method
   * sets a flag to indicate that the pulse should contact its policy
   * and request a state change.  It is synchronized and can safely be
   * called from timer threads and mouse event handlers.
   */
  public synchronized void orderChange () {
    mustChange = true;
  }

  private synchronized boolean mustChange () {
    return mustChange;
  }

  /** Completes a transition from cold to hot or hot to cold.  This
   * method is called by the pulse itself after it sucessfully
   * completes a transition with its policy.  It changes the internal
   * state of the pulse from cold to hot or hot to cold and resets the
   * flag that indicated that a transition was requested.  This method
   * can be overridden to add hooks if there are actions a pulse must
   * accomplish at the end of a transition.  It is important, however,
   * that <em>there is always a call to
   * <code>super.completeChange</code></em> so the internal state of
   * the pulse remains consistent.
   *
   * @see #orderChange
   * @see #isHot
   */
  protected synchronized void completeChange () {
    isHot = !isHot;
    mustChange = false;
  }

  /** Sets the pulse period.  The new period must be positive.  Note
   * that very low periods may result in "invisible pulses" (pulses
   * that change much faster than the rate of the display).  This
   * method is synchronized and the period can safely be changed while
   * the pulse is running.
   * 
   * @param period the new period
   */
  public synchronized void setPeriod (double period) {
    if (period <= 0)
      throw new IllegalArgumentException("Period must be positive");
    this.period = (float)period;
  }

  /** The pulse behavior.  The behavior is an infinite loop, in which
   * the pulse repeatedly changes its size and updates its pulse view.
   * How often the size changes is specified in the pulse constructor.
   * This loop can be interrupted with the
   * <code>Thread.interrupt</code> method.  This is the way a pulse
   * should be terminated.
   *
   * @see Thread#interrupt
   * @see PulseView
   */ 
  public void run () {
    long delay = Math.round(1000 / sts);
    while (!isInterrupted()) {
      try {
        if (mustChange()) {
          // not atomic, but no other thread can reset mustChange to false
          policy.setHot(this, !isHot);
          completeChange();
          view.setHot(isHot);
        }
        phi += (float)(Math.PI / (sts * period));
        if (phi > Math.PI)
          phi = 0;
        view.setPhase(phi);
        sleep(delay);
      } catch (InterruptedException e) {
        break;
      }
    }
    policy.deRegister(this);
  }

  /** A string representation of the pulse.  It includes the pulse
   * state (hot/cold) and how big the pulse currently is (as a
   * percentage of its maximum size).  This method is intended for
   * debugging purposes.
   *
   * @return a string representation of the pulse
   */
  public synchronized String toString () {
    StringBuilder b = new StringBuilder(getName());
    if (isHot)
      b.append(": hot ");
    else
      b.append(": cold ");
    b.append(Math.round(phi * 100 / Math.PI));
    b.append("%");
    return b.toString();
  }
}


