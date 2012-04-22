// Time-stamp: <28 Feb 2008 at 10:09:10 by charpov on copland.cs.unh.edu>

package edu.unh.cs.pulses;

import java.awt.*;
import java.awt.geom.*;

/** A collection of shapes to be used as pulse views.  Shapes are
 * created using a factory pattern: first, set up a factory for a
 * certain type of shape, then use the factory to create multiple
 * shapes of the same kind with different sizes and locations.  Each
 * factory has a reactangle area and can create shapes that are
 * located randomly within the rectangle area.
 *
 * @author  Michel Charpentier
 * @version 2.0, 02/27/08
 * @see PulseView
 * @see <a href="ShapeFactory.java">ShapeFactory.java</a>
 */
public abstract class ShapeFactory {

  /** The rectangle area of this factory.  Random shapes are created
   * within this area.
   */
  protected int minX, minY, maxX, maxY;

  /** The size of the shapes created by this factory.  It can be
   * changed using <code>setSize</code>.
   *
   * @see #setSize
   */
  protected int size;

  private static final java.util.Random rand = new java.util.Random();

  private static final double SQRT2 = Math.sqrt(2);
  private static final double SQRT3 = Math.sqrt(3);

  /** Protected constructor.  By default, factories have a 100x100
   * area and create shapes of size 100.
   */
  protected ShapeFactory () {
    size = 100;
    minX = 0;
    minY = 0;
    maxX = size;
    maxY = size;
  }

  /** Sets the size of the next shape to be constructed by this factory.
   * 
   * @param size the size of the next shape
   */
  public void setSize (int size) {
    if (size < 0)
      throw new IllegalArgumentException("No negative size");
    this.size = size;
  }

  /** Sets the area of the factory.  Future random shapes will be
   * located in this area.
   */
  public void setLimits (int minX, int minY, int maxX, int maxY) {
    if (minX < 0 || minY < 0 || maxX < minX || maxY < minY)
      throw new IllegalArgumentException
        ("No negative locations or dimensions");
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  /** Builds a shape.  The shape has a size equal to the field
   * <code>size</code> and is centered at the given coordinates.  The
   * shape need not lie within the factory's area.
   * 
   * @see #size
   */
  public abstract Shape makeShape (int xCenter, int yCenter);

  /** Builds a shape.  The shape has the same size and location of the
   * given shape.  The size and area of the factory are not used.
   *
   * @param s the shape to get size and location from
   * @return a new shape with the same size and location as the given shape
   */
  public Shape makeShapeLike (Shape s) {
    int saveSize = size;
    Rectangle r = s.getBounds();
    size = r.width;
    Shape t = makeShape(r.x+r.width/2, r.y+r.height/2);
    size = saveSize;
    return t;
  }

  /** Builds a random shape.  The shape has a random size chosen
   * uniformly between the given bounds and is placed in a random
   * location of the factory's area.  The shape is guaranteed to lie
   * entirely within the factory's area.
   * 
   * @param minSize the smallest possible size of the shape
   * @param maxSize the largest possible size of the shape
   * @return a new shape with random size and location
   * @throws IllegalArgumentException if <code>minSize &gt;
   * maxSize</code> or if <code>maxSize</code> does not fit within the
   * area
   */
  public Shape makeRandomShape (int minSize, int maxSize) {
    if (minSize > maxSize)
      throw new IllegalArgumentException("minSize <= maxSize");
    if (maxSize > maxX - minX || maxSize > maxY - minY)
      throw new IllegalArgumentException("maxSize must be within limits");
    int saveSize = size;
    size = rand.nextInt(maxSize - minSize) + minSize;
    int x = rand.nextInt(maxX - minX - size) + minX + size / 2;
    int y = rand.nextInt(maxY - minY - size) + minY + size / 2;
    Shape s = makeShape(x, y);
    size = saveSize;
    return s;
  }

  /** A factory for plain disks. */
  public static ShapeFactory roundFactory () {
    return new ShapeFactory () {
      public Shape makeShape (int x, int y) {
        return new Ellipse2D.Double(x-size/2,y-size/2,size,size);
      }
    };
  }
  
  /** A factory for plain squares. */
  public static ShapeFactory squareFactory () {
    return new ShapeFactory () {
      public Shape makeShape (int x, int y) {
        return new Rectangle2D.Double(x-size/2,y-size/2,size,size);
      }
    };
  }

  /** A factory for plain equilateral triangles. */
  public static ShapeFactory triangleFactory () {
    return new ShapeFactory () {
      public Shape makeShape (int x, int y) {
        int h = (int)Math.round(size * SQRT3 / 2);
        int[] a = new int[] {x-size/2, x, x+size/2};
        int[] b = new int[] {y+h/3, y-2*h/3, y+h/3};
        Polygon p = new Polygon(a, b, 3);
        return new Area(p);
      }
    };
  }

  /** A factory for rings.  A ring is a circular shape with a certain
   * "thickness".  A thickness close to 0 makes the ring almost a
   * circle; a thickness close to 1 makes the ring almost a disc.
   *
   * @param thick the thickness of the rings produced by this factory
   * @throws IllegalArgumentException if the thickness is not between 0
   * and 1 (bounds excluded)
   */
  public static ShapeFactory ringFactory (final double thick) {
    if (thick <= 0 || thick >= 1)
      throw new IllegalArgumentException("0 < thick < 1");
    return new ShapeFactory () {
      public Shape makeShape (int x, int y) {
        Ellipse2D e1 = new Ellipse2D.Double(x-size/2,y-size/2,size,size);
        double smallSize = size * thick;
        Ellipse2D e2 =
          new Ellipse2D.Double(x-smallSize/2,y-smallSize/2,smallSize,smallSize);
        Area a = new Area(e1);
        a.subtract(new Area(e2));
        return a;
      }
    };
  }

  /** A factory for rings with a square inside.  The square fills up
   * the empty area of the ring.
   *
   * @param thick the thickness of the ring
   */
  public static ShapeFactory sqringFactory (final double thick) {
    if (thick <= 0 || thick >= 1)
      throw new IllegalArgumentException("0 < thick < 1");
    final ShapeFactory r = ringFactory(thick);
    final ShapeFactory s = squareFactory();
    return new ShapeFactory () {
      public Shape makeShape (int x, int y) {
        r.setSize(size);
        s.setSize((int)Math.round(size * thick / SQRT2));
        Area a = new Area(s.makeShape(x,y));
        a.add((Area)r.makeShape(x, y));
        return a;
      }
    };
  }

  /** A factory for rings with a triangle inside.  The triangle fills up
   * the empty area of the ring.
   *
   * @param thick the thickness of the ring
   */
  public static ShapeFactory trringFactory (final double thick) {
    if (thick <= 0 || thick >= 1)
      throw new IllegalArgumentException("0 < thick < 1");
    final ShapeFactory r = ringFactory(thick);
    final ShapeFactory t = triangleFactory();
    return new ShapeFactory () {
      public Shape makeShape (int x, int y) {
        r.setSize(size);
        t.setSize((int)Math.round(size * thick * SQRT3 / 2));
        Area a = new Area(t.makeShape(x,y));
        a.add((Area)r.makeShape(x, y));
        return a;
      }
    };
  }
}