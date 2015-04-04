package gov.noaa.ncdc.wct.decoders;

import java.awt.image.WritableRaster;

public interface GISRaster {

   public WritableRaster getWritableRaster();
   public void setWritableRaster(WritableRaster raster);
   public java.awt.geom.Rectangle2D.Double getBounds();
   public double getCellsize();
   public double getNoDataValue();
   public boolean isNative();
   public boolean isEmptyGrid();
   public String getUnits();
   public void setUnits(String units);
   public String getLongName();
   public void setLongName(String longName);
   public long getDateInMilliseconds();
   public void setDateInMilliseconds(long dateInMillis);
}
