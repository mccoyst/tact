// Time-stamp: <28 Feb 2008 at 10:16:37 by charpov on copland.cs.unh.edu>

package edu.unh.cs.pulses;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Collection;

import java.awt.Graphics2D;
import java.awt.Point;

/** A display of views.  Views can register with a display to be
 * displayed.  Furthermore, the display will forward click events to a
 * view when a view is clicked on.  There are two kinds of displays:
 * <em>passive</em> displays and <em>active</em> displays.  Active
 * displays redraw all their views at a given frame rate; passive
 * displays must have their <code>repaint</code> method called to
 * redraw their views.  This class implements passive displays.
 *
 * @author  Michel Charpentier
 * @version 2.0, 02/27/08
 * @see View
 * @see #repaint()
 * @see <a href="Display.java">Display.java</a>
 */
public class Display extends javax.swing.JComponent {

  private LinkedList<View> views;
  private View[] viewArray;
  private boolean viewArrayNeedsUpdate;

  /** Creates a passive display with the given dimensions.
   * 
   * @param width the desired width of the display, in pixels
   * @param height the desired height of the display, in pixels
   */
  public Display (int width, int height) {
    setPreferredSize(new java.awt.Dimension(width, height));
    views = new LinkedList<View>();
    viewArray = new View[10]; // number doesn't matter
	addMouseListener(new java.awt.event.MouseListener(){
		public void mouseClicked(java.awt.event.MouseEvent e){
			for(View v : viewArray)
				if(v.currentShape().contains(e.getPoint())){
					v.doMouseEvent(e);
					return;
				}
		}
		public void mouseEntered(java.awt.event.MouseEvent e){
		}
		public void mouseExited(java.awt.event.MouseEvent e){
		}
		public void mousePressed(java.awt.event.MouseEvent e){
		}
		public void mouseReleased(java.awt.event.MouseEvent e){
		}
	});
  }

  /** Paints all the views registered with this display.
   */
  protected void paintComponent (java.awt.Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;
    synchronized (this) {
      if (viewArrayNeedsUpdate) {
        viewArray = views.toArray(viewArray);
        viewArrayNeedsUpdate = false;
      }
    }
    for (int i=viewArray.length-1; i>=0; i--) {
      View v = viewArray[i];
      if (v == null)
        continue;
      g2.setColor(v.currentColor());
      g2.fill(v.currentShape());
    }
  }

  /** Registers a view with this display.  The view appears on top of
   * already registered views.  This method is synchronized and can be
   * safely called by threads other than the event handling thread.
   *
   * @param view the view to register
   * @throws IllegalStateException if the view is already registered
   * with this display
   */
  public synchronized void register (View view) { // needs synchro!
    if (views.contains(view))
        throw new IllegalStateException("Unregistered view");
    views.addFirst(view);
    viewArrayNeedsUpdate = true;
  }

  /** Moves a view in front of this display.  The view now appears on top of
   * the other views.  This method is synchronized and can be
   * safely called by threads other than the event handling thread.
   *
   * @param view the view to move in front
   * @throws IllegalStateException if the view is not registered
   * with this display
   */
  public synchronized void moveInFront (View view) { // needs synchro!
    if (!views.remove(view)) // linear search acceptable
      throw new IllegalStateException("Unregistered view");
    views.addFirst(view);
    viewArrayNeedsUpdate = true;
  }

  /** Returns the active/passive status of the display.  This method
   * returns false as this class implements passive displays.
   * 
   * @return false
   */
  public boolean isActive () {
    return false;
  }

  /** Stops the display.  This method has no effect as this class
   * implements passive displays.
   */
  public void stop () {
  }

  /** Starts or restarts the display.  This method has no effect as this class
   * implements passive displays.
   */
  public void start () {
  }
}
