package steve.test;

import gov.noaa.ncdc.nexrad.NexradEquations;

public class NexradEquationsTest {

	public static void main(String[] args) {
		
		double slantRangeInMeters = 90000;
		double elevAngleInDegrees = 15.5;
		
		
		
		double heightFromEquation = NexradEquations.getRelativeBeamHeight(elevAngleInDegrees, slantRangeInMeters);
		
		
		double groundRangeFromTriangle = slantRangeInMeters*Math.cos(Math.toRadians(elevAngleInDegrees));
		double groundRangeFromHeightEquation = Math.sqrt(slantRangeInMeters*slantRangeInMeters - heightFromEquation*heightFromEquation);
		
		System.out.println("heightFromEquation  "+heightFromEquation);
		System.out.println("groundRangeFromTriangle  "+groundRangeFromTriangle);
		System.out.println("groundRangeFromHeightEquation  "+groundRangeFromHeightEquation);
		
	}
	
}
