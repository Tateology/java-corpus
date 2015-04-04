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

import gov.noaa.ncdc.iosp.area.AreaIosp;
import gov.noaa.ncdc.iosp.avhrr.AvhrrGacKLMIosp;
import gov.noaa.ncdc.iosp.avhrr.util.formats.GVARArea;
import gov.noaa.ncdc.iosp.avhrr.util.formats.GacKLM;
import gov.noaa.ncdc.iosp.avhrr.util.formats.GacVer1;
import gov.noaa.ncdc.iosp.avhrr.util.formats.IConvertableFile;
import gov.noaa.ncdc.iosp.avhrr.ver1.gac.AvhrrGacVer1Iosp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class Avhrr2Netcdf implements ConverterConstants {

	public static boolean convert(String file, List<String> options, String outdir) throws Exception {
		boolean success = false;

		boolean isGoesNetcdf = false;
		String fileName = file.substring(file.lastIndexOf(File.separator) + 1);
		NetcdfDataset ncfile = null;
		NetcdfFileWriteable ncOut = null;
		try {
			NetcdfFile.registerIOProvider(AvhrrGacKLMIosp.class);
			NetcdfFile.registerIOProvider(AvhrrGacVer1Iosp.class);
			NetcdfFile.registerIOProvider(AreaIosp.class);
//			NetcdfFile.registerIOProvider(GoesAreaIosp.class);

			String outfile = "";

			File outputDir = new File(outdir);
			if (options.contains(OPTION_CLOBBER)) {
				outfile = outputDir + File.separator + fileName + ".nc";
			} else {
				if (outputDir.exists() && outputDir.isDirectory()) {
					boolean validName = false;
					int z = 0;
					while (!validName) {
						if (z == 0) {
							if(!fileName.endsWith(".nc")){
								outfile = outputDir + File.separator + fileName + ".nc";
							}else{
								outfile = outputDir + File.separator + fileName;
							}
						} else {
							outfile = outputDir + File.separator + fileName + "." + z + ".nc";
						}
						File f = new File(outfile);
						if (!f.exists()) {
							validName = true;
						}
						f = null;
						z++;
					}
				}
			}
			ncfile = NetcdfDataset.openDataset(file, true, -1, null, null) ;
			
			// copy all dims and global attributes
			List<Variable> variables = new ArrayList<Variable>();
			ncOut = NetcdfFileWriteable.createNew(outfile, false);
			List<Dimension> dims = ncfile.getDimensions();
			for (Dimension d : dims) {
				ncOut.addDimension(null, d);
				// Variable v = ncfile.findVariable(d.getName());
				// variables.add(v);
			}
			List<Attribute> globalAtts = ncfile.getGlobalAttributes();
			for (Attribute att : globalAtts) {
				ncOut.addGlobalAttribute(att);
			}

			ncOut.addGlobalAttribute("converted by", "AvhrrLevel1B2Netcdf Version 0.5");
			ncOut.addGlobalAttribute("AvhrrLevel1B2Netcdf Version_options", createOptionsString(options));
			ncOut.addGlobalAttribute("original_Avhhr_file", fileName);
			ncOut.addGlobalAttribute("iospInformation", "AvhrrGacKLMIosp Version 2.3, AvhhrGacVer1Iosp Version 2.3");
			ncOut.addGlobalAttribute("ncJavaInformation", "Version 4.1");

		
			// handle options
			// if user wants everything just get variables from input file

			IConvertableFile cf = null;
			if (ncfile.getIosp() instanceof AvhrrGacVer1Iosp) {
				cf = new GacVer1();
			} else if (ncfile.getIosp() instanceof AvhrrGacKLMIosp) {
				cf = new GacKLM();
			} else if (ncfile.getIosp() instanceof AreaIosp) {
				cf = new GVARArea();
//			}else if(ncfile.getIosp() instanceof GoesAreaIosp){
//				cf = new GOESArea();
//			}else if(ncfile.getIosp().getClass().getName().equals("ucar.nc2.N3raf")){
//				cf = new GoesNetcdf();
//				isGoesNetcdf = true;
			}

			List<Attribute> atts = cf.getGlobalAttributes();
			for(Attribute att:atts){
				ncOut.addGlobalAttribute(att);
			}
			
			if (options.contains(OPTION_ALLVAR)) {
				variables = ncfile.getVariables();
				if(isGoesNetcdf){
					Variable var = addCountsVariable(ncfile,ncOut);
					variables.add(var);
					var = addRadianceVariable(ncfile,ncOut);
					variables.add(var);
					var = addCalibratedVariable(ncfile,ncOut);
					variables.add(var);
				}
			} else {
				// add default vars
				for (String s : cf.getDefaultVariables()) {
					Variable var = ncfile.findVariable(s);
					variables.add(var);
				}

				if (options.contains(OPTION_CH1) || options.contains(OPTION_ALLCHAN)) {
					if (options.contains(OPTION_RAW)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCountsVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan1RawVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_RADIANCE)) {
						Variable var;
						if(!isGoesNetcdf){
							var = ncfile.findVariable(cf.getChan1RadVariable());
						}else{
							var = addRadianceVariable(ncfile,ncOut);
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_TEMP)) {
						if(isGoesNetcdf){
							Variable var = addCalibratedVariable(ncfile,ncOut);
							variables.add(var);
						}else{
							Variable var = ncfile.findVariable(cf.getChan1CalVariable());
							variables.add(var);
						}
					}
				}
				if (options.contains(OPTION_CH2) || options.contains(OPTION_ALLCHAN)) {
					if (options.contains(OPTION_RAW)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCountsVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan2RawVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_RADIANCE)) {
						Variable var;
						if(!isGoesNetcdf){
							var = ncfile.findVariable(cf.getChan2RadVariable());
						}else{
							var = addRadianceVariable(ncfile,ncOut);
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_TEMP)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCalibratedVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan2CalVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
				}
				if (options.contains(OPTION_CH3) || options.contains(OPTION_ALLCHAN)) {
					if (options.contains(OPTION_RAW)) {
						String[] chan3vars = cf.getChan3RawVariable();
						if(null != chan3vars){
							for (String s : chan3vars) {
								Variable var = ncfile.findVariable(s);
								if(!variables.contains(var)){
									variables.add(var);
								}
							}
						}
					}
					if (options.contains(OPTION_RADIANCE)) {
						if(!isGoesNetcdf){
							String[] chan3vars = cf.getChan3RadVariable();
							for (String s : chan3vars) {
								Variable var = ncfile.findVariable(s);
								if(!variables.contains(var)){
									variables.add(var);
								}
							}							
						}else{
							Variable var = addCountsVariable(ncfile,ncOut);
							if(!variables.contains(var)){
								variables.add(var);
							}
						}
					}
					if (options.contains(OPTION_TEMP)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCalibratedVariable(ncfile,ncOut);
							variables.add(var);
						}else{
							String[] chan3vars = cf.getChan3CalVariable();
							for (String s : chan3vars) {
								var = ncfile.findVariable(s);
								if(!variables.contains(var)){
									variables.add(var);
								}
							}
						}
					}
				}
				if (options.contains(OPTION_CH4) || options.contains(OPTION_ALLCHAN)) {
					if (options.contains(OPTION_RAW)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCountsVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan4RawVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_RADIANCE)) {
						Variable var;
						if(!isGoesNetcdf){
							var = ncfile.findVariable(cf.getChan4RadVariable());
						}else{
							var = addRadianceVariable(ncfile,ncOut);
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_TEMP)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCalibratedVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan4CalVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
				}

				if (options.contains(OPTION_CH5) || options.contains(OPTION_ALLCHAN)) {

					if (options.contains(OPTION_RAW)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCountsVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan5RawVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_RADIANCE)) {
						Variable var;
						if(!isGoesNetcdf){
							var = ncfile.findVariable(cf.getChan5RadVariable());
						}else{
							var = addRadianceVariable(ncfile,ncOut);
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
					if (options.contains(OPTION_TEMP)) {
						Variable var;
						if(isGoesNetcdf){
							var = addCalibratedVariable(ncfile,ncOut);
						}else{
							var = ncfile.findVariable(cf.getChan5CalVariable());
						}
						if(!variables.contains(var)){
							variables.add(var);
						}
					}
				}
				if (options.contains(OPTION_QUALITY)) {
					for (String s : cf.getQualityVariables()) {
						Variable var = ncfile.findVariable(s);
						variables.add(var);
					}

				}
				if (options.contains(OPTION_CALIBRATION)) {
					for (String s : cf.getCalibrationVariables()) {
						Variable var = ncfile.findVariable(s);
						variables.add(var);
					}
				}
				if (options.contains(OPTION_LATLON)) {
					for (String s : cf.getLatLonVariables()) {
						Variable var = ncfile.findVariable(s);
						variables.add(var);
					}
				}
				if (options.contains(OPTION_METADATA)) {
					for (String s : cf.getMetadataVariables()) {
						Variable var = ncfile.findVariable(s);
						variables.add(var);
					}

				}
			}
			for (Variable var : variables) {
				if (null != var) {
					ncOut.addVariable(null, var);
				}
			}
			if(isGoesNetcdf){
				ncOut.removeVariable(null, "data");
				Variable data = ncfile.findVariable("data");
				variables.remove(data);
			}
			ncOut.create();
			ncOut.finish();
			for (Variable var : variables) {
				if (null != var) {
					if(var.getShortName().equals("calibratedData")  &&  isGoesNetcdf){
						Array a = GoesNetcdfUtil.getCalibratedData(ncfile);
						ncOut.write(var.getShortName(), a);
					}else if(var.getShortName().equals("counts") && isGoesNetcdf) {
						Array a = GoesNetcdfUtil.getCountsData(ncfile);
						ncOut.write(var.getShortName(), a);
					}else if(var.getShortName().equals("radiance") && isGoesNetcdf){
						Array a = GoesNetcdfUtil.getRadianceData(ncfile);
						ncOut.write(var.getShortName(), a);
					}else{
						ucar.ma2.Array varData = var.read();
						ncOut.write(var.getName(), varData);
					}
				}
			}
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally {
			ncOut.flush();
			ncOut.close();
			ncfile.close();
		}
		return success;
	}

	private static String createOptionsString(List<String> options) {
		StringBuffer sb = new StringBuffer("Created with following options ");
		if (options.contains(OPTION_ALLVAR)) {
			sb.append(":All Variables");
		} else {
			if (options.contains(OPTION_ALLCHAN)) {
				sb.append(":All Channels");
			} else {
				if (options.contains(OPTION_CH1)) {
					sb.append(":Ch1");
				}
				if (options.contains(OPTION_CH2)) {
					sb.append(":Ch2");
				}
				if (options.contains(OPTION_CH3)) {
					sb.append(":Ch3");
				}
				if (options.contains(OPTION_CH4)) {
					sb.append(":Ch4");
				}
				if (options.contains(OPTION_CH5)) {
					sb.append(":Ch5");
				}
			}

			if (options.contains(OPTION_RAW)) {
				sb.append(":Raw Data");
			}
			if (options.contains(OPTION_RADIANCE)) {
				sb.append(":Radiance");
			}
			if (options.contains(OPTION_TEMP)) {
				sb.append(":Brightness Temp");
			}
			if (options.contains(OPTION_QUALITY)) {
				sb.append(":Quality");
			}
			if (options.contains(OPTION_CALIBRATION)) {
				sb.append(":Calibration");
			}
			if (options.contains(OPTION_LATLON)) {
				sb.append(":LAT/LON");
			}
			if (options.contains(OPTION_METADATA)) {
				sb.append(":Metadata");
			}
		}
		return sb.toString();
	}
	
	private static Variable addCalibratedVariable(NetcdfDataset ncfile, NetcdfFileWriteable ncOut){
		List<Dimension> dimList = new ArrayList<Dimension>();
		dimList.add(ncfile.findDimension("bands"));
		dimList.add(ncfile.findDimension("lines"));
		dimList.add(ncfile.findDimension("elems"));
		Variable calDataVar = new Variable(ncOut,null,null,"calibratedData");
		calDataVar.setDataType(DataType.FLOAT);
		calDataVar.setDimensions(dimList);
		calDataVar.addAttribute(new Attribute("units","k"));
		calDataVar.addAttribute(new Attribute("long_name","Brightness Temperature"));
		return calDataVar;
	}
	
	private static Variable addRadianceVariable(NetcdfDataset ncfile, NetcdfFileWriteable ncOut){
		List<Dimension> dimList = new ArrayList<Dimension>();
		dimList.add(ncfile.findDimension("bands"));
		dimList.add(ncfile.findDimension("lines"));
		dimList.add(ncfile.findDimension("elems"));
		Variable calDataVar = new Variable(ncOut,null,null,"radiance");
		calDataVar.setDataType(DataType.FLOAT);
		calDataVar.setDimensions(dimList);
		calDataVar.addAttribute(new Attribute("units","mW/(m2-sr-cm-1"));
		calDataVar.addAttribute(new Attribute("long_name","Radiance"));
		return calDataVar;
	}
	
	private static Variable addCountsVariable(NetcdfDataset ncfile, NetcdfFileWriteable ncOut){
		List<Dimension> dimList = new ArrayList<Dimension>();
		dimList.add(ncfile.findDimension("bands"));
		dimList.add(ncfile.findDimension("lines"));
		dimList.add(ncfile.findDimension("elems"));
		Variable calDataVar = new Variable(ncOut,null,null,"counts");
		calDataVar.setDataType(DataType.INT);
		calDataVar.setDimensions(dimList);
		calDataVar.addAttribute(new Attribute("units","counts"));
		calDataVar.addAttribute(new Attribute("long_name","Counts"));
		return calDataVar;
	}
	
}
