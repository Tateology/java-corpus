package gov.noaa.ncdc.wct.unidata.contour.smoothing;
import java.awt.Point;

/**
 * A class to represent a line created from two points
 * @author Derek Springer
 */
public class Line {

    public int x1;
    public int y1;
    public int x2;
    public int y2;

    public Line(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public Line(Point point1, Point point2) {
        this.x1 = point1.x;
        this.y1 = point1.y;
        this.x2 = point2.x;
        this.y2 = point2.y;
    }

    public double slope() {
        if(x2-x1 == 0) return Double.NaN;
        return (double)(y2-y1)/(double)(x2-x1);
    }

    public double intercept() {
        return y1 - slope() * x1;
    }

    public static double slope(double x1, double y1, double x2, double y2) {
        return (y2-y1)/(x2-x1);
    }

    public static double slope(Point point1, Point point2) {
        return slope(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public static double intercept(double x1, double y1, double x2, double y2) {
        return y1 - slope(x1, y1, x2, y2) * x1;
    }

    public static double intercept(Point point1, Point point2) {
        return intercept(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    @Override
    public String toString() {
        return "[(" + x1 + ", " + x2 + "), (" + y1 + ", " + y2 + ")] " +
                "m=" + slope() + ", b=" + intercept();
    }
}