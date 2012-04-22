// Time-stamp: <28 Feb 2008 at 10:19:32 by charpov on copland.cs.unh.edu>

package edu.unh.cs.pulses;

/** A displayable view.  This is a graphical object with a shape and
 * color.  The shape defines the size and location of the object.  A
 * view can be requested to process mouse events when it is clicked
 * on.
 *
 * @author  Michel Charpentier
 * @version 2.0, 02/27/08
 * @see Display
 * @see <a href="View.java">View.java</a>
 */
public interface View {

  /** The current shape of the view.  This includes the size and
   * location of the view.
   * @return the current shape of the view
   */
  public java.awt.Shape currentShape ();

  /** The current color of the view.
   *
   * @return the current color of the view.
   */
  public java.awt.Color currentColor ();

  /** Requests that the view process a mouse event. 
   *
   * @param e the mouse event to process
   */
  public void doMouseEvent (java.awt.event.MouseEvent e);
}

