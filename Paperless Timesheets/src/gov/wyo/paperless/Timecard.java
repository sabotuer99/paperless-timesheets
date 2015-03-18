package gov.wyo.paperless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class Timecard {
	public String user = "";
	public String fullName = "";
	public String submissionStatus = "";
	public ArrayList<TimecardDay> days = new ArrayList<TimecardDay>();
	public Double summaryWorkedHrs = 0.0;
	public Double summaryHoliday = 0.0;
	public Double summarySickLeave = 0.0;
	public Double summaryAnnualLeave = 0.0;
	public Double summaryCompUsed = 0.0;
	public Double summaryOnCall = 0.0;
	public Double summaryOtherLeave = 0.0;
	public Double summaryTotalReported = 0.0;
	public Double summaryShiftDiff = 0.0;
	public Double summaryHolidayHrsWorked = 0.0;
	public Double summaryOTHrs = 0.0;
	public Double summarySTHrs = 0.0;
	public Double summaryLastMonthOTPaid = 0.0;
	public Double summaryLastMonthOTOwed = 0.0;
	
	public String getBaseCSV() {
		//TODO refactor this header list (which appears in TimeCardDay as well) into an enum or something...
		String[] columnHeaders = new String[]{"Date","Work Hours","Annual","Sick","Holiday","Other Leave","Comp Used","Reported Hours","OT Earned","ST Hours","Shift Diff","On Call","Base","Callback","Summary Field","Summary Value"};
		HashMap<String, String> summaryDataMap = getSummaryDataMap();
		ArrayList<String> summaryDataKeys = getSummaryKeys();
		
		String dataRows = "";
		
		for (int i = 0; i < days.size(); i++) {
			
			//get timecard data
			LinkedHashMap<String, String> dayData = days.get(i).generateDayData();
			for (int j = 0; j < columnHeaders.length - 2; j++) {
				dataRows += dayData.get(columnHeaders[j]).replace(",", " ") + ",";
			}
			
			if(i < summaryDataKeys.size()){
				String key = summaryDataKeys.get(i);
				String value = summaryDataMap.get(key);
				dataRows += key + "," + value + "\n";
			}
			else {
				dataRows += ",\n";
			}			
		}
	
		String headers = Arrays.toString(columnHeaders).replace("[", "").replace("]", "");
		return headers + ",\n" + dataRows;
	}
	
	public HashMap<String, String> getSummaryDataMap() {
		HashMap<String, String> summaryData = new HashMap<String, String>();
		ArrayList<String> keys = getSummaryKeys();
		summaryData.put(keys.get(0), summaryWorkedHrs.toString());
		summaryData.put(keys.get(1), summaryHoliday.toString());
		summaryData.put(keys.get(2), summarySickLeave.toString());
		summaryData.put(keys.get(3), summaryAnnualLeave.toString());
		summaryData.put(keys.get(4), summaryCompUsed.toString());
		summaryData.put(keys.get(5), summaryOnCall.toString());
		summaryData.put(keys.get(6), summaryOtherLeave.toString());
		summaryData.put(keys.get(7), summaryTotalReported.toString());
		summaryData.put(keys.get(8), summaryShiftDiff.toString());
		summaryData.put(keys.get(9), summaryHolidayHrsWorked.toString());
		summaryData.put(keys.get(10), summaryOTHrs.toString());
		summaryData.put(keys.get(11), summarySTHrs.toString());
		summaryData.put(keys.get(12), summaryLastMonthOTPaid.toString());
		summaryData.put(keys.get(13), summaryLastMonthOTOwed.toString());
		return summaryData;
	}
	
	public ArrayList<String> getSummaryKeys() {
		ArrayList<String> summaryData = new ArrayList<String>();
		summaryData.add("Worked Hrs");
		summaryData.add("Holiday");
		summaryData.add("Sick Leave");
		summaryData.add("Annual Leave");
		summaryData.add("Comp Used");
		summaryData.add("On Call");
		summaryData.add("Other Leave");
		summaryData.add("Total Reported Hrs");
		summaryData.add("Shift Diff");
		summaryData.add("Holiday Hrs Worked");
		summaryData.add("OT Hrs");
		summaryData.add("ST Hrs");
		summaryData.add("Last Month/Week OT Hrs Already Paid");
		summaryData.add("Last Month/Week OT Hrs Still Owed");
		return summaryData;
	}

}
