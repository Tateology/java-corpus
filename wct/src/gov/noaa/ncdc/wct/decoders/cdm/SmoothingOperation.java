package gov.noaa.ncdc.wct.decoders.cdm;

import gov.noaa.ncdc.wct.WCTUtils;
import gov.noaa.ncdc.wct.event.DataDecodeEvent;
import gov.noaa.ncdc.wct.event.DataDecodeListener;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.util.Vector;

import org.geotools.geometry.GeneralEnvelope;
import org.opengis.spatialschema.geometry.Envelope;


public class SmoothingOperation {

	public WritableRaster smoothRaster(WritableRaster raster, Kernel kernel, int iterations, Vector<DataDecodeListener> listeners) {

        DataDecodeEvent event = new DataDecodeEvent(this);
        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).decodeStarted(event);
        }

		int height = raster.getHeight();
		int width = raster.getWidth();

//		WritableRaster smoothedRaster = raster.createCompatibleWritableRaster();
		float[] smoothedData = new float[raster.getWidth()*raster.getHeight()];
		float[] data = new float[raster.getWidth()*raster.getHeight()];
		data = raster.getSamples(0, 0, width, height, 0, data);
		
		float[] kernelData = kernel.getKernelData(null);
		int kxOrigin = kernel.getXOrigin();
		int kyOrigin = kernel.getYOrigin();
		int kWidth = kernel.getWidth();
		int kHeight = kernel.getHeight();
		
		float cellVal;
		
		for (int n=0; n<iterations; n++) {
			
			System.out.println("SMOOTHING ITERATION "+n+" OF "+iterations);
			
			// If we are iterating more than once, than copy the smoothed results back
			// into the original raster for more smoothing.
//			if (n > 0) {
//				for (int j = 0; j < height; j++) {
//					for (int i = 0; i < width; i++) {
//						raster.setSample(i, j, 0, smoothedRaster.getSampleFloat(i, j, 0));
//					}
//				}
//				for (int m=0; m<data.length; m++) {
//					data[m] = smoothedData[m];
//				}
//			}

			for (int j = 0; j < height; j++) {
		        for (int i = 0; i < listeners.size(); i++) {
		        	event.setProgress((int)Math.round(100*WCTUtils.progressCalculator(new int[] { n, j }, new int[] { iterations, height })));
//		            event.setStatus("Smoothing (Run "+(n+1)+" / "+iterations+")");
		            event.setStatus("Smoothing...");
		            listeners.get(i).decodeProgress(event);
		        }

				for (int i = 0; i < width; i++) {

					// 1. ignore NaN values - don't average stuff into them
//					if (Float.isNaN( raster.getSampleFloat(i, j, 0) )) {
//						smoothedRaster.setSample(i, j, 0, Float.NaN);
//						continue;
//					}
					if (Float.isNaN( data[to1DIndex(i, j, width, height)] )) {
						smoothedData[to1DIndex(i, j, width, height)] = Float.NaN;
						continue;
					}

					cellVal = 0;
					// 2. get neighbors
					for (int kj=0; kj<kHeight; kj++) {
						for (int ki=0; ki<kWidth; ki++) {
							if (i-kxOrigin+ki >= 0 && j-kyOrigin+kj >= 0 && i-kxOrigin+ki < width && j-kyOrigin+kj < height) {
								cellVal += (data[to1DIndex(i-kxOrigin+ki, j-kyOrigin+kj, width, height)] * kernelData[to1DIndex(ki, kj, kWidth, kHeight)]); 
							}
							// if desired neighbor is outside src image, then use origin pixel (could use closest pixel, but that is harder)
							else {
								cellVal += (data[to1DIndex(i, j, width, height)] * kernelData[to1DIndex(ki, kj, kWidth, kHeight)]);
							}
						}
					}

					// 3. set smoothed value in new raster
//					smoothedRaster.setSample(i, j, 0, cellVal);
					smoothedData[to1DIndex(i, j, width, height)] = cellVal;

					if (Float.isNaN(smoothedData[to1DIndex(i, j, width, height)] = cellVal)) {
						System.out.println("SETTING SMOOTHED DATA TO NaN!");;
					}

					//                if (raster.getSampleFloat(i, j, 0) == getNoDataValue() || Float.isNaN( raster.getSampleFloat(i, j, 0) )) {
					//                    raster.setSample(i, j, 0, baseline);
					//                }
				}
			}
			for (int m=0; m<data.length; m++) {
				data[m] = smoothedData[m];
			}
		}

        for (int i = 0; i < listeners.size(); i++) {
            event.setProgress(0);
            listeners.get(i).decodeEnded(event);
        }

		raster.setSamples(0, 0, width, height, 0, data);
		return raster;
		
//        return smoothedRaster;

	}
	
	/**
	 * Assumes rectangular array in order of [j][i] 
	 * @param i
	 * @param j
	 * @param width
	 * @param height
	 * @return
	 */
	private static int to1DIndex(int i, int j, int width, int height) {
		return j*width + i;
	}
	
	
	
	
	
	
	
	
	public Rectangle2D.Double adjustSmoothingExtent(Rectangle2D.Double bounds, int imageWidth, int imageHeight, int smoothFactor) {
		SmoothingInfo si = getSmoothingInfo(bounds, imageWidth, imageHeight, smoothFactor);
		// we loose half the kernel-1 pixels off each side
		double xSize = bounds.getWidth()/imageWidth;
		double ySize = bounds.getHeight()/imageHeight;
		// quick compensation for those pixels
		double newWidth = bounds.getWidth() + 2*xSize*((si.getKernelSize()-1)/2.0 + 1);
		double newHeight = bounds.getHeight() + 2*ySize*((si.getKernelSize()-1)/2.0 + 1);
		bounds = new Rectangle2D.Double(bounds.getCenterX()-newWidth/2, bounds.getCenterY()-newHeight/2, newWidth, newHeight);
		// adjust to maintain aspect ratio
		bounds = WCTUtils.adjustGeographicBounds(new Dimension(imageWidth, imageHeight), bounds);
		
		return bounds;
	}
	
	public SmoothingInfo getSmoothingInfo(Rectangle2D.Double env, int imageWidth, int imageHeight, int smoothFactor) {
		return getSmoothingInfo(new GeneralEnvelope(env), imageWidth, imageHeight, smoothFactor);
	}	
	
	public SmoothingInfo getSmoothingInfo(Envelope env, int imageWidth, int imageHeight, int smoothFactor) {
		

        double size = Math.max(env.getLength(0), env.getLength(1));
        int gridSize = Math.max(imageWidth, imageHeight);
        int kernelSize = (int)(15.0/size);

        kernelSize = (int)(kernelSize*(gridSize/800.0)*(smoothFactor/10.0));
//        kernelSize = (int)(kernelSize*(gridSize/800.0)/10.0);
//        kernelSize/=2;


        // ---------------------
        int smoothIterations = (smoothFactor+1)/2;
        smoothIterations = (int)Math.round((double)smoothIterations/size);
//        smoothIterations = (int)Math.round(Math.pow(smoothIterations, 2)/3);
        if (smoothIterations == 0) {
        	smoothIterations = 1;
        }
        if (smoothIterations > 3) {
        	smoothIterations = 3;
        }
        smoothIterations = 1;
//        kernelSize = kernelSize/2;
        if (kernelSize > 97) {
        	kernelSize = 97;
        }
        // ---------------------

        // only create odd kernel sizes (This is a bug on Macs: JAI messes up convolve with even kernel sizes)
        if (kernelSize%2 == 0) {
            kernelSize++;
        }
        if (smoothFactor == 0) {
        	kernelSize = 1;
        }
        else if (kernelSize < 3) {
            kernelSize = 3;
        }

        System.out.println("SMOOTHING KERNEL SIZE = "+kernelSize+" , iterations: "+smoothIterations+" geo. max size: "+size);
//        System.out.println("SMOOTHING KERNEL SIZE = "+kernelSize+" , geo. max size: "+size);

        float[] kernelMatrix = new float[kernelSize*kernelSize];
        for (int k=0; k<kernelMatrix.length; k++) {
            kernelMatrix[k] = 1.0f/(kernelSize * kernelSize);
        }   

//      // Gaussian smoothing kernel
        float[] gaussianKernelMatrix = new float[] {
        		1,  4,  7,  4, 1,
        		4, 16, 26, 16, 4,
        		7, 26, 41, 26, 7,
        		4, 16, 26, 16, 4,
        		1,  4,  7,  4, 1
        };
        for (int n=0; n<gaussianKernelMatrix.length; n++) {
        	gaussianKernelMatrix[n] = gaussianKernelMatrix[n]/273.0f;
        }
//
//        // Mean smoothing kernel
        float[] meanKernelMatrix = new float[] {
        		1,  1,  1,
        		1,  1,  1,
        		1,  1,  1,
        };
//        float[] meanKernelMatrix = new float[] {
//        		1,  1,  1,  1,  1,
//        		1,  1,  1,  1,  1,
//        		1,  1,  1,  1,  1,
//        		1,  1,  1,  1,  1,
//        		1,  1,  1,  1,  1
//        };
        for (int n=0; n<meanKernelMatrix.length; n++) {
        	meanKernelMatrix[n] = meanKernelMatrix[n]/(float)meanKernelMatrix.length;
        }

        
//      Custom smoothing kernel
        float[] kernelMatrix2 = createUnitKernel(new float[] {
        		1,  1,  1,
        		1,  4,  1,
        		1,  1,  1,
        });
        
        
        SmoothingInfo info = new SmoothingInfo();
        info.setSmoothingIterations(smoothIterations);
        info.setKernelSize(kernelSize);
//        info.setSmoothingIterations(10);
        info.setKernelMatrix(kernelMatrix);
        return info;
	}
	

	
	
	public float[] createUnitKernel(float[] kernelMatrix) {
        float total = 0f;
        for (int n=0; n<kernelMatrix.length; n++) {
        	total += kernelMatrix[n];
        }
        for (int n=0; n<kernelMatrix.length; n++) {
        	kernelMatrix[n] = kernelMatrix[n]/total;
        }
        return kernelMatrix;
	}
	
	
	
	public class SmoothingInfo {
		private int smoothingIterations;
		private int kernelSize;
		private float[] kernelMatrix;
		
		public void setSmoothingIterations(int smoothingIterations) {
			this.smoothingIterations = smoothingIterations;
		}
		public int getSmoothingIterations() {
			return smoothingIterations;
		}
		public void setKernelSize(int kernelSize) {
			this.kernelSize = kernelSize;
		}
		public int getKernelSize() {
			return kernelSize;
		}
		public void setKernelMatrix(float[] kernelMatrix) {
			this.kernelMatrix = kernelMatrix;
		}
		public float[] getKernelMatrix() {
			return kernelMatrix;
		}
	}
	
	
	

}
