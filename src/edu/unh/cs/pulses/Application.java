// Time-stamp: <28 Feb 2008 at 14:12:18 by charpov on copland.cs.unh.edu>

package edu.unh.cs.pulses;

import javax.swing.JApplet;
import javax.swing.JFrame;

import java.awt.Color;
import java.awt.Shape;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.List;
import java.util.Iterator;

/** A "pulsating thread" application.  This program can be used as an
 * applet or as a standalone application.  As an applet, the various
 * parameters are set using the html syntax:
 *
 * <pre>
 * &lt;param name="foo" value="bar"/&gt;
 * </pre>
 *
 * (See the description of the <code>main</code> method for a list of
 * parameters and default values.)
 *
 * @author  Michel Charpentier
 * @version 2.0, 02/27/08
 * @see Policy
 * @see #main
 * @see <a href="Application.java">Application.java</a>
 */
public class Application extends JApplet {

  private static ShapeFactory colds = ShapeFactory.ringFactory(.6);
  private static ShapeFactory hots = ShapeFactory.sqringFactory(.6);

  private static JFrame frame = new JFrame("Pulses");

  private Policy policy = Policy.getMutexInstance();
  private int nbPulses = 10;
  private int size = 500;
  private double period = 5;
  private double fps = 25;
  private Display display;
  private Pulse[] pulses;
  private boolean activeDisplay;
  private int coldDelay, hotDelay;
  private boolean auto;

  private static final Pattern DELAY = Pattern.compile("(\\d+)\\D+(\\d+)");

  private void setAuto (String s) {
    if (s == null)
      return;
    Matcher m = DELAY.matcher(s);
    if (m.matches()) {
      try {
        int c = Integer.parseInt(m.group(1));
        int h = Integer.parseInt(m.group(2));
        if (c > 0 && h > 0) {
          auto = true;
          coldDelay = c;
          hotDelay = h;
        } else {
          System.err.println
            ("Invalid delays: "+c+","+h+"; using manual pulses");
        }
        return;
      } catch (NumberFormatException e) {
        System.err.println(e);
      }
    }
    System.err.println("Unrecognized delays; using manual pulses");
  }

  private void setSize (String s) {
    if (s == null)
      return;
    try {
      int n = Integer.parseInt(s);
      if (n < 100)
        System.err.println("Invalid size: "+n+"; using "+size);
      else 
        size = n;
    } catch (NumberFormatException e) {
      System.err.println(e);
      System.err.println("Unrecognized size; using "+size);
    }
  }

  private void setNbPulses (String s) {
    if (s == null)
      return;
    try {
      int n = Integer.parseInt(s);
      if (n < 1)
        System.err.println("Invalid number of pulses: "+n+"; using "+nbPulses);
      else 
        nbPulses = n;
    } catch (NumberFormatException e) {
      System.err.println(e);
      System.err.println("Unrecognized number of pulses; using "+nbPulses);
    }
  }

  private static final Pattern POL =
    Pattern.compile("(\\w+)\\s*(\\(\\s*(\\d+(\\s*,\\s*\\d+)*)\\s*\\))?");
  private static final Pattern POLPAR = Pattern.compile("\\d+");

  private void setPolicy (String s) {
    if (s == null)
      return;
    Matcher m = POL.matcher(s);
    if (m.matches()) {
      String name = m.group(1);
      String params = m.group(2);
      int[] p;
      if (params == null) {
        p = new int[0];
      } else {
        List<String> l = new java.util.ArrayList<String>();
        m = POLPAR.matcher(m.group(3));
        while (m.find())
          l.add(m.group(0));
        p = new int[l.size()];
        Iterator<String> iter = l.iterator();
        for (int i=0; i<p.length; i++) {
          String str = iter.next();
          try {
            p[i] = Integer.parseInt(str);
          } catch (NumberFormatException e) {
            System.err.println(e);
            System.err.println
              ("Unrecognized policy parameter: "+str+"; using default policy");
            return;
          }
        }
      }
      Policy pol;
      try {
        pol = Policy.getSomeInstance(name, p);
      } catch (Throwable e) {
        pol = null;
      }
      if (pol != null) {
        policy = pol;
        return;
      }
    }
    System.err.println("Could not find policy '"+s+"'; using default");
  }

  private void setPeriod (String s) {
    if (s == null)
      return;
    try {
      double d = Double.parseDouble(s);
      if (d < 0.5)
        System.err.println("Invalid period: "+d+"; using "+period);
      else 
        period = d;
    } catch (NumberFormatException e) {
      System.err.println(e);
      System.err.println("Unrecognized period; using "+period);
    }
  }

  private void setFPS (String s) {
    if (s == null)
      return;
    activeDisplay = true;
    try {
      double d = Double.parseDouble(s);
      if (d > 200)
        System.err.println("Invalid FPS: "+d+"; using "+fps);
      else 
        fps = d;
    } catch (NumberFormatException e) {
      System.err.println(e);
      System.err.println("Unrecognized fps; using "+fps);
    }
  }

  /** Initializes the applet.  This method starts the pulse threads
   * but not the active display (if any).  It relies on
   * <code>getParameter</code> to change parameters from their default
   * values.  See <code>main</code> for a list of parameters and their
   * default values.
   *
   * @see #getParameter
   * @see #main
   */
  public void init () { // applet specific inits
    setSize(getParameter("size"));
    setNbPulses(getParameter("pulses"));
    setPolicy(getParameter("policy"));
    setPeriod(getParameter("period"));
    setFPS(getParameter("fps"));
    setAuto(getParameter("auto"));
    sharedInit();
  }

  private static final Pattern NONDIGIT = Pattern.compile("\\D");

  private boolean applicationInit (String[] args) {
    for (int i=0; i<args.length; i++) {
      try {
        if (args[i].equals("-size")) {
          setSize(args[++i].trim());
          continue;
        }
        if (args[i].equals("-pulses")) {
          setNbPulses(args[++i].trim());
          continue;
        }
        if (args[i].equals("-policy")) {
          setPolicy(args[++i].trim());
          continue;
        }
        if (args[i].equals("-period")) {
          setPeriod(args[++i].trim());
          continue;
        }
        if (args[i].equals("-fps")) {
          setFPS(args[++i].trim());
          continue;
        }
        if (args[i].equals("-auto")) {
          String s = args[++i].trim();
          if (NONDIGIT.matcher(s).find())
            setAuto(s);
          else
            setAuto(s+","+args[++i]);
          continue;
        }
        if (args[i].equals("-help")) {
          help();
          return false;
        }
        System.err.println("Unknown option: "+args[i]);
      } catch (IndexOutOfBoundsException e) {
        System.err.println("Incomplete option: "+args[i-1]);
        break;
      }
    }
    sharedInit();
    return true;
  }

  private void sharedInit () {
    display = activeDisplay?
      new ActiveDisplay(size,size,fps) : new Display(size, size);
    colds.setSize(size/2);
    colds.setLimits(0,0,size,size);
    getContentPane().add(display);
    pulses = new Pulse[nbPulses];
    for (int i=0; i<pulses.length; i++) {
      final PulseView v = new PulseView(display);
      Shape c = colds.makeRandomShape(50, size/2);
      Shape h = hots.makeShapeLike(c);
      v.setColdShape(c);
      v.setHotShape(h);
      Color color = randomColor();
      v.setColdColor(color);
      v.setHotColor(color);
      final Pulse p = pulses[i] =
        auto? new AutomaticPulse(v,coldDelay,hotDelay) : new Pulse(v);
      p.setPeriod(period*c.getBounds().width/100);      
      p.setPolicy(policy);
      v.setLeftClickHandler(new Runnable(){
        public void run(){
          p.orderChange();
        }
      });
      v.setRightClickHandler(new Runnable(){
        public void run(){
          Color color = randomColor();
          v.setColdColor(color);
          v.setHotColor(color);
        }
      });
      p.setSTS(25);
    }
    for (Pulse p : pulses)
      p.start();
  }

  private static Color randomColor () {
    float r = (float)Math.random();
    float g = (float)Math.random();
    float b = (float)Math.random();
    return new Color(r,g,b);
  }

  /** Starts the applet.  This method starts the active display (if any).*/
  public void start () {
    display.start();
  }

  /** Destroyes the applet.  This method terminates the pulse threads.*/
  public void destroy () {
    for (Pulse p : pulses)
      p.interrupt();
  }

  /** Stops the applet.  This method stops the active display (if any).*/
  public void stop () {
    display.stop();
  }

  /** Public constructor. */
  public Application () {
  }

  private static void p (String s) {
    System.out.println(s);
  }

  private static void help () {
    p("Usage: Application [options]");
    p("where options are:");
    p("-size <size>:                 display size, in pixels");
    p("-pulses <# of pulses>:        number of pulses");
    p("-policy <string>:             policy name (and parameters)");
    p("                              If the policy takes parameters,"+
      " they are specified");
    p("                              as a comma-separated list of"+
      " numbers, between parentheses");
    p("                              This implementation offers the following"+
      " policies:");
    p("                              Vacuous, Mutex, UpperBound(#),"+
      " Range(#,#) and Exact(#)");
    p("-period <time>:               period (in seconds) of a pulse of"+
      " size 100,");
    p("                              smaller pulses are faster, larger"+
      " pulses are slower");
    p("-fps <rate>:                  makes the display an active display and"+
      " specifies the frame rate");
    p("                              This implies the -active option");
    p("-auto <cold time> <hot time>: makes the pulses"+
      " automatic pulses, with the specified average delays");
    p("-help:                        dipslay this message");
  }

  /** Command-line standalone application.  The command-line arguments
   * are as follows:
   *
   * <pre>
   * -size &lt;size&gt;:                 display size, in pixels
   * -pulses &lt;# of pulses&gt;:        number of pulses
   * -policy &lt;string&gt;:             policy name (and parameters)
   *                               If the policy takes parameters, they are specified
   *                               as a comma-separated list of numbers, between parentheses
   *                               This implementation offers the following policies:
   *                               Vacuous, Mutex, UpperBound(#), Range(#,#) and Exact(#)
   * -period &lt;time&gt;:               period (in seconds) of a pulse of size 100
   *                               smaller pulses are faster, larger pulses are slower
   * -fps &lt;rate&gt;:                  makes the display an active display and specifies the frame rate
   * -auto &lt;cold time&gt; &lt;hot time&gt;: makes the pulses automatic pulses, with the specified average delays
   * -help:                        dipslay this message
   * </pre>
   * Parameters have default values as if the program were started as:
   * <pre>
   * Application -size 500 -pulses 10 -period 5 -fps 25 -policy Mutex
   * </pre>
   *
   * See class <code>Policy</code> for a detailed description of each
   * synchronization policy.
   *
   * @param args cmomand-line arguments
   * @see Policy
   */
  public static void main (String[] args) {
    final Application app = new Application();
    if (app.applicationInit(args)) {
      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.addWindowListener(new java.awt.event.WindowAdapter() {
          public void windowClosed (java.awt.event.WindowEvent e) {
            app.stop();
            app.destroy();
          }
        });
      frame.getContentPane().add(app);
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
      app.start();
    }
  }

  /** Applet info.
   * @return the string 
   * <code>"Pulsating Pulses, \u00a9 2008 Michel Charpentier"</code>
   */
  public String getAppletInfo () {
    return "Pulsating Pulses, \u00a9 2008 Michel Charpentier";
  }
}

