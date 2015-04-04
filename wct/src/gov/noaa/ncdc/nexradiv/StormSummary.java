/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 * 
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE 
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.  
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package gov.noaa.ncdc.nexradiv;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryImpl;
import org.geotools.filter.GeometryDistanceFilter;
import org.geotools.map.DefaultMapLayer;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;



public class StormSummary {
   

   
   public static FeatureCollection getIntersectingFeatures(
      DefaultMapLayer nexradLayer, 
      DefaultMapLayer queryLayer) {
         
      FilterFactory ff = new FilterFactoryImpl();
      try {
         // Create Filter
         //GeometryFilter filt = ff.createGeometryFilter(GeometryFilter.GEOMETRY_INTERSECTS);         
			GeometryDistanceFilter filt = ff.createGeometryDistanceFilter(Filter.GEOMETRY_DWITHIN);
			filt.addRightGeometry(
				ff.createAttributeExpression(
					nexradLayer.getFeatureSource().getSchema(), 
					nexradLayer.getFeatureSource().getSchema().getDefaultGeometry().getName()
            )
         );

			//filt.setDistance(5/nexview.getMapPane().getScaleFactor());
         filt.setDistance(0.0001);
         
			filt.addLeftGeometry(
				ff.createAttributeExpression(
					queryLayer.getFeatureSource().getSchema(), 
					queryLayer.getFeatureSource().getSchema().getDefaultGeometry().getName()
            )
         );
         
         // Get Intersecting Features
			FeatureSource fs = queryLayer.getFeatureSource();	
			FeatureResults fr = fs.getFeatures(filt);
         
			if (fr.getCount() == 0) {
System.out.println("NO INTERSECTING FEATURES FOUNDS");            
         }
         else {
System.out.println(fr.getCount()+" FEATURES FOUNDS!!!");            
         }
         
         
         return fr.collection();
         
         
         
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
      }      
      
      return null;
   }

   
   
   
   
   
   
   
   
   
   
   public static FeatureCollection getIntersectingFeatures(
      FeatureCollection nxFeatures, 
      DefaultMapLayer queryLayer) {
         
      return getIntersectingFeatures(null, nxFeatures, queryLayer);         
   }
   
   
   public static FeatureCollection getIntersectingFeatures(
      java.awt.geom.Rectangle2D.Double currentExtent,
      FeatureCollection nxFeatures, 
      DefaultMapLayer queryLayer) {
         
      try {
         
         FeatureCollection returnFeatures = FeatureCollections.newCollection();
         
         // Get Intersecting Features
			FeatureSource fs = queryLayer.getFeatureSource();	
			FeatureResults fr = fs.getFeatures();
         FeatureReader dataReader = fr.reader();
         
         // Get bounds of NEXRAD features
         GeometryFactory geoFactory = new GeometryFactory();
         Geometry nxBounds = geoFactory.toGeometry(nxFeatures.getBounds());
         
         Geometry viewBounds;
         if (currentExtent != null) {
            viewBounds = geoFactory.toGeometry(new Envelope(currentExtent.x, currentExtent.y, 
               currentExtent.x + currentExtent.width, currentExtent.y + currentExtent.height));
         }
         else {
            viewBounds = nxBounds;
         }
         
         // Compare NEXRAD data to highways
         int cnt = 0;
         while (dataReader.hasNext()) {            
            Feature dataFeature = dataReader.next();
            Geometry dataGeometry = dataFeature.getDefaultGeometry();
            
            // do initial check to make sure input features are within bounds of NEXRAD site
            if (dataGeometry.within(viewBounds) && dataGeometry.within(nxBounds)) {
            
               FeatureIterator fci = nxFeatures.features();
               while (fci.hasNext()) {
                  Feature nxFeature = fci.next();
                  Geometry nxGeometry = nxFeature.getDefaultGeometry();

                  if (nxGeometry.within(viewBounds)) {
                     if (dataGeometry.intersects(nxGeometry)) {
                        returnFeatures.add(dataFeature);
                     }
                  }
               }
            }

         }
         
         
         System.out.println(returnFeatures.size()+" FEATURES FOUNDS!!!");            
         
         
         return returnFeatures;
         
         
         
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
      }      
      
      return null;
   }
   



   
   
   
   
   
   public static FeatureCollection getIntersectingJoinFeatures(
      FeatureCollection nxFeatures, 
      DefaultMapLayer queryLayer) {
         
      return getIntersectingJoinFeatures(null, nxFeatures, queryLayer, null);         
   }
   
   
   public static FeatureCollection getIntersectingJoinFeatures(
      java.awt.geom.Rectangle2D.Double currentExtent,
      FeatureCollection nxFeatures, 
      DefaultMapLayer queryLayer) {
         
      return getIntersectingJoinFeatures(null, nxFeatures, queryLayer, null);         
   }
   
   public static FeatureCollection getIntersectingJoinFeatures(
      java.awt.geom.Rectangle2D.Double currentExtent,
      FeatureCollection nxFeatures, 
      DefaultMapLayer queryLayer,
      javax.swing.BoundedRangeModel rangeModel) {
         
         
      try {
         
         FeatureCollection returnFeatures = FeatureCollections.newCollection();
         
         // Get Intersecting Features
			FeatureSource fs = queryLayer.getFeatureSource();	
			FeatureResults fr = fs.getFeatures();
         FeatureReader dataReader = fr.reader();
         
         // Get bounds of NEXRAD features
         GeometryFactory geoFactory = new GeometryFactory();
         Geometry nxBounds = geoFactory.toGeometry(nxFeatures.getBounds());
         
         Geometry viewBounds;
         if (currentExtent != null) {
            viewBounds = geoFactory.toGeometry(new Envelope(currentExtent.x, currentExtent.y, 
               currentExtent.x + currentExtent.width, currentExtent.y + currentExtent.height));
         }
         else {
            viewBounds = nxBounds;
         }
         
         FeatureType dataType = dataReader.getFeatureType();
         FeatureType nxType = nxFeatures.features().next().getFeatureType();

         AttributeType[] joinAtts = new AttributeType[dataType.getAttributeCount()+1];
         joinAtts[0] = nxType.getAttributeType("value");
         for (int n=0; n<dataType.getAttributeCount(); n++) {
            joinAtts[n+1] = dataType.getAttributeType(n);  
         }

System.out.println("CURREXT: "+currentExtent);               

         //for (int n=0; n<joinAtts.length; n++) {
         //   System.out.println(joinAtts[n]);
         //}


         FeatureType returnType = FeatureTypeFactory.newFeatureType(joinAtts, "Joined Nexrad Attributes");


         
         // set up range model
         if (rangeModel != null) {
            rangeModel.setMaximum(fr.getCount());
         }
         
         
         // Compare NEXRAD data to highways
         int geoIndex = 0;
         int cnt = 0;
         while (dataReader.hasNext()) {

            if (rangeModel != null) {
               rangeModel.setValue(cnt++);
            }
            
            Feature dataFeature = dataReader.next();
            Geometry dataGeometry = dataFeature.getDefaultGeometry();
            
            // do initial check to make sure input features are within bounds of NEXRAD site
            if (dataGeometry.within(viewBounds) && dataGeometry.within(nxBounds)) {
            //if ((dataGeometry.within(viewBounds) || dataGeometry.intersects(viewBounds)) && 
            //   (dataGeometry.within(nxBounds) || dataGeometry.intersects(nxBounds))) {
            
               FeatureIterator fci = nxFeatures.features();
               while (fci.hasNext()) {
                  Feature nxFeature = fci.next();
                  Geometry nxGeometry = nxFeature.getDefaultGeometry();

                  if (nxGeometry.within(viewBounds)) {
                     if (dataGeometry.intersects(nxGeometry)) {
                        
                        Object[] atts = new Object[returnType.getAttributeCount()];
                        atts[0] = nxFeature.getAttribute("value");
                        for (int n=0; n<dataType.getAttributeCount(); n++) {
                           atts[n+1] = dataFeature.getAttribute(n);  
                        }
                        returnFeatures.add(returnType.create( atts, new Integer(geoIndex++).toString() ) );
                     }
                  }
               }
            }

         }
         
         if (rangeModel != null) {
            rangeModel.setValue(0);
         }
         
         System.out.println(returnFeatures.size()+" FEATURES FOUNDS!!!");            
         
         
         return returnFeatures;
         
         
         
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println(e);
      }      
      
      return null;
   }
   

   

}

