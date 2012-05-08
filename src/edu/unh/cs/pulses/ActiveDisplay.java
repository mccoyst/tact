// Copyright © 2012 Steve McCoy under the MIT license.

package edu.unh.cs.pulses;

/** An active display of views. This class is similar to class Display excepts that its instances are active displays, which redraw their registered views at a fixed given rate. Usually, there is no need to call the repaint methods of an active display. This implementation relies on javax.swing.Timer.
* @see Display
* @see javax.swing.Timer
*/
public class ActiveDisplay extends Display{
	private static final long serialVersionUID = 1;

	/** Timer for refreshing the display. */
	private javax.swing.Timer refresher;

	/** Task for refreshing the display. */
	private class RefreshTask implements java.awt.event.ActionListener{
		public void actionPerformed(java.awt.event.ActionEvent e){
			repaint();
		}
	}

	/** Builds an active display. The display has the given dimensions and frame rate. It is initially stopped.
	* @param width the desired width of the display, in pixels
	* @param height the desired height of the display, in pixels
	* @param fps the desired fram rate, in frames per second
	*/
	public ActiveDisplay(int width, int height, double fps){
		super(width, height);
		refresher = new javax.swing.Timer(1, new RefreshTask());
		setFPS(fps);
		start();
	}

	/** Returns the active/passive status of the display. This method returns true as this class implements active displays. Note that this method returns true even when the display is stopped (the name is somewhat misleading, and there is no method to test whether a display is running or not).
	* @return true
	*/
	public boolean isActive(){
		return true;
	}

	/** Sets the frame rate of this display. Frame rates can be changed safely after a display has been created and is already running.
	* @param fps the new frame rate, in frames per second
	* @throws IllegalArgumentException if the rate is larger than 200
	*/
	public void setFPS(double fps){
		if(fps > 200) throw new IllegalArgumentException("FPS must not be greater than 200");
		refresher.setDelay((int)fps/1000);
	}

	/** Stops the display.
	*/
	public void stop(){
		refresher.stop();
	}

	/** Starts or restarts the display.
	*/
	public void start(){
		refresher.start();
	}
}
