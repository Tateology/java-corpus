package gov.noaa.ncdc.wct.decoders.cdm;


import gov.noaa.ncdc.wct.decoders.DecodeException;
import gov.noaa.ncdc.wct.decoders.DecodeHintNotSupportedException;
import gov.noaa.ncdc.wct.decoders.nexrad.RadarHashtables;
import gov.noaa.ncdc.wct.decoders.sigmet.SigmetDataset;
import gov.noaa.ncdc.wct.decoders.sigmet.SigmetIOServiceProvider;
import gov.noaa.ncdc.wct.ui.RadialProperties;
import gov.noaa.ncdc.wct.ui.event.LoadDataListener;

import java.io.IOException;
import java.util.Formatter;

import javax.swing.JFrame;

import org.junit.Test;

import ucar.nc2.NetcdfFile;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.RadialDatasetSweep;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.DebugFlags;

public class TestRadialPropertiesUI {

    public static void main(String[] args) {
        try {
            new TestRadialPropertiesUI().testLevel2NEXRAD();
            
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DecodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DecodeHintNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    @Test
    public void testLevel2NEXRAD() 
        throws InstantiationException, IOException, DecodeException, DecodeHintNotSupportedException, IllegalAccessException {

        
        
            // Switch use of lines below to switch between NEXRAD and SIGMET data
            String fileIn = "testdata\\KLIX20050829_135451.Z";
//            String fileIn = "C:\\work\\Canadian\\data\\CONVOL.sig";

            NetcdfFile.setDebugFlags(new DebugFlags() {
                public boolean isSet(String flag) {
                    if (flag.equals("NetcdfFile/debugSPI")) {
                        return true;
                    }
                    return false;
                }
                public void set(String arg0, boolean arg1) {
                    // TODO Auto-generated method stub
                    
                }
            });
            
            NetcdfFile.registerIOProvider(SigmetIOServiceProvider.class);
            FeatureDatasetFactoryManager.registerFactory(FeatureType.RADIAL, SigmetDataset.class);

            CancelTask emptyCancelTask = new CancelTask() {
				@Override
                public boolean isCancel() {
                    return false;
                }
				@Override
                public void setError(String arg0) {
                }
				@Override
				public void setProgress(String arg0, int arg1) {					
				}
            };            
            RadialDatasetSweep rds = (RadialDatasetSweep) FeatureDatasetFactoryManager.open(
            		FeatureType.RADIAL, fileIn, emptyCancelTask, new Formatter());


            /* radar information */
            String stationID      = rds.getRadarID();
            String stationName    = rds.getRadarName();
            boolean isVolume       = rds.isVolume();
            
            System.out.println("stationID = "+stationID);
            System.out.println("stationName = "+stationName);
            System.out.println("isVolume = "+isVolume);
            System.out.println("station location = "+rds.getCommonOrigin());

            
            
            RadarHashtables nxhash = RadarHashtables.getSharedInstance();
            System.out.println("nxhash location = "+nxhash.getLat(stationID)+" "+nxhash.getLon(stationID)+" "+nxhash.getElev(stationID));
            
            
            JFrame frame = new JFrame("propsUI test");
            final RadialProperties propsUI = new RadialProperties(frame, rds);
            propsUI.addLoadDataListener(new LoadDataListener() {
                public void loadData() {
                    System.out.println("LOAD: "+propsUI.getRadialPropsPanel().getVariableName()+" : cut="+propsUI.getRadialPropsPanel().getCut()+
                            " , "+propsUI.getRadialPropsPanel().getCutElevation()+" , useRF:"+propsUI.getUseRFvalues()+" classify:"+propsUI.getClassify());                    
                }                
            });
            
            
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            propsUI.setVisible(true);
            
            frame.pack();
            


    }

    
    
    
    
    
    
    
    
}
