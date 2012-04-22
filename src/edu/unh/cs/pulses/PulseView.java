// Time-stamp: <28 Feb 2008 at 13:12:22 by charpov on copland.cs.unh.edu>

package edu.unh.cs.pulses;

import java.awt.Shape;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;
import java.awt.event.MouseEvent;

/** A pulse view.  This is a passive graphical object that is driven
 * by an active pulse thread.  From it, one can retrieve the current
 * shape, size and color of the view, which depend on the current
 * state and size of the pulse driving this view.  A pulse view also
 * handles mouse events by having desired action registered for a
 * left, middle or right click of the mouse.
 *
 * @author  Michel Charpentier
 * @version 2.0, 02/27/08
 * @see Pulse
 * @see Display
 * @see <a href="PulseView.java">PulseView.java</a>
 */
public class PulseView implements View {

  private Shape hotShape, coldShape;
  private Rectangle bounds;
  private float phase;
  private Color hotColor, coldColor;
  private boolean isHot;
  private final Display display;
  private final AffineTransform trans;
  private Runnable lefter, midder, righter;

  /** Process a mouse event. This method is called (usually by the
   * mouse event handling thread) to request this view to process a
   * mouse-click event. Pulse views only handle simple clicks, which
   * trigger the corresponding mouse-click handler.
   * @param e the mouse event
   * @see #setLeftClickHandler(java.lang.Runnable)
   * @see #setMiddleClickHandler(java.lang.Runnable)
   * @see #setMiddleClickHandler(java.lang.Runnable)
   */
   public void doMouseEvent(MouseEvent e){
     switch(e.getButton()){
	 case MouseEvent.BUTTON1: if(lefter != null) lefter.run(); break;
	 case MouseEvent.BUTTON2: if(midder != null) midder.run(); break;
	 case MouseEvent.BUTTON3: if(righter != null) righter.run(); break;
	 default: break;
	 }
   }

  /** Sets the left-click handler. The handler will be run (usually by the event handling thread) when there is a left click on the view. The new handler replaces the old handler, if any (i.e., handlers are not chained). Setting the handler to null is valid and in effect removes the current handler without replacing it with a new one.
  * @param r the new left-click handler
  */
  public void setLeftClickHandler(Runnable r){
    lefter = r;
  }

  /** Sets the middle-click handler. The handler will be run (usually by the event handling thread) when there is a middle click on the view. The new handler replaces the old handler, if any (i.e., handlers are not chained). Setting the handler to null is valid and in effect removes the current handler without replacing it with a new one.
  * @param r the new middle-click handler
  */
  public void setMiddleClickHandler(Runnable r){
    midder = r;
  }

  /** Sets the right-click handler. The handler will be run (usually by the event handling thread) when there is a right click on the view. The new handler replaces the old handler, if any (i.e., handlers are not chained). Setting the handler to null is valid and in effect removes the current handler without replacing it with a new one.
  * @param r the new right-click handler
  */
  public void setRightClickHandler(Runnable r){
    righter = r;
  }

  /** Build a pulse view and registers it with the given display.  By
   * default, the view's cold and hot shapes are empty, its cold color
   * is blue and its hot color is red.  No handlers are registered and
   * mouse events have no effect.
   *
   * @param d the display
   * @see Color#BLUE
   * @see Color#RED
   */
  public PulseView (Display d) {
    if (d == null)
      throw new NullPointerException("display cannot be null");
    display = d;
    display.register(this);
    Area empty = new Area();
    hotShape = empty;
    coldShape = empty;
    coldColor = Color.BLUE;
    hotColor = Color.RED;
    setBounds();
    trans = new AffineTransform();
  }

  /** Sets this pulse view to cold or hot.  This usually changes the
   * shape and/or color of the view (and possibly its size as well).
   * If the view is connected to a passive display, it sends a request
   * to be repainted.  This method is usually called by the pulse
   * thread that is driving this view.
   *
   * @param hot true to set the view to hot, false to set it to cold
   * @see Display#isActive
   */
  public synchronized void setHot (boolean hot) {
    isHot = hot;
    if (!display.isActive())
      display.repaint(bounds);
  }

  /** Sets the size of this pulse view as a phase.  <code>Pi/2</code>
   * gives the view its maximum size while <code>Pi</code> and
   * <code>0</code> gives it a zero size.  If the view changes from
   * shrinking to growing, it sends to its display a request to be
   * drawn in front of the other views.  Furthermore, if the display
   * is passive, the view sends a request to be repainted. This method
   * is usually called by the pulse thread that is driving this view.
   * 
   * @param phi the desired size of the view, as a phase of its maximal size
   */
  public synchronized void setPhase (double phi) {
    if (phi < phase)
      display.moveInFront(this);
    phase = (float)phi;
    if (!display.isActive())
      display.repaint(bounds);
  }

  private void setBounds () {
    bounds = coldShape.getBounds();
    bounds.add(hotShape.getBounds());
  }

  /** Sets the cold color of this pulse view.  By default, the cold
   * color is initially blue.  Colors can be changed an any time
   * (e.g., while a pulse is running).
   * 
   * @param c the new cold color for this view
   */
  public synchronized void setColdColor (Color c) {
    if (c == null)
      throw new NullPointerException("Color cannot be null");
    coldColor = c;
  }

  /** Sets the hot color of this pulse view.  By default, the hot
   * color is initially red.  Colors can be changed an any time
   * (e.g., while a pulse is running).
   * 
   * @param c the new hot color for this view
   */
  public synchronized void setHotColor (Color c) {
    if (c == null)
      throw new NullPointerException("Color cannot be null");
    hotColor = c;
  }

  /** The current color of the view.  This is the cold color if the
   * view is cold, the hot color if it's hot.
   * 
   * @return the current color of the view
   * @see #setColdColor
   * @see #setHotColor
   */
  public synchronized Color currentColor () {
    return isHot? hotColor : coldColor;
  }

  /** Sets the cold shape of this pulse view.  By default, the cold
   * shape is initially empty.  Shapes can be changed an any time
   * (e.g., while a pulse is running).
   * 
   * @param s the new cold shape for this view
   */
  public synchronized void setColdShape (Shape s) {
    if (s == null)
      throw new NullPointerException("Shape cannot be null");
    coldShape = s;
    setBounds();
  }

  /** Sets the hot shape of this pulse view.  By default, the hot
   * shape is initially empty.  Shapes can be changed an any time
   * (e.g., while a pulse is running).
   * 
   * @param s the new hot shape for this view
   */
  public synchronized void setHotShape (Shape s) {
    if (s == null)
      throw new NullPointerException("Shape cannot be null");
    hotShape = s;
    setBounds();
  }

  /** The current shape of the view.  This is the cold shape if the
   * view is cold, the hot shape if it's hot.  The size of the shape
   * depends on the current phase value of the view.
   * 
   * @return the current shape of the view
   * @see #setColdShape
   * @see #setHotShape
   * @see #setPhase
   */
  public synchronized Shape currentShape () {
    Shape base = isHot? hotShape : coldShape;
    Rectangle r = base.getBounds();
    double scale = Math.abs(Math.sin(phase));
    double t = (1 - scale);
    trans.setToTranslation(t * (r.x + r.width/2.0), t * (r.y + r.height/2.0));
    trans.scale(scale, scale);
    return trans.createTransformedShape(base);
  }
}
