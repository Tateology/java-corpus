
package steve.test.cluster;

/*-----------------PrgMain.java---------------*/

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: shyam.s
 * Date: Apr 18, 2004
 * Time: 4:26:06 PM
 */
public class KMeansClusteringTest {
    
    
    public static void main (String args[]){
        Vector<DataPoint> dataPoints = new Vector<DataPoint>();
        dataPoints.add(new DataPoint(22,21,"p53"));
        dataPoints.add(new DataPoint(19,20,"bcl2"));
        dataPoints.add(new DataPoint(18,22,"fas"));
        dataPoints.add(new DataPoint(1,3,"amylase"));
        dataPoints.add(new DataPoint(3,2,"maltase"));

        KMeansClustering jca = new KMeansClustering(2,1000,dataPoints);
        jca.startAnalysis();

        Vector<DataPoint>[] v = jca.getClusterOutput();
        for (int i=0; i<v.length; i++){
            System.out.println("-----------Cluster"+i+"---------");
            Cluster cluster = jca.getCluster(i);
            System.out.println(cluster);
            Vector<DataPoint> tempV = v[i];
            Iterator<DataPoint> iter = tempV.iterator();
            while(iter.hasNext()){
                DataPoint dpTemp = (DataPoint)iter.next();
                System.out.println(dpTemp.getObjName()+"["+dpTemp.getX()+","+dpTemp.getY()+"]");
            }
        }
    }
} 