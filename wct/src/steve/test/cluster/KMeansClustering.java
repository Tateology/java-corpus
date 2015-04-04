
package steve.test.cluster;

//---------------JCA.java-------------
import java.util.Vector;

public class KMeansClustering {
    
    private Cluster[] clusters;
    private int miter;
    private Vector<DataPoint> mDataPoints = new Vector<DataPoint>();
    private double mSWCSS;

    public KMeansClustering(int k, int iter, Vector<DataPoint> dataPoints) {
        clusters = new Cluster[k];
        for (int i = 0; i < k; i++) {
            clusters[i] = new Cluster("Cluster" + i);
        }
        this.miter = iter;
        this.mDataPoints = dataPoints;
    }

    private void calcSWCSS() {
        double temp = 0;
        for (int i = 0; i < clusters.length; i++) {
            temp = temp + clusters[i].getSumSqr();
        }
        mSWCSS = temp;
    }

    public void startAnalysis() {
        //set Starting centroid positions - Start of Step 1
        setInitialCentroids();
        int n = 0;
        //assign DataPoint to clusters
        loop1: while (true) {
            for (int l = 0; l < clusters.length; l++) 
            {
                clusters[l].addDataPoint(mDataPoints.elementAt(n));
                n++;
                if (n >= mDataPoints.size())
                    break loop1;
            }
        }
        
        //calculate E for all the clusters
        calcSWCSS();
        
        //recalculate Cluster centroids - Start of Step 2
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].getCentroid().calcCentroid();
        }
        
        //recalculate E for all the clusters
        calcSWCSS();

        for (int i = 0; i < miter; i++) {
            //enter the loop for cluster 1
            for (int j = 0; j < clusters.length; j++) {
                for (int k = 0; k < clusters[j].getNumDataPoints(); k++) {
                
                    //pick the first element of the first cluster
                    //get the current Euclidean distance
                    double tempEuDt = clusters[j].getDataPoint(k).getCurrentEuDt();
                    Cluster tempCluster = null;
                    boolean matchFoundFlag = false;
                    
                    //call testEuclidean distance for all clusters
                    for (int l = 0; l < clusters.length; l++) {
                    
                    //if testEuclidean < currentEuclidean then
                        if (tempEuDt > clusters[j].getDataPoint(k).testEuclideanDistance(clusters[l].getCentroid())) {
                            tempEuDt = clusters[j].getDataPoint(k).testEuclideanDistance(clusters[l].getCentroid());
                            tempCluster = clusters[l];
                            matchFoundFlag = true;
                        }
                        //if statement - Check whether the Last EuDt is > Present EuDt 
                        
                        }
//for variable 'l' - Looping between different Clusters for matching a Data Point.
//add DataPoint to the cluster and calcSWCSS

       if (matchFoundFlag) {
        tempCluster.addDataPoint(clusters[j].getDataPoint(k));
        clusters[j].removeDataPoint(clusters[j].getDataPoint(k));
                        for (int m = 0; m < clusters.length; m++) {
                            clusters[m].getCentroid().calcCentroid();
                        }

//for variable 'm' - Recalculating centroids for all Clusters

                        calcSWCSS();
                    }
                    
//if statement - A Data Point is eligible for transfer between Clusters.
                }
                //for variable 'k' - Looping through all Data Points of the current Cluster.
            }//for variable 'j' - Looping through all the Clusters.
        }//for variable 'i' - Number of iterations.
    }

    public Vector<DataPoint>[] getClusterOutput() {
        Vector<DataPoint> v[] = new Vector[clusters.length];
        for (int i = 0; i < clusters.length; i++) {
            v[i] = clusters[i].getDataPoints();
        }
        return v;
    }


    private void setInitialCentroids() {
        //kn = (round((max-min)/k)*n)+min where n is from 0 to (k-1).
        double cx = 0, cy = 0;
        for (int n = 1; n <= clusters.length; n++) {
            cx = (((getMaxXValue() - getMinXValue()) / (clusters.length + 1)) * n) + getMinXValue();
            cy = (((getMaxYValue() - getMinYValue()) / (clusters.length + 1)) * n) + getMinYValue();
            Centroid c1 = new Centroid(cx, cy);
            clusters[n - 1].setCentroid(c1);
            c1.setCluster(clusters[n - 1]);
        }
    }

    private double getMaxXValue() {
        double temp;
        temp = mDataPoints.elementAt(0).getX();
        for (int i = 0; i < mDataPoints.size(); i++) {
            DataPoint dp = mDataPoints.elementAt(i);
            temp = (dp.getX() > temp) ? dp.getX() : temp;
        }
        return temp;
    }

    private double getMinXValue() {
        double temp = 0;
        temp = mDataPoints.elementAt(0).getX();
        for (int i = 0; i < mDataPoints.size(); i++) {
            DataPoint dp = mDataPoints.elementAt(i);
            temp = (dp.getX() < temp) ? dp.getX() : temp;
        }
        return temp;
    }

    private double getMaxYValue() {
        double temp = 0;
        temp = mDataPoints.elementAt(0).getY();
        for (int i = 0; i < mDataPoints.size(); i++) {
            DataPoint dp = mDataPoints.elementAt(i);
            temp = (dp.getY() > temp) ? dp.getY() : temp;
        }
        return temp;
    }

    private double getMinYValue() {
        double temp = 0;
        temp = mDataPoints.elementAt(0).getY();
        for (int i = 0; i < mDataPoints.size(); i++) {
            DataPoint dp = mDataPoints.elementAt(i);
            temp = (dp.getY() < temp) ? dp.getY() : temp;
        }
        return temp;
    }

    public int getKValue() {
        return clusters.length;
    }

    public int getIterations() {
        return miter;
    }

    public int getTotalDataPoints() {
        return mDataPoints.size();
    }

    public double getSWCSS() {
        return mSWCSS;
    }

    public Cluster getCluster(int pos) {
        return clusters[pos];
    }
}

