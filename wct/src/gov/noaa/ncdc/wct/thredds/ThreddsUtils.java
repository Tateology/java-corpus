package gov.noaa.ncdc.wct.thredds;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvCatalogImpl;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
import ucar.nc2.constants.FeatureType;

public class ThreddsUtils {



    private ArrayList<InvDataset> directoryDatasetArray = new ArrayList<InvDataset>();
    private ArrayList<InvAccess> opendapAccessArray = new ArrayList<InvAccess>();
    private ArrayList<InvAccess> httpAccessArray = new ArrayList<InvAccess>();



    /**
     * By default, not recursive
     * @param catalogURI
     * @throws IOException 
     */
    public void process(String catalogURI) throws IOException {
        process(catalogURI, false);
    }
    /**
     *
     * @param catalogURI
     * @param recursive TODO - not yet implemented
     * @throws IOException 
     */
    public void process(String catalogURI, boolean recursive) throws IOException {

        clearScannedData();
        
        InvCatalogFactory factory = new InvCatalogFactory("default", false);
        InvCatalogImpl catalog = (InvCatalogImpl) factory.readXML( catalogURI);
        List<InvDataset> datasets = catalog.getDatasets();
        if (datasets == null || datasets.size() == 0) {
        	throw new IOException("No THREDDS Datasets found at: "+catalogURI);
        }

        
        
        // TOP LEVEL
        for (int n=0; n<datasets.size(); n++) {
            System.out.println(datasets.get(n));
            InvDataset topDataset = (InvDataset)datasets.get(n);
//            processDataset(topDataset, recursive, false);
            parseChildren(topDataset, recursive);
        }
    }

    public void processDataset(InvDataset dataset, boolean recursive) throws MalformedURLException {
        processDataset(dataset, recursive, true);
    }
    
    private void processDataset(InvDataset dataset, boolean recursive, boolean clear) throws MalformedURLException {

        if (clear) {
            clearScannedData();
        }
        

//        System.out.println("PROCESSING DATASET: "+dataset);
        List<InvDataset> nestedDatasets = dataset.getDatasets();
        for (int i=0; i<nestedDatasets.size(); i++) {
            parseChildren((InvDataset)nestedDatasets.get(i), recursive);
        }

    }
    
    
    
    private void parseChildren(InvDataset nestedDataset, boolean recursive) throws MalformedURLException {
        

        if (nestedDataset.hasNestedDatasets() && recursive) {
            processDataset(nestedDataset, recursive);
        }
        else {
            
            if (nestedDataset.hasNestedDatasets()) {
//                if (nestedDataset.getDatasets() != null && nestedDataset.getDatasets().size() > 0) {
                    
//                    System.out.println(nestedDataset.getDatasets());
                    
                    directoryDatasetArray.add(nestedDataset);
//                    continue;
//                }
            }
            
//            System.out.println("nested: "+nestedDataset);
//            System.out.println("nestedURL: "+nestedDataset.getCatalogUrl());

//            List<InvAccess> accessTypes = nestedDataset.getAccess();
//            System.out.println("accessType: "+accessTypes.toString());
//            System.out.println("dataType: "+nestedDataset.getDataType());

            InvAccess access = nestedDataset.getAccess(ServiceType.OPENDAP);
//            if (access != null && nestedDataset.getDataType() == FeatureType.GRID) {
            if (access != null && 
                    nestedDataset.getDataType() != FeatureType.RADIAL &&
                    nestedDataset.getDataType() != FeatureType.STATION_RADIAL &&
                    nestedDataset.getDataType() != FeatureType.ANY_POINT &&
                    nestedDataset.getDataType() != FeatureType.PROFILE &&
                    nestedDataset.getDataType() != FeatureType.SWATH
                    ) {
//            if (access != null) {
                opendapAccessArray.add(access);
            }


            access = nestedDataset.getAccess(ServiceType.HTTPServer);
            if (access != null) {
                httpAccessArray.add(access);
            }
            //System.out.println("access - getUrlPath: "+access.getUrlPath());
            //System.out.println("access - getStandardUrlName: "+access.getStandardUrlName()); // this will work!

        }

    }
    
    private void clearScannedData() {
        directoryDatasetArray.clear();
        opendapAccessArray.clear();
        httpAccessArray.clear();
    }
    
    
    public ArrayList<InvAccess> getHttpAccessArray() {
        return httpAccessArray;
    }
    public ArrayList<InvDataset> getDirectoryDatasetArray() {
        return directoryDatasetArray;
    }
    public ArrayList<InvAccess> getOpendapAccessArray() {
        return opendapAccessArray;
    }

    
    
    public class ThreddsEntry {
        private URL accessURL;
        private String name;
        
        public ThreddsEntry(URL accessURL, String name) {
            this.accessURL = accessURL;
            this.name = name;
        }
        
        public void setAccessURL(URL accessURL) {
            this.accessURL = accessURL;
        }
        public URL getAccessURL() {
            return accessURL;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return this.accessURL + " (" + this.name + ")";
        }
    }

}

