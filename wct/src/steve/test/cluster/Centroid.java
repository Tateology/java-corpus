
/*---------------Centroid.java-----------------*/

package steve.test.cluster;

/**
 * This class represents the Centroid for a Cluster. The initial centroid is calculated
 * using a equation which divides the sample space for each dimension into equal parts
 * depending upon the value of k.
 * @author Shyam Sivaraman
 * @version 1.0
 * @see Cluster
 */

class Centroid {
    private double mCx, mCy;
    private Cluster mCluster;

    public Centroid(double cx, double cy) {
        this.mCx = cx;
        this.mCy = cy;
    }

    public void calcCentroid() { //only called by CAInstance
        int numDP = mCluster.getNumDataPoints();
        double tempX = 0, tempY = 0;
        int i;
        //caluclating the new Centroid
        for (i = 0; i < numDP; i++) {
            tempX = tempX + mCluster.getDataPoint(i).getX(); 
            //total for x
            tempY = tempY + mCluster.getDataPoint(i).getY(); 
            //total for y
        }
        this.mCx = tempX / numDP;
        this.mCy = tempY / numDP;
        //calculating the new Euclidean Distance for each Data Point
        tempX = 0;
        tempY = 0;
        for (i = 0; i < numDP; i++) {
            mCluster.getDataPoint(i).calcEuclideanDistance();
        }
        //calculate the new Sum of Squares for the Cluster
        mCluster.calcSumOfSquares();
    }

    public void setCluster(Cluster c) {
        this.mCluster = c;
    }

    public double getCx() {
        return mCx;
    }

    public double getCy() {
        return mCy;
    }

    public Cluster getCluster() {
        return mCluster;
    }

    public String toString() {
        return "("+mCx+" , "+mCy+")";
    }
}
