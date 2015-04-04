package gov.noaa.ncdc.wct.ui;

import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentDataType;
import gov.noaa.ncdc.wct.ui.WCTViewer.CurrentViewType;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class WctSession implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private OverlayInfo[] overlayInfoArray;		
	private String extentString;
	private String dataLocationType;
	private String dataLocation;
	private String[] wmsName = new String[3];
	
	private int[] selectedDataIndices;
	private String[] dataUrlStringArray;
	
    private boolean autoExtentSelected;
	private CurrentViewType currentViewType;
	private CurrentDataType currentDataType;
	
	private RadialInfo radialInfo;
	private SatelliteInfo satInfo;
	private GridInfo gridInfo;
	private UiInfo uiInfo;
	private LegendInfo legendInfo;
    private FilterInfo filterInfo;
    
	
	public void setOverlayInfoArray(OverlayInfo[] overlayInfoArray) {
		this.overlayInfoArray = overlayInfoArray;
	}

	public OverlayInfo[] getOverlayInfoArray() {
		return overlayInfoArray;
	}	

    /**
     * Returns format of: x,y,width,height
     */
	public Rectangle2D.Double getExtent() {
	    String[] extentArray = extentString.split(",");
	    return new Rectangle2D.Double(
	            Double.parseDouble(extentArray[0]),
	            Double.parseDouble(extentArray[1]),
	            Double.parseDouble(extentArray[2]),
	            Double.parseDouble(extentArray[3]));
	}
	
	/**
	 * Stores format of: x,y,width,height
	 * @param extent
	 */
	public void setExtent(Rectangle2D.Double extent) {
	    this.extentString = extent.x+","+extent.y+","+extent.width+","+extent.height;
	}
	
	public void setDataLocationType(String dataLocationType) {
        this.dataLocationType = dataLocationType;
    }

    public String getDataLocationType() {
        return dataLocationType;
    }

    public void setDataLocation(String dataLocation) {
        this.dataLocation = dataLocation;
    }

    public String getDataLocation() {
        return dataLocation;
    }
    
    public void setWMSLayer(int index, String name) {
        this.wmsName[index] = name;
    }
    
    public String getWMSLayer(int index) {
        return wmsName[index];
    }
    
    public void setSelectedDataIndices(int[] selectedDataIndices) {
        this.selectedDataIndices = selectedDataIndices;
    }
    
    public int[] getSelectedDataIndices() {
        return selectedDataIndices;
    }
    
    public void setSelectedDataURLs(String[] dataUrlStringArray) {
        this.dataUrlStringArray = dataUrlStringArray;
    }
    
    public String[] getSelectedDataURLs() {
        return this.dataUrlStringArray;
    }
    
    public void setAutoExtentSelected(boolean autoExtentSelected) {
        this.autoExtentSelected = autoExtentSelected;
    }

    public boolean isAutoExtentSelected() {
        return autoExtentSelected;
    }



    
    
    public void setCurrentViewType(CurrentViewType currentViewType) {
        this.currentViewType = currentViewType;
    }

    public CurrentViewType getCurrentViewType() {
        return currentViewType;
    }

    public void setCurrentDataType(CurrentDataType currentDataType) {
        this.currentDataType = currentDataType;
    }

    public CurrentDataType getCurrentDataType() {
        return currentDataType;
    }





    public void setRadialInfo(RadialInfo radialInfo) {
        this.radialInfo = radialInfo;
    }

    public RadialInfo getRadialInfo() {
        return radialInfo;
    }

    public void setGridInfo(GridInfo gridInfo) {
        this.gridInfo = gridInfo;
    }

    public GridInfo getGridInfo() {
        return gridInfo;
    }

    public void setSatelliteInfo(SatelliteInfo satInfo) {
        this.satInfo = satInfo;
    }

    public SatelliteInfo getSatelliteInfo() {
        return satInfo;
    }

    public void setUiInfo(UiInfo uiInfo) {
        this.uiInfo = uiInfo;
    }

    public UiInfo getUiInfo() {
        return uiInfo;
    }
    
    public void setFilterInfo(FilterInfo filterInfo) {
		this.filterInfo = filterInfo;
	}

	public FilterInfo getFilterInfo() {
		return filterInfo;
	}






	public static class LegendInfo implements Serializable {
        
        private Color bgColor;
        private Color fgColor;
        private Font font;
        
        public void setBackgroundColor(Color bgColor) {
            this.bgColor = bgColor;
        }
        public Color getBackgroundColor() {
            return bgColor;
        }
        public void setForegroundColor(Color fgColor) {
            this.fgColor = fgColor;
        }
        public Color getForegroundColor() {
            return fgColor;
        }
        public void setFont(Font font) {
            this.font = font;
        }
        public Font getFont() {
            return font;
        }
    }
    

    public static class UiInfo implements Serializable {
        
        private int mainX;
        private int mainY;
        private int mainWidth;
        private int mainHeight;
        
        public void setMainX(int mainX) {
            this.mainX = mainX;
        }
        public int getMainX() {
            return mainX;
        }
        public void setMainY(int mainY) {
            this.mainY = mainY;
        }
        public int getMainY() {
            return mainY;
        }
        public void setMainWidth(int mainWidth) {
            this.mainWidth = mainWidth;
        }
        public int getMainWidth() {
            return mainWidth;
        }
        public void setMainHeight(int mainHeight) {
            this.mainHeight = mainHeight;
        }
        public int getMainHeight() {
            return mainHeight;
        }
        
    }

    public static class RadialInfo implements Serializable {
        
        private int radarTransparency;
        private int smoothingFactor;

        public void setRadarTransparency(int radarTransparency) {
            this.radarTransparency = radarTransparency;
        }

        public int getRadarTransparency() {
            return radarTransparency;
        }

        public int getSmoothingFactor() {
            return smoothingFactor;
        }

        public void setSmoothingFactor(int smoothingFactor) {
            this.smoothingFactor = smoothingFactor;
        }

    }

    public static class SatelliteInfo implements Serializable {
        
        private String satColorTableName;
        private int transparency;
        private String legendType;
        
        public void setSatColorTableName(String satColorTableName) {
            this.satColorTableName = satColorTableName;
        }
        public String getSatColorTableName() {
            return satColorTableName;
        }
        public void setTransparency(int transparency) {
            this.transparency = transparency;
        }
        public int getTransparency() {
            return transparency;
        }
        public void setLegendType(String legendType) {
            this.legendType = legendType;
        }
        public String getLegendType() {
            return legendType;
        }
    }
    

    public static class GridInfo implements Serializable {
        
        private int selectedGridIndex;
        private int[] selectedRuntimeIndices;
        private int[] selectedTimeIndices;
        private int[] selectedZIndices;

        private int gridSatTransparency;
        private String gridSatelliteLegendType;
        private String gridColorTableName;
        private double gridColorTableMaxValue;
        private double gridColorTableMinValue;


        public void setSelectedGridIndex(int selectedGridIndex) {
            this.selectedGridIndex = selectedGridIndex;
        }
        public int getSelectedGridIndex() {
            return selectedGridIndex;
        }
        public void setSelectedRuntimeIndices(int[] selectedRuntimeIndices) {
            this.selectedRuntimeIndices = selectedRuntimeIndices;
        }
        public int[] getSelectedRuntimeIndices() {
            return selectedRuntimeIndices;
        }
        public void setSelectedTimeIndices(int[] selectedTimeIndices) {
            this.selectedTimeIndices = selectedTimeIndices;
        }
        public int[] getSelectedTimeIndices() {
            return selectedTimeIndices;
        }
        public void setSelectedZIndices(int[] selectedZIndices) {
            this.selectedZIndices = selectedZIndices;
        }
        public int[] getSelectedZIndices() {
            return selectedZIndices;
        }
        
        public void setGridSatTransparency(int gridSatTransparency) {
            this.gridSatTransparency = gridSatTransparency;
        }

        public int getGridSatTransparency() {
            return gridSatTransparency;
        }

        public void setGridSatelliteLegendType(String gridSatelliteLegendType) {
            this.gridSatelliteLegendType = gridSatelliteLegendType;
        }
        
        public String getGridSatelliteLegendType() {
            return this.gridSatelliteLegendType;
        }

        public void setGridColorTableName(String gridColorTableName) {
            this.gridColorTableName = gridColorTableName;
        }
        
        public String getGridColorTableName() {
            return this.gridColorTableName;
        }

        public void setGridColorTableMinValue(double gridColorTableMinValue) {
            this.gridColorTableMinValue = gridColorTableMinValue;
        }
        
        public double getGridColorTableMinValue() {
            return this.gridColorTableMinValue;
        }

        public void setGridColorTableMaxValue(double gridColorTableMaxValue) {
            this.gridColorTableMaxValue = gridColorTableMaxValue;
        }

        public double getGridColorTableMaxValue() {
            return this.gridColorTableMaxValue;
        }

    }

    public static class OverlayInfo implements Serializable {
		
		private boolean layerVisible;
		private Color layerColor;
		private int layerSize;
		private boolean labelVisible;
		private String layerName;
		
		public void setLayerVisible(boolean layerVisible) {
			this.layerVisible = layerVisible;
		}
		public boolean isLayerVisible() {
			return layerVisible;
		}
		public void setLayerColor(Color layerColor) {
			this.layerColor = layerColor;
		}
		public Color getLayerColor() {
			return layerColor;
		}
		public void setLayerSize(int layerSize) {
			this.layerSize = layerSize;
		}
		public int getLayerSize() {
			return layerSize;
		}
		public void setLabelVisible(boolean labelVisible) {
			this.labelVisible = labelVisible;
		}
		public boolean isLabelVisible() {
			return labelVisible;
		}
		public void setLayerName(String layerName) {
			this.layerName = layerName;
		}
		public String getLayerName() {
			return layerName;
		}
		
	}

    
    
    public static class FilterInfo implements Serializable {
    	private Level3Filter l3Filter;
    	private RadialFilter radialFilter;
    	private GridFilter gridFilter;
    	
    	
		public void setLevel3Filter(Level3Filter l3Filter) {
			this.l3Filter = l3Filter;
		}
		public Level3Filter getLevel3Filter() {
			return l3Filter;
		}
		public void setRadialFilter(RadialFilter radialFilter) {
			this.radialFilter = radialFilter;
		}
		public RadialFilter getRadialFilter() {
			return radialFilter;
		}
		public void setGridFilter(GridFilter gridFilter) {
			this.gridFilter = gridFilter;
		}
		public GridFilter getGridFilter() {
			return gridFilter;
		}
    }


    public static abstract class SimpleFilter implements Serializable {
    	private double[] minValues;
    	private double[] maxValues;
    	
		public void setMinValues(double[] minValues) {
			this.minValues = minValues;
		}
		public double[] getMinValues() {
			return minValues;
		}
		public void setMaxValues(double[] maxValues) {
			this.maxValues = maxValues;
		}
		public double[] getMaxValues() {
			return maxValues;
		}
    }

    public static class GridFilter extends SimpleFilter implements Serializable {    	
    }

    public static class Level3Filter extends SimpleFilter implements Serializable {
    	private String[] categoryOverrides;

		public void setCategoryOverrides(String[] categoryOverrides) {
			this.categoryOverrides = categoryOverrides;
		}

		public String[] getCategoryOverrides() {
			return categoryOverrides;
		}
    }

    public static class RadialFilter extends SimpleFilter implements Serializable {
    	private double minDistance;
    	private double maxDistance;
    	private double minAzimuth;
    	private double maxAzimuth;
    	private double minHeight;
    	private double maxHeight;
    	
		public void setMinDistance(double minDistance) {
			this.minDistance = minDistance;
		}
		public double getMinDistance() {
			return minDistance;
		}
		public void setMaxDistance(double maxDistance) {
			this.maxDistance = maxDistance;
		}
		public double getMaxDistance() {
			return maxDistance;
		}
		public void setMinAzimuth(double minAzimuth) {
			this.minAzimuth = minAzimuth;
		}
		public double getMinAzimuth() {
			return minAzimuth;
		}
		public void setMaxAzimuth(double maxAzimuth) {
			this.maxAzimuth = maxAzimuth;
		}
		public double getMaxAzimuth() {
			return maxAzimuth;
		}
		public void setMinHeight(double minHeight) {
			this.minHeight = minHeight;
		}
		public double getMinHeight() {
			return minHeight;
		}
		public void setMaxHeight(double maxHeight) {
			this.maxHeight = maxHeight;
		}
		public double getMaxHeight() {
			return maxHeight;
		}
    }














}
