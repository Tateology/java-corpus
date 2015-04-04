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

import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JTextArea;

public class NetcdfConverter extends Thread{
	
	private int current;
	private int total;
	private Vector<File> files;
	private List<String> options;
	private String outdir;

	public NetcdfConverter(Vector<File> files, List<String> options,String basedir) {
		current = 0;
		this.files = files;
		total = files.size();
		this.options = options;
		outdir = basedir;
	}

	public int getTotal() {
		return total;
	}

	public int getCurrent() {
		return current -1;
	}

	public void run() {
		AvhrrLevel1B2Netcdf myFrame = AvhrrLevel1B2Netcdf.getFrame();
		JTextArea myArea = myFrame.getLogTextArea();
		current++;
		current++;
		int row = 0;
		while (current < total +2 && !interrupted()) {
			try {
				File file = files.get(current-2);
				row = myFrame.getRowForFile(file.getName());
				boolean b = Avhrr2Netcdf.convert(file.getPath(), options, outdir);
				if(!b){
					myFrame.getJTable1().setValueAt("FAILED", row, 8);
					myArea.append("Error converting file: " + file.getName() + "\n");
				}else{
					myArea.append("Successfully converted file: " + file.getName() + "\n");
					myFrame.getJTable1().setValueAt("YES", row, 8);
				}
			} catch (Exception e) {
				myArea.append(e.getMessage());
				myFrame.getJTable1().setValueAt("FAILED", row, 8);
			}
			current++;
		}
	}
}
