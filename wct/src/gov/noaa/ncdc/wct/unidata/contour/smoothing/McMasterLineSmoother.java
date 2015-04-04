package gov.noaa.ncdc.wct.unidata.contour.smoothing;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A method to smooth a hand-drawn line based on the McMaster 
 * line smoothing algorithm
 * 
 * @author Derek Springer
 */
public class McMasterLineSmoother {

	public static final int NEIGHBOR_SIZE = 16;
	
	
    /**
     * @param lineSegments A list of line segments representing a line
     * @return A list line segments representing the smoothed line
     */
    public static List<Line> smoothLine(List<Line> lineSegments) {
        if(lineSegments.size() < NEIGHBOR_SIZE-1) return lineSegments; 

        List<Line> smoothedLine = new ArrayList<Line>();
        List<Point> points = getPoints(lineSegments);
        smoothedLine.add(lineSegments.get(0));
        
        int leftSideRange = NEIGHBOR_SIZE/2;
        int rightSideRange = NEIGHBOR_SIZE/2 + NEIGHBOR_SIZE%2;
        
//        System.out.println(leftSideRange +" *********** "+rightSideRange);

        Point newPoint = points.get(1);
        for(int i = leftSideRange; i < points.size()-rightSideRange-1; i++) {
            Point lastPoint = newPoint;
            newPoint = smoothPoint(points.subList(i-leftSideRange, i+rightSideRange));
            smoothedLine.add(new Line(lastPoint, newPoint));
        }

//        for(int i = leftSideRange; i < points.size()-rightSideRange-1; i++) {
//            Point lastPoint = newPoint;
//            if (i-leftSideRange < 0) {
//            	
//            	for (int n=0; n<leftSideRange+rightSideRange; n++) {
//            		
//            	}
//            }
//            else {
//            	newPoint = smoothPoint(points.subList(i-leftSideRange, i+rightSideRange));
//            }
//            smoothedLine.add(new Line(lastPoint, newPoint));
//        }

        
        Line lastSegment = lineSegments.get(lineSegments.size()-1);
        smoothedLine.add(new Line(
                newPoint, 
                new Point(lastSegment.x1, lastSegment.y1)));
        smoothedLine.add(lastSegment);

        return smoothedLine;
    }

    /**
     * @param lineSegments A list of line segments representing a line
     * @return A list of Points representing the points in a series of 
     *  line segments
     */
    public static List<Point> getPoints(List<Line> lineSegments) {
        List<Point> points = new ArrayList<Point>();
        for(Line segment : lineSegments) {
            points.add(new Point(segment.x1, segment.y1));
        }
        points.add(new Point(
                lineSegments.get(lineSegments.size()-1).x2, 
                lineSegments.get(lineSegments.size()-1).y2));

        return points;
    }

    /**
     * Find the new point for a smoothed line segment 
     * @param points The five points needed
     * @return The new point for the smoothed line segment
     */
    public static Point smoothPoint(List<Point> points) {
        int avgX = 0;
        int avgY = 0;
        for(Point point : points) {
            avgX += point.x;
            avgY += point.y;
        }

        avgX = avgX/points.size();
        avgY = avgY/points.size();
        Point newPoint = new Point(avgX, avgY);
        Point oldPoint = points.get(points.size()/2);
        int newX = (newPoint.x + oldPoint.x)/2;
        int newY = (newPoint.y + oldPoint.y)/2;

        return new Point(newX, newY);
    }
}