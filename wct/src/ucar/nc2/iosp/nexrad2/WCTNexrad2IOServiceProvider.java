/*
 * Copyright 1998-2009 University Corporation for Atmospheric Research/Unidata
 *
 * Portions of this software were developed by the Unidata Program at the
 * University Corporation for Atmospheric Research.
 *
 * Access and use of this software shall impose the following obligations
 * and understandings on the user. The user is granted the right, without
 * any fee or cost, to use, copy, modify, alter, enhance and distribute
 * this software, and any derivative works thereof, and its supporting
 * documentation for any purpose whatsoever, provided that this entire
 * notice appears in all copies of the software, derivative works and
 * supporting documentation.  Further, UCAR requests that the user credit
 * UCAR/Unidata in any publications that result from the use of this
 * software or in any product that includes this software. The names UCAR
 * and/or Unidata, however, may not be used in any advertising or publicity
 * to endorse or promote any products or commercial entity unless specific
 * written permission is obtained from UCAR/Unidata. The user also
 * understands that UCAR/Unidata is not obligated to provide the user with
 * any support, consulting, training or assistance of any kind with regard
 * to the use, operation and performance of this software nor to provide
 * the user with any updates, revisions, new versions or "bug fixes."
 *
 * THIS SOFTWARE IS PROVIDED BY UCAR/UNIDATA "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL UCAR/UNIDATA BE LIABLE FOR ANY SPECIAL,
 * INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
 * FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
 * NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
 * WITH THE ACCESS, USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package ucar.nc2.iosp.nexrad2;

import java.io.IOException;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.AbstractIOServiceProvider;
import ucar.nc2.util.CancelTask;
import ucar.unidata.io.RandomAccessFile;

/**
 * An IOServiceProvider for NEXRAD level II files.
 *
 * @author caron
 */
public class WCTNexrad2IOServiceProvider extends AbstractIOServiceProvider {
	static private org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(WCTNexrad2IOServiceProvider.class);

	private Nexrad2IOServiceProvider iosp = new Nexrad2IOServiceProvider();

	@Override
	public boolean isValidFile( RandomAccessFile raf) throws IOException {
		return iosp.isValidFile(raf);
	}


	@Override
	public void open(RandomAccessFile raf, NetcdfFile ncfile, CancelTask cancelTask) throws IOException {
		
		
//		System.out.println("Level-2 file opening... "+raf.getFD().hashCode());
//		System.out.println(RandomAccessFile.getOpenFiles());
		
		
		iosp.open(raf, ncfile, cancelTask);
		
//		System.out.println(RandomAccessFile.getOpenFiles());

		String[] varNameList = new String[] {
				"Reflectivity_HI", "Reflectivity", 
				"RadialVelocity_HI", "RadialVelocity", 
				"SpectrumWidth_HI", "SpectrumWidth", 
				"DifferentialReflectivity_HI", "DifferentialReflectivity",
				"CorrelationCoefficient_HI", "CorrelationCoefficient", 
				"DifferentialPhase_HI", "DifferentialPhase"
		};
		
		for (String s : varNameList) {	

			try {

				Variable v = ncfile.findVariable(s);
				if (v == null) {
					continue;
				}

				Attribute attScaleFactor = v.findAttribute("scale_factor");
				Attribute attAddOffset = v.findAttribute("add_offset");

				v.addAttribute( new Attribute("scale_factor_metadata", attScaleFactor.getNumericValue()));
				v.addAttribute( new Attribute("add_offset_metadata", attAddOffset.getNumericValue()));
				v.addAttribute( new Attribute("range_folded_value_packed", new Integer(1).byteValue()));  
				v.addAttribute( new Attribute("range_folded_value", 
						attScaleFactor.getNumericValue().doubleValue()*
						DataType.unsignedByteToShort(new Integer(1).byteValue()) + 
						attAddOffset.getNumericValue().doubleValue()));

				v.addAttribute( new Attribute("missing_value", new Integer(0).byteValue()));
			
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public Array readData(Variable v2, Section section) throws IOException,
	InvalidRangeException {
		return iosp.readData(v2, section);
	}


	@Override
	public String getFileTypeId() {
		return iosp.getFileTypeId();
	}


	@Override
	public String getFileTypeDescription() {
		return iosp.getFileTypeDescription();
	}
	
	
	@Override
	public void close() throws IOException {
		
//		System.out.println("Level-2 file closing... ");
		
		
		iosp.close();
//		System.out.println(RandomAccessFile.getOpenFiles());
	}


}