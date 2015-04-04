package gov.noaa.ncdc.wct.ui.plugins;

import java.util.ArrayList;
import java.util.Iterator;


// ==========================================================================================================
// IMPACT_Data
// ==========================================================================================================
class IMPACTData {

	boolean valid;

	private ArrayList<String> availableDateList = new ArrayList<String>();
	private ArrayList<String> statParmList = new ArrayList<String>();
	
	private int month;
	private int monthYearEnd;
	private int monthYearStart;
	
	private int season;
	private int seasonYearEnd;
	private int seasonYearStart;
	
	private int timePeriod;
	
	private int variable;
	
	private int yearEnd;
	private int yearStart;

	private long endDateMillisecs;
	private long startDateMillisecs;


	public IMPACTData() { }

	public void displayIMPACTData() { 
		
		System.out.println("\n-------------------");
		System.out.println("Start Date Millisecs = " + getStartDateMillisecs());
		System.out.println("End Date Millisecs = " + getEndDateMillisecs());
		System.out.println("-------------------");
		System.out.println("Season Year Start = " + getSeasonYearStart());
		System.out.println("Season Year End = " + getSeasonYearEnd());
		System.out.println("-------------------");
		System.out.println("Month Year Start = " + getMonthYearStart());
		System.out.println("Month Year End = " + getMonthYearEnd());
		System.out.println("-------------------");
		System.out.println("Year Start = " + getYearStart());
		System.out.println("Year End = " + getYearEnd());
		System.out.println("-------------------");
		
		if (isValid()) { 
			System.out.println("Valid = True");
		}
		else { 
			System.out.println("Valid = False");
		}
		
		System.out.println("-------------------");
		
		ArrayList<String> availdateList = getAvailableDateList();
		
		Iterator<String> itr = availdateList.iterator();
		
		while (itr.hasNext()) { 
			String element = itr.next();
			System.out.println("Available Date List: " + element);
		}
		
		System.out.println("-------------------");
		
		Iterator<String> itr2 = statParmList.iterator();
		
		while (itr2.hasNext()) { 
			String element = itr2.next();
			System.out.println("Statistical Parameter List: " + element);
		}
		
		System.out.println("-------------------");
		System.out.print("\n");
	}


	public long getEndDateMillisecs() { 
		return this.endDateMillisecs;
	}


	public ArrayList<String> getAvailableDateList() { 
		return availableDateList;
	}

	public int getMonthYearEnd() { 
		return this.monthYearEnd;
	}

	public int getMonthYearStart() { 
		return this.monthYearStart;
	}

	public int getMonthly() { 
		return this.month;
	}

	public int getSeason() { 
		return this.season;
	}

	public int getSeasonYearEnd() { 
		return this.seasonYearEnd;
	}

	public int getSeasonYearStart() { 
		return this.seasonYearStart;
	}

	public long getStartDateMillisecs() { 
		return this.startDateMillisecs;
	}

	public ArrayList<String> getStatParmList() { 
		return statParmList;
	}

	public int getTimePeriod() { 
		return this.timePeriod;
	}

	public int getVariable() { 
		return this.variable;
	}

	public int getYearEnd() { 
		return this.yearEnd;
	}

	public int getYearStart() { 
		return this.yearStart;
	}

	public boolean isValid() { 
		return valid;
	}

	public void setEndDateMillisecs(long value) { 
		this.endDateMillisecs = value;
	}

	public void setAvailableDateList(ArrayList<String> availableDateList) { 
		this.availableDateList = availableDateList;
	}

	public void setMonthYearEnd(int value) {
		this.monthYearEnd = value;
	}

	public void setMonthYearStart(int value) { 
		this.monthYearStart = value;
	}

	public void setMonthly(int value) { 
		this.month = value;
	}

	public void setSeason(int value) { 
		this.season = value;
	}

	public void setSeasonYearEnd(int value) { 
		this.seasonYearEnd = value;
	}

	public void setSeasonYearStart(int value) { 
		this.seasonYearStart = value;
	}

	public void setStartDateMillisecs(long value) { 
		this.startDateMillisecs = value;
	}

	public void setStatParmList(ArrayList<String> statParmList) { 
		this.statParmList = statParmList;
	}

	public void setTimePeriod(int value) { 
		this.timePeriod = value;
	}

	public void setValid(boolean valid) { 
		this.valid = valid;
	}

	public void setVariable(int value) { 
		this.variable = value;
	}

	public void setYearEnd(int value) { 
		this.yearEnd = value;
	}

	public void setYearStart(int value) { 
		this.yearStart = value;
	}
}
