package core.game;

import java.io.Serializable;

/**
 * The <code>Rectangle2D</code> class describes a rectangle
 * defined by a location {@code (x,y)} and dimension
 * {@code (w x h)}.
 * <p>
 * This class is only the abstract superclass for all objects that
 * store a 2D rectangle.
 * The actual storage representation of the coordinates is left to
 * the subclass.
 *
 * @author      Jim Graham
 * @since 1.2
 */
public class MyRectangle implements Serializable {

    /**
     * The X coordinate of this <code>Rectangle2D</code>.
     * @since 1.2
     * @serial
     */
    public double x;

    /**
     * The Y coordinate of this <code>Rectangle2D</code>.
     * @since 1.2
     * @serial
     */
    public double y;

    /**
     * The width of this <code>Rectangle2D</code>.
     * @since 1.2
     * @serial
     */
    public double width;

    /**
     * The height of this <code>Rectangle2D</code>.
     * @since 1.2
     * @serial
     */
    public double height;

    /**
     * Constructs a new <code>Rectangle2D</code>, initialized to
     * location (0,&nbsp;0) and size (0,&nbsp;0).
     * @since 1.2
     */
    public MyRectangle() {
    }

    /**
     * Constructs and initializes a <code>Rectangle2D</code>
     * from the specified <code>double</code> coordinates.
     *
     * @param x the X coordinate of the upper-left corner
     *          of the newly constructed <code>Rectangle2D</code>
     * @param y the Y coordinate of the upper-left corner
     *          of the newly constructed <code>Rectangle2D</code>
     * @param w the width of the newly constructed
     *          <code>Rectangle2D</code>
     * @param h the height of the newly constructed
     *          <code>Rectangle2D</code>
     * @since 1.2
     */
    public MyRectangle(double x, double y, double w, double h) {
        setRect(x, y, w, h);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getY() {
        return y;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public double getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean isEmpty() {
        return (width <= 0.0) || (height <= 0.0);
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public void setRect(double x, double y, double w, double h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public void setRect(MyRectangle r) {
        this.x = r.getX();
        this.y = r.getY();
        this.width = r.getWidth();
        this.height = r.getHeight();
    }

    /**
     * Returns the <code>String</code> representation of this
     * <code>Rectangle2D</code>.
     * @return a <code>String</code> representing this
     * <code>Rectangle2D</code>.
     * @since 1.2
     */
    public String toString() {
        return getClass().getName()
            + "[x=" + x +
            ",y=" + y +
            ",w=" + width +
            ",h=" + height + "]";
    }

    /*
     * JDK 1.6 serialVersionUID
     */
    private static final long serialVersionUID = 7771313791441850493L;

    /**
     * {@inheritDoc}
     * @since 1.2
     */
    /*public boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double x0 = getX();
        double y0 = getY();
        return (x + w > x0 &&
                y + h > y0 &&
                x < x0 + getWidth() &&
                y < y0 + getHeight());
    }*/
    
    /**
     * {@inheritDoc}
     * @since 1.2
     */
    public boolean intersects(double x, double y, double w, double h) {
        if (isEmpty() || w <= 0 || h <= 0) {
            return false;
        }
        double l1X = getX();
        double l1Y = getY();
        double r1X = l1X + getWidth();
        double r1Y = l1Y - getHeight();
        
        double l2X = x;
        double l2Y = y;
        double r2X = l2X + w;
        double r2Y = l2Y - h;
        
        // If one rectangle is on left side of other
        if (l1X > r2X || l2X > r1X)
            return false;
     
        // If one rectangle is above other
        if (l1Y < r2Y || l2Y < r1Y)
            return false;
     
        return true;
    }

    public boolean intersects(MyRectangle r) {
        return intersects(r.x, r.y, r.width, r.height);
    }
    
    /**
     * Returns the hashcode for this <code>Rectangle2D</code>.
     * @return the hashcode for this <code>Rectangle2D</code>.
     * @since 1.2
     */
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits += java.lang.Double.doubleToLongBits(getY()) * 37;
        bits += java.lang.Double.doubleToLongBits(getWidth()) * 43;
        bits += java.lang.Double.doubleToLongBits(getHeight()) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    /**
     * Determines whether or not the specified <code>Object</code> is
     * equal to this <code>Rectangle2D</code>.  The specified
     * <code>Object</code> is equal to this <code>Rectangle2D</code>
     * if it is an instance of <code>Rectangle2D</code> and if its
     * location and size are the same as this <code>Rectangle2D</code>.
     * @param obj an <code>Object</code> to be compared with this
     * <code>Rectangle2D</code>.
     * @return     <code>true</code> if <code>obj</code> is an instance
     *                     of <code>Rectangle2D</code> and has
     *                     the same values; <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof MyRectangle) {
        	MyRectangle r2d = (MyRectangle) obj;
            return ((getX() == r2d.getX()) &&
                    (getY() == r2d.getY()) &&
                    (getWidth() == r2d.getWidth()) &&
                    (getHeight() == r2d.getHeight()));
        }
        return false;
    }
}
