package gov.wyo.paperless;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;

import gov.wyo.ets.core.ETSHarvestTimeSheet;
import gov.wyo.ets.core.ETSHarvestUser;
import gov.wyo.ets.view.ViewData;
import gov.wyo.ets.view.ViewDataRow;

public class HarvestHelper {

	public Timecard getTimecard(String email, int month, int year){
		
		/*
		 * sDate is the month and year that you
		 * want to get ts information for. It doesn't matter if you
		 * enter 11/15/2014 or 11/01/2014, everything gets processed as
		 * 11/01/2014
		 */
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		
		SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY"); 
		
		String sDate = sdf.format(calendar.getTime());
		
		ViewData viewData = getHarvestTimecard(sDate, email);
		Timecard timecard = convertViewDataToTimecard(viewData);	
		timecard.user = email;
		
		return timecard;
	}
	
	private ViewData getHarvestTimecard(String sdate, String email)
	{
		/*
		 * The following are the Harvest information used to connect
		 * to the Harvest REST api.
		 */
		String subdomain = Secrets.HARVEST_SUBDOMAIN;
		String username  = Secrets.HARVEST_USERNAME;
		String password  = Secrets.HARVEST_PASSWORD;
		
		ViewData vd = null;
		ETSHarvestUser user = new ETSHarvestUser();
		ETSHarvestTimeSheet ts = new ETSHarvestTimeSheet(subdomain, username, password, user);
		
		System.out.println("Fetching Harvest Data...");
		try
		{
			Gson gson = new Gson();
			vd = ts.getUserTimeSheetInformation(email, sdate);
			
			/*
			 * If it is a null it means something screwed up, so return null
			 * and have the user deal with it.
			 */
			if(vd != null)
			{
				String s = gson.toJson(vd);
				System.out.println(s);
			}
			else
			{
				System.out.println("Error generating timesheet data.....");
			}	
		}
		catch(Exception ex)
		{
			System.out.println("Threw exception getting Harvest Data: " + ex.getMessage());
			for (StackTraceElement ste : ex.getStackTrace()) {
				System.out.println(ste.getFileName() + ":" + ste.getClassName() + 
						":" + ste.getLineNumber() + ":" + ste.getMethodName() );
			}
		}
		
		return vd;
	}
	
	private Timecard convertViewDataToTimecard(ViewData viewData){
		
		Timecard timecard = new Timecard();
		
		if(viewData != null){
			timecard.summaryWorkedHrs = viewData.getTimeSheetTotals()[0].totals;
			timecard.summaryHoliday = viewData.getTimeSheetTotals()[1].totals;
			timecard.summarySickLeave = viewData.getTimeSheetTotals()[2].totals;
			timecard.summaryAnnualLeave = viewData.getTimeSheetTotals()[3].totals;
			timecard.summaryCompUsed = viewData.getTimeSheetTotals()[4].totals;
			timecard.summaryOnCall = viewData.getTimeSheetTotals()[5].totals;
			timecard.summaryOtherLeave = viewData.getTimeSheetTotals()[6].totals;
			timecard.summaryTotalReported = viewData.getTimeSheetTotals()[7].totals;
			timecard.summaryShiftDiff = viewData.getTimeSheetTotals()[8].totals;
			timecard.summaryHolidayHrsWorked = viewData.getTimeSheetTotals()[9].totals;
			timecard.summaryOTHrs = viewData.getTimeSheetTotals()[10].totals;
			timecard.summarySTHrs = viewData.getTimeSheetTotals()[11].totals;
			timecard.summaryLastMonthOTPaid = viewData.getTimeSheetTotals()[12].totals;
			timecard.summaryLastMonthOTOwed = viewData.getTimeSheetTotals()[13].totals;
			
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, viewData.getYear());
			calendar.set(Calendar.MONTH, shortMonthStringToInteger(viewData.getMonth()));
			
			for (ViewDataRow viewDataDay : viewData.getTimeSheetValues()) {
				TimecardDay day = new TimecardDay();
				
				calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(viewDataDay.mDate));
				
				day.setDate(calendar.getTime());
				day.setWorkHours(viewDataDay.mWorkHours);
				day.setHoliday(viewDataDay.mHoliday);
				day.setSick(viewDataDay.mSick);
				day.setAnnual(viewDataDay.mAnnual);
				day.setOtherLeave(viewDataDay.mOtherLeave);
				day.setCompUsed(viewDataDay.mCompUsed);
				day.setShiftDiff(viewDataDay.mShiftDiff);
				day.setOnCall(viewDataDay.mOnCall);
				day.setBase(viewDataDay.mBase);
				day.setCallback(viewDataDay.mCallBack);
				day.setReportedHours(viewDataDay.mReportedHours);
				day.setOtEarned(viewDataDay.mOTEarned);
				day.setStHours(viewDataDay.mSTHours);
				
				timecard.days.add(day);		
			}
		}
		return timecard;
	}
	
	private Integer shortMonthStringToInteger(String shortMonth){
		int month = 0;
		
		try {
			Date date = new SimpleDateFormat("MMM").parse(shortMonth);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			month = cal.get(Calendar.MONTH);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return month;
	}
	
}
