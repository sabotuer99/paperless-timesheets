package gov.wyo.paperless;

import java.util.Date;
import java.util.HashMap;

public class TimecardDay {
	private Date date;
	private Double workHours = 0.0;
	private Double holiday = 0.0;
	private Double sick = 0.0;
	private Double annual = 0.0;
	private Double otherLeave = 0.0;
	private Double compUsed = 0.0;
	private Double shiftDiff = 0.0;
	private Double onCall = 0.0;
	private Double base = 0.0;
	private Double callback = 0.0;
	private Double reportedHours = 0.0;
	private Double otEarned = 0.0;
	private Double stHours = 0.0;
	
	public HashMap<String, String> generateDayData(){
		HashMap<String, String> dayData = new HashMap<String, String>();
		
		dayData.put("Date", date.toString());
		dayData.put("Work Hours", workHours.toString());
		dayData.put("Annual", annual.toString());
		dayData.put("Sick", sick.toString());
		dayData.put("Holiday", holiday.toString());
		dayData.put("Other Leave", otherLeave.toString());
		dayData.put("Comp Used", compUsed.toString());
		dayData.put("Reported Hours", reportedHours.toString());
		dayData.put("OT Earned", otEarned.toString());
		dayData.put("ST Hours", stHours.toString());
		dayData.put("Shift Diff", shiftDiff.toString());
		dayData.put("On Call", onCall.toString());
		dayData.put("Base", base.toString());
		dayData.put("Callback", callback.toString());
		
		return dayData;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Double getWorkHours() {
		return workHours;
	}
	public void setWorkHours(Double workHours) {
		this.workHours = workHours;
	}
	public Double getHoliday() {
		return holiday;
	}
	public void setHoliday(Double holiday) {
		this.holiday = holiday;
	}
	public Double getSick() {
		return sick;
	}
	public void setSick(Double sick) {
		this.sick = sick;
	}
	public Double getAnnual() {
		return annual;
	}
	public void setAnnual(Double annual) {
		this.annual = annual;
	}
	public Double getOtherLeave() {
		return otherLeave;
	}
	public void setOtherLeave(Double otherLeave) {
		this.otherLeave = otherLeave;
	}
	public Double getCompUsed() {
		return compUsed;
	}
	public void setCompUsed(Double compUsed) {
		this.compUsed = compUsed;
	}
	public Double getShiftDiff() {
		return shiftDiff;
	}
	public void setShiftDiff(Double shiftDiff) {
		this.shiftDiff = shiftDiff;
	}
	public Double getOnCall() {
		return onCall;
	}
	public void setOnCall(Double onCall) {
		this.onCall = onCall;
	}
	public Double getBase() {
		return base;
	}
	public void setBase(Double base) {
		this.base = base;
	}
	public Double getCallback() {
		return callback;
	}
	public void setCallback(Double callback) {
		this.callback = callback;
	}
	public Double getReportedHours() {
		return reportedHours;
	}
	public void setReportedHours(Double reportedHours) {
		this.reportedHours = reportedHours;
	}
	public Double getOtEarned() {
		return otEarned;
	}
	public void setOtEarned(Double otEarned) {
		this.otEarned = otEarned;
	}
	public Double getStHours() {
		return stHours;
	}
	public void setStHours(Double stHours) {
		this.stHours = stHours;
	}
	
}
