package gov.noaa.ncdc.wct.io;

import gov.noaa.ncdc.wct.event.GeneralProgressEvent;
import gov.noaa.ncdc.wct.event.GeneralProgressListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import ucar.unidata.io.UncompressInputStream;

public class Decompressor {

	
	private boolean shouldCancel = false;
	
	private ArrayList<GeneralProgressListener> progressListeners = new ArrayList<GeneralProgressListener>();
	private GeneralProgressEvent progressEvent = new GeneralProgressEvent(this);

	
	
	public File processFile(File file, File outputDir) throws IOException {
		return processStream(file.getName(), file.length(), new BufferedInputStream(new FileInputStream(file)), outputDir);
	}
	
	public File processURL(URL url, File outputDir) throws FileNotFoundException, IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("HEAD");
        InputStream is = conn.getInputStream();
        long fileLength = conn.getContentLength();

        conn.setRequestMethod("GET");
        if (fileLength < 0) {
        	is = conn.getInputStream();
        	fileLength = conn.getContentLength();
        }
        
        return processStream(url.getPath(), fileLength, new BufferedInputStream(is), outputDir);	
	}
	
	public File processStream(String filename, long fileSize, InputStream inputStream, File outputDir) throws IOException {
		int BUFFER = 2048;
		
		if (! outputDir.exists()) {
			outputDir.mkdirs();
		}

		
		
		
        progressEvent.setStatus("Initializing decompression...");
        progressEvent.setProgress(0);
        for (GeneralProgressListener l : progressListeners) {
        	l.started(progressEvent);
        }

        
        
        
        File returnFile = null;
		
		if (filename.endsWith(".zip")) {
			
	        BufferedOutputStream dest = null;
	        ZipInputStream zis = new ZipInputStream(inputStream);
	        ZipEntry entry;
	        long bytesRead = 0L;
//	        progressBar.setMaximum((int)fileSize);
	        
	
	        
	        
	        
	        try {
	        
	        	while((entry = zis.getNextEntry()) != null) {
	        		//	           System.out.println("Extracting: " +entry.getName());
	        		bytesRead += entry.getCompressedSize();
//	        		progressBar.setString("Extracting: "+entry.getName());
//	        		progressBar.setValue((int)bytesRead);
	        		//	        	System.out.println(bytesRead +" : "+ fileSize + " : "+bytesRead/(double)fileSize);

	        		
	    	        progressEvent.setStatus("Extracting: "+entry.getName());
	    	        progressEvent.setProgress(100.0*bytesRead/((double)fileSize));
	    	        for (GeneralProgressListener l : progressListeners) {
	    	        	l.progress(progressEvent);
	    	        }


	        		if (entry.isDirectory()) {
	        			new File(outputDir.toString()+File.separator+entry).mkdirs();
	        		}
	        		else {

	        			int count;
	        			byte data[] = new byte[BUFFER];
	        			// write the files to the disk
	        			FileOutputStream fos = new FileOutputStream(outputDir + File.separator + entry.getName());
	        			dest = new BufferedOutputStream(fos, BUFFER);
	        			while ((count = zis.read(data, 0, BUFFER)) != -1) {
	        				dest.write(data, 0, count);
	        			}
	        			dest.flush();
	        			dest.close();

	        		}	           


	        		if (shouldCancel) {
	        			shouldCancel = false;
	        			break;
	        		}
	        	}
	        
			} catch (IOException e) {
				e.printStackTrace();
				zis.close();
				throw e;
			}

	        
	        zis.close();	
	        returnFile = outputDir;
		}
		else if (filename.endsWith(".tar") || filename.endsWith(".tar.gz") 
				|| filename.endsWith(".tar.Z") || filename.endsWith(".tgz")) {
			
	        BufferedOutputStream dest = null;
	        TarArchiveInputStream tis = null;
	        if (filename.endsWith(".tar.gz") || filename.endsWith(".tgz")) {
	        	tis = new TarArchiveInputStream(new GZIPInputStream(inputStream));
	        }
	        else if (filename.endsWith(".tar.Z")) {
	        	tis = new TarArchiveInputStream(new UncompressInputStream(inputStream));
	        }
	        else {
	        	tis = new TarArchiveInputStream(new BufferedInputStream(inputStream));
	        }
	        TarArchiveEntry entry;
	        long bytesRead = 0L;
//	        progressBar.setMaximum((int)fileSize);
	        
	        try {
				
	        
	        	while((entry = tis.getNextTarEntry()) != null) {
	        		//		           System.out.println("Extracting: " +entry.getName());
	        		bytesRead += entry.getSize();
//	        		progressBar.setString("Extracting: "+entry.getName());
//	        		progressBar.setValue((int)tis.getBytesRead());
	        		//	        	System.out.println(bytesRead +" : "+ fileSize + " : "+bytesRead/(double)fileSize);

	        		
	    	        progressEvent.setStatus("Extracting: "+entry.getName());
	    	        progressEvent.setProgress(100.0*bytesRead/((double)fileSize));
	    	        for (GeneralProgressListener l : progressListeners) {
	    	        	l.progress(progressEvent);
	    	        }
	    	        
	        		

	        		if (entry.isDirectory()) {
	        			new File(outputDir.toString()+File.separator+entry).mkdirs();
	        		}
	        		else {

	        			int count;
	        			byte data[] = new byte[BUFFER];
	        			// write the files to the disk
	        			FileOutputStream fos = new FileOutputStream(outputDir + File.separator + entry.getName());
	        			dest = new BufferedOutputStream(fos, BUFFER);
	        			while ((count = tis.read(data, 0, BUFFER)) != -1) {
	        				dest.write(data, 0, count);
	        			}
	        			dest.flush();
	        			dest.close();

	        		}	           



	        		if (shouldCancel) {
	        			shouldCancel = false;
	        			break;
	        		}
	        	}
	        
			} catch (IOException e) {
				e.printStackTrace();
				tis.close();
				throw e;
			}
	        
	        tis.close();	
	        returnFile = outputDir;
		}
		else if (filename.endsWith(".Z") || filename.endsWith(".gz") 
				|| filename.endsWith(".GZ") || filename.endsWith(".bz2")
				|| filename.endsWith(".bzip2")) {
			
	        BufferedOutputStream dest = null;
	        InputStream is = null;
	        if (filename.endsWith(".gz") || filename.endsWith(".GZ")) {
//	        	is = new GZIPInputStream(inputStream);
	        	is = new ExposedGZIPInputStream(inputStream);
	        }
	        else if (filename.endsWith(".Z")) {
	        	is = new UncompressInputStream(inputStream);
	        }
	        else {
	        	is = new BZip2CompressorInputStream(inputStream);
	        }
	        
	        try {
				
	        		
	    	        
	        		
	    	     long bytesRead = 0L;

	    	     int count;
	    	     byte data[] = new byte[BUFFER];
	    	     // write the files to the disk
	    	     String filenameNoExt = filename.substring(0, filename.lastIndexOf("."));
	    	     FileOutputStream fos = new FileOutputStream(outputDir + File.separator + filenameNoExt);
	    	     dest = new BufferedOutputStream(fos, BUFFER);
	    	     while ((count = is.read(data, 0, BUFFER)) != -1) {
	    	    	 dest.write(data, 0, count);



	    	    	 progressEvent.setStatus("Decompressing: "+filename);
	    	    	 bytesRead += BUFFER;
	    	    	 
	    	    	 long compressedBytesRead = 0;
	    		     if (filename.endsWith(".gz") || filename.endsWith(".GZ")) {
	    		    	 compressedBytesRead = ((ExposedGZIPInputStream)is).inflater().getBytesRead();
		    	    	 progressEvent.setProgress(100.0*compressedBytesRead/((double)fileSize));
		    	    	 for (GeneralProgressListener l : progressListeners) {
		    	    		 l.progress(progressEvent);
		    	    	 }
	    		     }
	    		     else {
		    	    	 progressEvent.setProgress(100.0*bytesRead/((double)fileSize));
		    	    	 for (GeneralProgressListener l : progressListeners) {
		    	    		 l.progress(progressEvent);
		    	    	 }
	    		     }
	    		     
	    	    	 


	    	    	 if (shouldCancel) {
	    	    		 shouldCancel = false;
	    	    		 
	    	    		 fos.close();
	    	    		 new File(outputDir + File.separator + filenameNoExt).delete();
	    	    		 break;
	    	    	 }
	    	     }
	    	     dest.flush();
	    	     dest.close();





	        	
	        
			} catch (IOException e) {
				e.printStackTrace();
				is.close();
				throw e;
			}
	        
	        is.close();	 
	        returnFile = new File(outputDir.toString() + File.separator + filename);
		}


		progressEvent.setStatus("");
		progressEvent.setProgress(0);
		for (GeneralProgressListener l : progressListeners) {
			l.ended(progressEvent);
		}

		return returnFile;

	}
	
	
	
	
	
	
	public void addProgressListener(GeneralProgressListener l) {
		progressListeners.add(l);
	}
	public void clearProgressListeners() {
		progressListeners.clear();
	}
	public void removeProgressListener(GeneralProgressListener l) {
		progressListeners.remove(l);
	}
	
	
	public void setShouldCancel(boolean shouldCancel) {
		this.shouldCancel = shouldCancel;
	}

	public boolean isShouldCancel() {
		return shouldCancel;
	}


	
	
	
	
	
	
	
	public final class ExposedGZIPInputStream extends GZIPInputStream {

		  public ExposedGZIPInputStream(final InputStream stream) throws IOException {
		    super(stream);
		  }

		  public ExposedGZIPInputStream(final InputStream stream, final int n) throws IOException {
		    super(stream, n);
		  }

		  public Inflater inflater() {
		    return super.inf;
		  }
	}
}
