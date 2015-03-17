package gov.wyo.paperless;

import gov.wyo.paperless.GoogleHelper.AccountTypes;
import gov.wyo.paperless.GoogleHelper.FileRoles;

//import java.io.IOException;
//import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.model.File;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
//import com.google.gdata.data.spreadsheet.WorksheetEntry;
//import com.google.gdata.util.ServiceException;

import javax.inject.Named;

/** An endpoint class we are exposing */
@Api(name = "paperless", version = "v1", namespace = @ApiNamespace(ownerDomain = "paperless.wyo.gov", ownerName = "paperless.wyo.gov", packagePath = ""))
public class YourFirstAPI {

	/** A simple endpoint method that takes a name and says Hi back */
	@ApiMethod(name = "wipeServiceAccount")
	public MyBean wipeServiceAccount(@Named("access_token") String token) {

		String email = new GoogleHelper().validateEmailFromToken(token);
		MyBean response = new MyBean();

		if (email.equals("troy.whorten@wyo.gov")) {
			new GoogleHelper().deleteAllServiceAccountFiles();
			response.setData("Wiped.");
		} else {
			response.setData("NOT AUTHORIZED SUCKA!");
		}

		return response;
	}

	/**
	 * This endpoint makes up Harvest data
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "timecard")
	public Timecard timecard(@Named("token") String token,
			@Named("month") int month, @Named("year") int year) {

		String email = new GoogleHelper().validateEmailFromToken(token);
		// Timecard timecard = generateFakeTimecard(email, month, year);
		Timecard timecard = new HarvestHelper().getTimecard(email, month, year);

		return timecard;
	}

	/**
	 * This endpoint makes up Harvest data
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "reportTimecards")
	public ArrayList<Timecard> reportTimecards(@Named("token") String token,
			@Named("month") int month, @Named("year") int year) {

		String email = new GoogleHelper().validateEmailFromToken(token);
		ArrayList<String> reports = getReports(email);
		ArrayList<Timecard> timecards = new ArrayList<Timecard>();

		for (String report : reports) {

			// Timecard reportTimecard = generateFakeTimecard(report, month,
			// year);
			Timecard reportTimecard = new HarvestHelper().getTimecard(report,
					month, year);

			reportTimecard.submissionStatus = getReportTimecardStatus(token,
					month, year, report);
			timecards.add(reportTimecard);
		}

		return timecards;
	}

	private ArrayList<String> getReports(String email) {
		ArrayList<String> reports = new ArrayList<String>();

		reports = new OrgChartHelper().getReports(email);

		return reports;
	}

	/**
	 * This endpoint makes up Harvest data
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "serviceAccountToken")
	public MyBean serviceAccountToken() {
		String token = "";
		// try this with the service account
		token = new GoogleHelper().getServiceAccountCredential()
				.getAccessToken();
		System.out.println("Service Account access_token: " + token);

		MyBean response = new MyBean();
		response.setData(token);
		return response;
	}

	@ApiMethod(name = "submitTimecard")
	public MyBean submitTimecard(@Named("accessToken") String accessToken,
			@Named("month") Integer month, @Named("year") Integer year) {
		GoogleHelper goog = new GoogleHelper();
		MyBean response = new MyBean();

		try {
			// get email from token
			String email = goog.validateEmailFromToken(accessToken);
			if (email == "") {
				email = goog.validateEmailFromToken(accessToken);
			}

			if (email != "") {
				// get service account credential
				submitTimecard(month, year, goog, email, email);
				response.setData("SUCCESS");

			} else {
				response.setData("BADTOKEN");
			}

		} catch (Exception e) {
			response.setData("ERROR");
			e.printStackTrace();
		}

		return response;
	}

	private void submitTimecard(Integer month, Integer year, GoogleHelper goog,
			String email, String submitter) {
		GoogleCredential serviceCred = goog.getServiceAccountCredential();

		// get drive service with service account
		Drive drive = goog.getDriveService(serviceCred);

		// String monthFolderId =
		// goog.findFolderId(serviceCred.getAccessToken(),
		// monthFolderTitle,yearFolderId);
		String monthFolderId = goog.findFileIdByAlias(
				serviceCred.getAccessToken(), getMonthFolderAlias(month, year));
		if (monthFolderId == "") {
			monthFolderId = createMonthFolderPath(month, year, goog,
					serviceCred, drive);
		}

		// find and delete existing timecard sheet file
		// String timesheetId = goog.findSheetId(
		// serviceCred.getAccessToken(), email, monthFolderId);
		String timesheetId = goog.findFileIdByAlias(
				serviceCred.getAccessToken(), getUserAlias(email, month, year));
		if (timesheetId != null && timesheetId.length() > 0) {
			goog.deleteFile(drive, timesheetId);
		}

		// create new sheet file
		// share with group and submitter
		// get timecard data
		// Timecard timecard = generateFakeTimecard(email, month, year);
		Timecard timecard = new HarvestHelper().getTimecard(email, month, year);

		String timesheetTitle = email + "_(Pending)";
		String content = timecard.getBaseCSV() + "Submitted by " + submitter
				+ " on " + getNowDateString();
		timesheetId = goog.createNewDriveSheet(drive, timesheetTitle,
				monthFolderId, content, getUserAlias(email, month, year))
				.getId();
		goog.insertPermission(drive, timesheetId, Constants.TIMECARD_GROUP,
				AccountTypes.group, FileRoles.writer);

		// this works its just annoying...
		String supervisorEmail = new OrgChartHelper().getSupervisorEmail(email);
		if (email != null) {
			goog.insertPermission(drive, timesheetId, supervisorEmail,
					AccountTypes.user, FileRoles.commenter);
		}
	}

	private String createMonthFolderPath(Integer month, Integer year,
			GoogleHelper goog, GoogleCredential serviceCred, Drive drive) {
		// find or create root folder
		// if new, share with group
		String rootFolderTitle = "Timecards";
		String yearFolderTitle = year.toString();
		String monthFolderTitle = getMonthName(month);
		String rootFolderAlias = "Timecards";
		String yearFolderAlias = rootFolderTitle + yearFolderTitle;
		String monthFolderAlias = getMonthFolderAlias(month, year);

		// find or create year folder
		// if new, share with group
		String yearFolderId = goog.findFileIdByAlias(
				serviceCred.getAccessToken(), yearFolderAlias);
		if (yearFolderId == "") {

			// find or create root folder
			// if new, share with group
			String rootFolderId = goog.findFileIdByAlias(
					serviceCred.getAccessToken(), rootFolderAlias);
			if (rootFolderId == "") {
				System.out.println("Creating root folder 'Timecards'...");
				rootFolderId = goog.createNewDriveFolder(drive,
						rootFolderTitle, null, rootFolderAlias).getId();
				goog.insertPermission(drive, rootFolderId,
						Constants.TIMECARD_GROUP, AccountTypes.group,
						FileRoles.writer);
			}

			System.out.println("Creating year folder '" + yearFolderTitle
					+ "'...");
			yearFolderId = goog.createNewDriveFolder(drive, yearFolderTitle,
					rootFolderId, yearFolderAlias).getId();
			goog.insertPermission(drive, yearFolderId,
					Constants.TIMECARD_GROUP, AccountTypes.group,
					FileRoles.writer);
		}

		// create month folder, share with group
		System.out.println("Creating month folder '" + monthFolderTitle
				+ "'...");
		String monthFolderId = goog.createNewDriveFolder(drive,
				monthFolderTitle, yearFolderId, monthFolderAlias).getId();
		goog.insertPermission(drive, monthFolderId, Constants.TIMECARD_GROUP,
				AccountTypes.group, FileRoles.writer);

		return monthFolderId;

	}

	private String getMonthName(Integer month) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, month - 1);
		String monthName = new SimpleDateFormat("MMMM").format(cal.getTime());
		return monthName;
	}

	private String getMonthFolderAlias(Integer month, Integer year) {
		return year.toString() + getMonthName(month);
	}

	@ApiMethod(name = "checkReportTimecardStatus")
	public MyBean checkReportTimecardStatus(
			@Named("accessToken") String accessToken,
			@Named("month") Integer month, @Named("year") Integer year,
			@Named("reportEmail") String reportEmail) {

		MyBean response = new MyBean();
		response.setData(getReportTimecardStatus(accessToken, month, year,
				reportEmail));
		return response;
	}

	@ApiMethod(name = "submitReportTimecard")
	public ApprovalStatus submitReportTimecard(
			@Named("accessToken") String accessToken,
			@Named("month") Integer month, @Named("year") Integer year,
			@Named("reportEmail") String reportEmail) {

		ApprovalStatus response = new ApprovalStatus();
		response.setEmail(reportEmail);
		GoogleHelper goog = new GoogleHelper();

		label: try {
			// get email from token
			String email = goog.validateEmailFromToken(accessToken);
			if (email == "") {
				email = goog.validateEmailFromToken(accessToken);
			}

			if (email != "") {

				ArrayList<String> reports = getReports(email);
				if (!reports.contains(reportEmail)) {
					response.setData("NOT AUTHORIZED TO APPROVE THIS TIMECARD");
					break label;
				}

				submitTimecard(month, year, goog, reportEmail, email);

				response.setData("SUCCESS");

			} else {
				response.setData("BADTOKEN");
			}
		} catch (Exception e) {
			response.setData("ERROR");
			e.printStackTrace();
		}

		System.out.print(response.getData());
		return response;
	}

	@ApiMethod(name = "approveReportTimecard")
	public ApprovalStatus approveReportTimecard(
			@Named("accessToken") String accessToken,
			@Named("month") Integer month, @Named("year") Integer year,
			@Named("reportEmail") String reportEmail) {

		ApprovalStatus response = new ApprovalStatus();
		response.setEmail(reportEmail);
		GoogleHelper goog = new GoogleHelper();

		label: try {
			// get email from token
			String email = goog.validateEmailFromToken(accessToken);
			if (email == "") {
				email = goog.validateEmailFromToken(accessToken);
			}

			if (email != "") {

				ArrayList<String> reports = getReports(email);
				if (!reports.contains(reportEmail)) {
					response.setData("NOT AUTHORIZED TO APPROVE THIS TIMECARD");
					break label;
				}

				String status = getReportTimecardStatus(accessToken, month,
						year, reportEmail);

				System.out.print(status);

				if (status.equals("Pending")) {
					// get service account credential
					GoogleCredential serviceCred = goog
							.getServiceAccountCredential();

					// get drive service with service account
					Drive drive = goog.getDriveService(serviceCred);

					// get folder id of timecard
					String monthFolderId = getMonthFolderId(month, year,
							serviceCred, goog);

					// find and delete existing timecard sheet file
					String timesheetId = goog.findSheetId(
							serviceCred.getAccessToken(), reportEmail,
							monthFolderId);
					if (timesheetId != null && timesheetId.length() > 0) {
						goog.deleteFile(drive, timesheetId);
					}

					// create new sheet file
					// share with group and submitter
					// get timecard data
					// Timecard timecard = generateFakeTimecard(reportEmail,
					// month, year);
					Timecard timecard = new HarvestHelper().getTimecard(
							reportEmail, month, year);

					String timesheetTitle = reportEmail + "_(Approved)";
					String content = timecard.getBaseCSV() + "Approved by "
							+ email + " on " + getNowDateString();
					timesheetId = goog.createNewDriveSheet(drive,
							timesheetTitle, monthFolderId, content,
							getUserAlias(reportEmail, month, year)).getId();
					goog.insertPermission(drive, timesheetId,
							Constants.TIMECARD_GROUP, AccountTypes.group,
							FileRoles.writer);

					response.setData("SUCCESS");
				} else {
					response.setData("TIMECARD NOT PENDING");
				}
			} else {
				response.setData("BADTOKEN");
			}
		} catch (Exception e) {
			response.setData("ERROR");
			e.printStackTrace();
		}

		System.out.print(response.getData());
		return response;
	}

	private String getUserAlias(String email, Integer month, Integer year) {

		String monthFolderAlias = getMonthFolderAlias(month, year);
		return monthFolderAlias + email;
	}

	private String getNowDateString() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		return sdf.format(date);
	}

	// private String getMonthFolderId(Integer month, Integer year,
	// GoogleCredential serviceCred, GoogleHelper goog, Drive drive) {
	//
	// String monthFolderId = "";
	//
	// // find root folder
	// String rootFolderTitle = "Timecards";
	// String rootFolderId = goog.findFolderId(serviceCred.getAccessToken(),
	// rootFolderTitle, null);
	// if (rootFolderId != "") {
	// // find year folder
	// String yearFolderTitle = year.toString();
	// String yearFolderId = goog
	// .findFolderId(serviceCred.getAccessToken(),
	// yearFolderTitle, rootFolderId);
	// if (yearFolderId != "") {
	// String monthFolderTitle = getMonthFolderAlias(month, year);
	// monthFolderId = goog.findFolderId(serviceCred.getAccessToken(),
	// monthFolderTitle, yearFolderId);
	// }
	// }
	//
	// return monthFolderId;
	// }

	private String getMonthFolderId(Integer month, Integer year,
			GoogleCredential serviceCred, GoogleHelper goog) {

		String monthFolderId = goog.findFileIdByAlias(
				serviceCred.getAccessToken(), getMonthFolderAlias(month, year));

		return monthFolderId;
	}

	private String getReportTimecardStatus(String accessToken, Integer month,
			Integer year, String reportEmail) {

		GoogleHelper goog = new GoogleHelper();
		String response = "";

		label: try {
			// get email from token
			String email = goog.validateEmailFromToken(accessToken);
			if (email == "") {
				email = goog.validateEmailFromToken(accessToken);
			}

			if (email != "") {

				ArrayList<String> reports = getReports(email);
				if (!reports.contains(reportEmail)) {
					response = "NOT AUTHORIZED TO ACCESS THIS PERSON";
					break label;
				}

				// get service account credential
				GoogleCredential serviceCred = goog
						.getServiceAccountCredential();

				// get drive service with service account
				// Drive drive = goog.getDriveService(serviceCred);
				//
				// // find existing timecard sheet file
				// String timesheetId = goog.findSheetId(
				// serviceCred.getAccessToken(), reportEmail,
				// monthFolderId);
				String timesheetId = goog.findFileIdByAlias(
						serviceCred.getAccessToken(),
						getUserAlias(reportEmail, month, year));

				if (timesheetId == null || timesheetId.length() <= 0) {

					String pathStatus = getPathStatus(month, year, goog, serviceCred, false);
					
					if(pathStatus.equals("GOOD")){
						response = "NOT SUBMITTED";
					} else {
						response = pathStatus;
					}					
					
				} else {
					SpreadsheetService service = goog
							.getServiceAccountSpreadsheetService();
					SpreadsheetEntry timecardSheet = goog
							.getSpreadsheetFromFileId(timesheetId, service);
					String title = timecardSheet.getTitle().getPlainText();
					Pattern regex = Pattern.compile("\\(([^\\)]+)\\)");
					Matcher matcher = regex.matcher(title);
					if (matcher.find()) {
						String status = matcher.group(1);
						System.out.println(status);
						response = status;
					} else {
						response = "INVALID NAME";
					}

				}

			} else {
				response = "BADTOKEN";
			}

		} catch (Exception e) {
			response = "ERROR";
			e.printStackTrace();
		}

		return response;
	}

	private String getPathStatus(Integer month, Integer year,
			GoogleHelper goog, GoogleCredential serviceCred, boolean skipMonth) {

		String response = "GOOD";

		String rootFolderAlias = "Timecards";
		String yearFolderAlias = rootFolderAlias + year.toString();
		String monthFolderAlias = getMonthFolderAlias(month, year);

		// find year folder, unless we already know it doesn't exist
		String monthFolderId = "";		
		if(!skipMonth) {
			monthFolderId = goog.findFileIdByAlias(serviceCred.getAccessToken(), monthFolderAlias);
		}
		
		
		if (monthFolderId == "") {
			// find year folder
			String yearFolderId = goog.findFileIdByAlias(serviceCred.getAccessToken(), yearFolderAlias);
			
			if (yearFolderId == "") {

				// find root folder
				String rootFolderId = goog.findFileIdByAlias(serviceCred.getAccessToken(), rootFolderAlias);
				
				if (rootFolderId == "") {
					response = "ROOT FOLDER NOT FOUND";
				} else {
					//root exists, missing year
					response = "YEAR FOLDER NOT FOUND";
				}		
			} else {
				//found year, only month is missing
				response = "MONTH FOLDER NOT FOUND";
			}
			
		}

		System.out.println(response);
		return response;
	}

}


//@ApiMethod(name = "findServiceAcctFileByAlias")
//public MyBean findServiceAcctFileByAlias(@Named("alias") String alias) {
//
//	GoogleHelper goog = new GoogleHelper();
//	GoogleCredential serviceCred = goog.getServiceAccountCredential();
//	MyBean response = new MyBean();
//
//	response.setData(goog.findFileIdByAlias(serviceCred.getAccessToken(),
//			alias));
//
//	return response;
//}

//@ApiMethod(name = "findFolderId")
//public MyBean findFolderId(@Named("folderName") String folderName,
//		@Named("accessToken") String accessToken) {
//	String id = new GoogleHelper().findFolderId(accessToken, folderName);
//
//	MyBean response = new MyBean();
//	response.setData(id);
//	return response;
//}


// find root folder
// String rootFolderTitle = "Timecards";
// String rootFolderId = goog.findFolderId(
// serviceCred.getAccessToken(), rootFolderTitle, null);
// if (rootFolderId == "") {
// response = "ROOT FOLDER NOT FOUND";
// break label;
// }
//
// // find year folder
// String yearFolderTitle = year.toString();
// String yearFolderId = goog.findFolderId(
// serviceCred.getAccessToken(), yearFolderTitle,
// rootFolderId);
// if (yearFolderId == "") {
// response = "YEAR FOLDER NOT FOUND";
// break label;
// }
//
// String monthFolderTitle = getMonthFolderAlias(month, year);
// String monthFolderId = goog.findFolderId(
// serviceCred.getAccessToken(), monthFolderTitle,
// yearFolderId);
// if (monthFolderId == "") {
// response = "MONTH FOLDER NOT FOUND";
// break label;
// }


// private ArrayList<String> getReports(String email) {
// ArrayList<String> reports = new ArrayList<String>();
//
//
//
// // if (email == "josh.soffe@wyo.gov") {
// // reports.add("troy.whorten@wyo.gov");
// // reports.add("paul.ogle@wyo.gov");
// // reports.add("tyler.bjornestad@wyo.gov");
// // reports.add("tyler.christopherson@wyo.gov");
// // reports.add("matt.pfister@wyo.gov");
// // reports.add("kim.turner@wyo.gov");
// // } else {
// // reports.add("test.user1@wyo.gov");
// // reports.add("test.user2@wyo.gov");
// // reports.add("test.user3@wyo.gov");
// // reports.add(email);
// // }
//
// reports = new OrgChartHelper().getReports(email);
//
// return reports;
// }

/** This endpoint retrieves Harvest data */
// @ApiMethod(name = "timecard")
// public MyBean timecard(@Named("token") String token) {
//
// String targetURL = "";
// String urlParameters = "";
// String harvestData = new HttpHelper().executePost(targetURL,
// urlParameters);
//
// MyBean response = new MyBean();
// response.setData(harvestData);
// return response;
// }

/**
 * This endpoint writes timesheet data to Google Drive spreadsheet
 * 
 * @throws ParseException
 */
// @ApiMethod(name = "dummyGoogleSheet")
// public MyBean dummyGoogleSheet(@Named("token") String token,
// @Named("month") int month, @Named("year") int year) {
//
// File sheet = new File();
// GoogleHelper goog = new GoogleHelper();
// WorksheetEntry worksheet = new WorksheetEntry();
//
// // Create a new spreadsheet
// try {
// sheet = goog.createNewTestSheet(token);
// } catch (IOException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// // Update the default worksheet
// try {
// worksheet = goog.updateTestWorksheet(sheet, token);
// } catch (IOException | ServiceException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
//
// MyBean result = new MyBean();
// result.setData(sheet.getId() + " | " + worksheet.getId());
// return result;
// }

/**
 * This endpoint writes timesheet data to Google Drive spreadsheet
 * 
 * @throws GeneralSecurityException
 * 
 * @throws ParseException
 */
// @ApiMethod(name = "dummyGoogleFolder")
// public MyBean dummyGoogleFolder(@Named("token") String token,
// @Named("month") int month, @Named("year") int year)
// throws GeneralSecurityException, IOException {
//
// // String email = validateEmailFromToken(token);
// // Timecard timecard = generateFakeTimecard(email, month, year);
//
// GoogleHelper goog = new GoogleHelper();
//
// // return timecard;
// File folder = new File();
// File subfolder = new File();
// File sharedsubfolder = new File();
//
// folder = goog.createNewTestFolder(token, "");
// subfolder = goog.createNewTestFolder(token, folder.getId());
// sharedsubfolder = goog
// .createNewTestFolder(token,
// "0BxN4AmtAyCpGfkU3YW9DMXQ3VGY2X2xHNHYycUZOQnRRbEdBVDhvRzVPcDhIYTRVck5oMkk");
//
// MyBean result = new MyBean();
// result.setData(folder.getId() + " | " + subfolder.getId() + " | "
// + sharedsubfolder.getId());
// return result;
// }

// private Timecard generateFakeTimecard(String email, int month, int year) {
// Timecard timecard = new Timecard();
// timecard.user = email;
//
// Calendar calendar = Calendar.getInstance();
// calendar.set(Calendar.YEAR, year);
// calendar.set(Calendar.MONTH, month - 1);
//
// if (email != "") {
// for (int i = 0; i < calendar
// .getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
// calendar.set(Calendar.DAY_OF_MONTH, i + 1);
// addFakeTimecardDay(timecard, calendar);
// }
// }
//
// timecard.summaryWorkedHrs = 132.0;
// timecard.summarySickLeave = 16.0;
// timecard.summaryAnnualLeave = 32.0;
//
// return timecard;
// }
//
// private void addFakeTimecardDay(Timecard timecard, Calendar calendar) {
// TimecardDay day = new TimecardDay();
//
// day.setDate(calendar.getTime());
// switch (calendar.get(Calendar.DAY_OF_WEEK)) {
// case Calendar.MONDAY:
// case Calendar.TUESDAY:
// case Calendar.WEDNESDAY:
// day.setWorkHours(8.0);
// break;
// case Calendar.THURSDAY:
// day.setWorkHours(4.0);
// day.setSick(4.0);
// break;
// case Calendar.FRIDAY:
// day.setAnnual(8.0);
// day.setReportedHours(40.0);
// break;
// default:
// break;
// }
//
// timecard.days.add(day);
// }