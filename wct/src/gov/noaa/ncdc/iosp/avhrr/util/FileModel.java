/**
*      Copyright (c) 2008 Work of U.S. Government.
*      No rights may be assigned.
*
* LIST OF CONDITIONS
* Redistribution and use of this program in source and binary forms, with or
* without modification, are permitted for any purpose (including commercial purposes) 
* provided that the following conditions are met:
*
* 1.  Redistributions of source code must retain the above copyright notice,
*     this list of conditions, and the following disclaimer.
*
* 2.  Redistributions in binary form must reproduce the above copyright notice,
*     this list of conditions, and the following disclaimer in the documentation
*    and/or materials provided with the distribution.
*
* 3.  In addition, redistributions of modified forms of the source or binary
*     code must carry prominent notices stating that the original code was
*     changed, the author of the revisions, and the date of the change.
*
* 4.  All publications or advertising materials mentioning features or use of
*     this software are asked, but not required, to acknowledge that it was
*     developed at the NOAA's National Climatic Data Center in Asheville, NC and to
*     credit the contributors.
*
* 5.  THIS SOFTWARE IS PROVIDED BY THE GOVERNMENT AND THE CONTRIBUTORS  "AS IS"
*     WITH NO WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED.  In no event
*     shall the Government or the Contributors be liable for any damages
*     suffered by the users arising out of the use of this software, even if
*     advised of the possibility of such damage.
*/


package gov.noaa.ncdc.iosp.avhrr.util;

import gov.noaa.ncdc.iosp.area.AreaFileUtil;
import gov.noaa.ncdc.iosp.area.AreaIosp;
import gov.noaa.ncdc.iosp.avhrr.AvhrrGacKLMIosp;
import gov.noaa.ncdc.iosp.avhrr.ver1.gac.AvhrrGacVer1Iosp;

import java.io.File;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

public class FileModel extends AbstractTableModel{

	public static String AVHRR_GAC_VER1 = "Avhrr Gac Version 1";
	public static String AVHRR_GAC_KLM = "Avhrr Gac KLM";
	public static String UNDEFINED_FORMAT = "undefined";
	
	
	String titles[] = new String[] {
			"Convert?","File Name", "Size(MB)","Satellite", "Valid","Scan Lines","pixels/line", "Output Size(MB)", "Converted"};
	Class types[] = new Class[]{
			Boolean.class,String.class,Double.class,String.class,String.class,Integer.class,Integer.class,Double.class};
	
	Object data[][];
	
	Vector<File> files = new Vector<File>();
//	public FileModel() {this("."); }
	
	public FileModel() {
//		setFileStats(files);
	}
	
	public int getRowCount() {	
		return  data == null ? 0 : data.length;
	}
	
	public int getColumnCount() {return titles.length;}
	public String getColumnName(int c) {return titles[c];}
	public Class getColumnClass(int c) {return types[c];}
	public Object getValueAt(int r, int c) {return data[r][c];}
	
	public void setFileStats(Vector<File> files){
		if(null != files){
			data = new Object[files.size()][9];
			int i=0;
			for(File f: files){
				ConverterFile cf = validateFile(f);
				data[i][0] = cf.getConvert();
				data[i][1] = cf.getFilename();
				data[i][2] = f.length() * 1e-6f;
//				data[i][2] = new Date(f.lastModified());
				data[i][3] = cf.getVersion();
				data[i][4] = cf.isValid();
				data[i][5] = cf.getScanLines();
				data[i][6] = cf.getPixels();
				data[i][7] = (f.length() * 10.0f)/1e6;
				data[i][8] = "NO";
				i++;
			}
		}
		fireTableDataChanged();
	}
	
	public void removeRows(int[] rFiles){
		for(int i = rFiles.length - 1;i>=0;i--){
			files.remove(rFiles[i]);
		}
		setFileStats(files);
		fireTableDataChanged();
	}
	
	public void updateTable(File[] newFiles){
		for(File f: newFiles){
			if(!files.contains(f)){
				files.add(f);
			}
		}
		setFileStats(files);
	}

	public File getFileAtRow(int i){
		return files.get(i);
	}
	
	public Vector<File> getFiles(int[] ia){
		Vector<File> v = new Vector<File>();
		for(int i:ia){
			v.add(files.get(i));
		}
		return v;
	}
	
	
    public boolean isCellEditable(int row, int col) {
        if (col < 1 &&  (Boolean)data[row][4]) {
            return true;
        } else {
            return false;
        }
    }
	
    public Vector<File> getFilesToConvert() {
		if (null != files && !files.isEmpty()) {
			Vector<File> toConvert = new Vector<File>();
			for (int i = 0; i < files.size(); i++) {
				boolean b = (Boolean) data[i][0];
				boolean valid = (Boolean) data[i][4];
				if (b && valid) {
					toConvert.add(files.get(i));
				}
			}
			return toConvert;
		} else {
			return null;
		}
	}
    
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
        fireTableDataChanged();
    }
	
	private ConverterFile validateFile(File file){
		AvhrrLevel1B2Netcdf myFrame = AvhrrLevel1B2Netcdf.getFrame();
		JTextArea myArea = myFrame.getLogTextArea();
		ConverterFile cf = new ConverterFile();
		try {
			cf.setConvert(true);
			cf.setFilename(file.getName());
			ucar.unidata.io.RandomAccessFile raf = new ucar.unidata.io.RandomAccessFile(file.getAbsolutePath(),"r");
			AvhrrGacVer1Iosp iosp1 = new AvhrrGacVer1Iosp();
			AvhrrGacKLMIosp iosp2 = new AvhrrGacKLMIosp();
			AreaIosp iosp3 = new AreaIosp();
//			GoesAreaIosp iosp4 = new GoesAreaIosp();
//			GoesNetcdfIosp iosp5 = new GoesNetcdfIosp();
			
			if(iosp1.isValidFile(raf)){
				cf.setValid(true);
				cf.setVersion(iosp1.getSatelliteName(raf));
				cf.setScanLines(iosp1.getScanLineCount(raf));
				cf.setPixels(409);
				raf.close();
			}else if(iosp2.isValidFile(raf)) {
				cf.setValid(true);
				cf.setVersion(iosp2.getSatelliteName(raf));
				cf.setScanLines(iosp2.getScanLineCount(raf));
				cf.setPixels(409);
				raf.close();
			}else if(iosp3.isValidFile(raf)){
				int[] isize = AreaFileUtil.getImageSize(raf);
				int pixels = isize[0] * isize[1];
				if(2000000 > pixels){
					cf.setValid(true);
				}else{
					AvhrrLevel1B2Netcdf.getFrame();
					cf.setValid(true);
					
				}
				cf.setVersion(AreaFileUtil.getSatelliteName(raf));
				int[] size = AreaFileUtil.getImageSize(raf);
				cf.setScanLines(size[0]);
			    cf.setPixels(size[1]);
				raf.close();
//			}else if(iosp4.isValidFile(raf)){
//				int[] isize = AreaFileUtil.getImageSize(raf);
//				int pixels = isize[0] * isize[1];
//				if(2000000 > pixels){
//					cf.setValid(false);
//					myArea.append("Input file: " + raf.getLocation() + " is too big to be converted\n");
//				}else{
//					cf.setValid(true);
//				}
//				cf.setVersion(AreaFileUtil.getSatelliteName(raf));
//				int[] size = AreaFileUtil.getImageSize(raf);
//				cf.setScanLines(size[0]);
//				cf.setPixels(size[1]);
//			    raf.close();
//			}else if(iosp5.isValidFile(raf)){
//				int bpp = iosp5.getBytesPerPixel(raf);
//				if(2 == bpp){
//					cf.setValid(true);
//				}else{
//					cf.setValid(false);
//					myArea.append("Input file: " + raf.getLocation() + " is a 1 byte file and is not supported! \n");
//				}
//				cf.setVersion(iosp5.getSatelliteName(raf));
//				int[] size = iosp5.getImageSize(raf);
//				cf.setScanLines(size[0]);
//				cf.setPixels(size[1]);
//				raf.close();
			}else{
				cf.setValid(false);
				myArea.append("Input file: " + raf.getLocation() + " is not supported!\n");
				cf.setVersion(UNDEFINED_FORMAT);
				cf.setConvert(false);
				raf.close();
			}
		}catch(Exception e){
			cf.setValid(false);
			cf.setConvert(false);
		}
		return cf;
	}
	
	/**
	 * returns all files in the table
	 * @return
	 */
	public Vector<File> getAllFiles(){
		return files;
	}
	
	class ConverterFile {
		
		boolean convert;
		String filename;
		Number size;
		String version;
		boolean valid;
		String satellite;
		int scanLines;
		int pixels;
		
		public void setConvert(boolean convert){
			this.convert = convert;
		}
		
		public boolean getConvert(){
			return convert;
		}
		
		public void setFilename(String name){
			filename = name;
		}
		
		public String getFilename(){
			return filename;
		}

		public Number getSize() {
			return size;
		}

		public void setSize(Number size) {
			this.size = size;
		}

		public boolean isValid() {
			return valid;
		}

		public void setValid(boolean valid) {
			this.valid = valid;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}
		
		public String getSatellite(){
			return satellite;
		}
		
		public void setSatellite(String s){
			satellite = s;
		}
		
		public int getScanLines(){
			return scanLines;
		}
		
		public void setScanLines(int i){
			scanLines = i;
		}

		public int getPixels() {
			return pixels;
		}

		public void setPixels(int pixels) {
			this.pixels = pixels;
		}
	}
}
