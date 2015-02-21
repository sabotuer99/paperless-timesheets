package gov.wyo.paperless;

import java.util.Date;

public class TimecardDay {
	private Date date;
	private double workHours;
	private double holiday;
	private double sick;
	private double annual;
	private double otherLeave;
	private double compUsed;
	private double shiftDiff;
	private double onCall;
	private double base;
	private double callback;
	private double reportedHours;
	private double otEarned;
	private double stHours;
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getWorkHours() {
		return workHours;
	}
	public void setWorkHours(double workHours) {
		this.workHours = workHours;
	}
	public double getHoliday() {
		return holiday;
	}
	public void setHoliday(double holiday) {
		this.holiday = holiday;
	}
	public double getSick() {
		return sick;
	}
	public void setSick(double sick) {
		this.sick = sick;
	}
	public double getAnnual() {
		return annual;
	}
	public void setAnnual(double annual) {
		this.annual = annual;
	}
	public double getOtherLeave() {
		return otherLeave;
	}
	public void setOtherLeave(double otherLeave) {
		this.otherLeave = otherLeave;
	}
	public double getCompUsed() {
		return compUsed;
	}
	public void setCompUsed(double compUsed) {
		this.compUsed = compUsed;
	}
	public double getShiftDiff() {
		return shiftDiff;
	}
	public void setShiftDiff(double shiftDiff) {
		this.shiftDiff = shiftDiff;
	}
	public double getOnCall() {
		return onCall;
	}
	public void setOnCall(double onCall) {
		this.onCall = onCall;
	}
	public double getBase() {
		return base;
	}
	public void setBase(double base) {
		this.base = base;
	}
	public double getCallback() {
		return callback;
	}
	public void setCallback(double callback) {
		this.callback = callback;
	}
	public double getReportedHours() {
		return reportedHours;
	}
	public void setReportedHours(double reportedHours) {
		this.reportedHours = reportedHours;
	}
	public double getOtEarned() {
		return otEarned;
	}
	public void setOtEarned(double otEarned) {
		this.otEarned = otEarned;
	}
	public double getStHours() {
		return stHours;
	}
	public void setStHours(double stHours) {
		this.stHours = stHours;
	}
	
}
