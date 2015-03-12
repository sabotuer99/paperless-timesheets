package gov.wyo.paperless;

import java.util.ArrayList;

public class Timecard {
	public String user = "";
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
}
