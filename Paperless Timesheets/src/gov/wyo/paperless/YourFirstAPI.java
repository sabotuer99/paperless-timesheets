package gov.wyo.paperless;

import gov.wyo.paperless.GoogleHelper.AccountTypes;
import gov.wyo.paperless.GoogleHelper.FileRoles;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

import javax.inject.Named;

/** An endpoint class we are exposing */
@Api(name = "paperless", 
     version = "v1", 
     namespace = @ApiNamespace(ownerDomain = "paperless.wyo.gov", 
                               ownerName = "paperless.wyo.gov", 
                               packagePath = ""))
public class YourFirstAPI {

	/** A simple endpoint method that takes a name and says Hi back */
	@ApiMethod(name = "sayHi")
	public MyBean sayHi(@Named("name") String name) {
		MyBean response = new MyBean();
		response.setData("Hi, " + name);

		return response;
	}

	/** This endpoint retrieves Harvest data */
	@ApiMethod(name = "timecard")
	public MyBean timecard(@Named("token") String token) {

		String targetURL = "";
		String urlParameters = "";
		String harvestData = new HttpHelper().executePost(targetURL,
				urlParameters);

		MyBean response = new MyBean();
		response.setData(harvestData);
		return response;
	}

	/**
	 * This endpoint makes up Harvest data
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "dummyTimecard")
	public Timecard dummyTimecard(
			@Named("token") String token, 
			@Named("month") int month, 
			@Named("year") int year) 
	{

		String email = new GoogleHelper().validateEmailFromToken(token);
		Timecard timecard = generateFakeTimecard(email, month, year);
		
		return timecard;
	}
	
	/**
	 * This endpoint writes timesheet data to Google Drive spreadsheet
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "dummyGoogleSheet")
	public MyBean dummyGoogleSheet(
			@Named("token") String token, 
			@Named("month") int month, 
			@Named("year") int year) 
	{

		File sheet = new File();
		GoogleHelper goog = new GoogleHelper();
		WorksheetEntry worksheet = new WorksheetEntry();
		
		//Create a new spreadsheet
		try {
			sheet = goog.createNewTestSheet(token);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Update the default worksheet
		try {
			worksheet = goog.updateTestWorksheet(sheet, token);
		} catch (IOException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MyBean result = new MyBean();
		result.setData(sheet.getId() + " | " + worksheet.getId());
		return result;
	}
	
	/**
	 * This endpoint writes timesheet data to Google Drive spreadsheet
	 * @throws GeneralSecurityException 
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "dummyGoogleFolder")
	public MyBean dummyGoogleFolder(
			@Named("token") String token, 
			@Named("month") int month, 
			@Named("year") int year) throws GeneralSecurityException, IOException 
	{

		//String email = validateEmailFromToken(token);
		//Timecard timecard = generateFakeTimecard(email, month, year);
		
		GoogleHelper goog = new GoogleHelper();
		
		//return timecard;
		File folder = new File();
		File subfolder = new File();
		File sharedsubfolder = new File();
		
		folder = goog.createNewTestFolder(token, "");
		subfolder = goog.createNewTestFolder(token, folder.getId());
		sharedsubfolder = goog.createNewTestFolder(token, "0BxN4AmtAyCpGfkU3YW9DMXQ3VGY2X2xHNHYycUZOQnRRbEdBVDhvRzVPcDhIYTRVck5oMkk");

		MyBean result = new MyBean();
		result.setData(folder.getId() + " | " + subfolder.getId() + " | " + sharedsubfolder.getId());
		return result;
	}

	private Timecard generateFakeTimecard(String email, int month, int year) {
		Timecard timecard = new Timecard();
		timecard.user = email;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
	
		if (email != "") {				
			for (int i = 0; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
				calendar.set(Calendar.DAY_OF_MONTH, i + 1);
				addFakeTimecardDay(timecard, calendar);
			}
		}
		return timecard;
	}

	private void addFakeTimecardDay(Timecard timecard, Calendar calendar) {
		TimecardDay day = new TimecardDay();

		day.setDate(calendar.getTime());
		switch (calendar.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
		case Calendar.TUESDAY:
		case Calendar.WEDNESDAY:
			day.setWorkHours(8.0);
			break;
		case Calendar.THURSDAY:
			day.setWorkHours(4.0);
			day.setSick(4.0);
			break;
		case Calendar.FRIDAY:
			day.setAnnual(8.0);
			day.setReportedHours(40.0);
			break;
		default:
			break;
		}

		timecard.days.add(day);
	}
	
	/**
	 * This endpoint makes up Harvest data
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "serviceAccountToken")
	public MyBean serviceAccountToken() 
	{
		String token = "";
		//try this with the service account
		try {
			token = new GoogleHelper().getServiceAccountCredential().getAccessToken();
			System.out.println("Service Account access_token: " + token);
		} catch (GeneralSecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MyBean response = new MyBean();
		response.setData(token);
		return response;
	}
	
	@ApiMethod(name = "findFolderId")
	public MyBean findFolderId(@Named("folderName") String folderName, @Named("accessToken") String accessToken)
	{
		String id = new GoogleHelper().findFolderId(accessToken, folderName);
		
		MyBean response = new MyBean();
		response.setData(id);
		return response;
	}
	
	@ApiMethod(name = "submitTimecard")
	public MyBean submitTimecard(@Named("month") Integer month, @Named("year") Integer year, @Named("accessToken") String accessToken)
	{
		GoogleHelper goog = new GoogleHelper();
		
		//get email from token
		String email = goog.validateEmailFromToken(accessToken);		
		
		//get service account credential
		GoogleCredential serviceCred;
		try {
			serviceCred = goog.getServiceAccountCredential();
		} catch (GeneralSecurityException | IOException e) {
			serviceCred = null;
			e.printStackTrace();
		}
		
		//get drive service with service account
		Drive drive = goog.getDriveService(serviceCred);
		
		//find or create root folder
		//if new, share with group
		String rootFolderTitle = "Timecards";
		String rootFolderId = goog.findFolderId(serviceCred.getAccessToken(), rootFolderTitle, null);
		if(rootFolderId == ""){
			rootFolderId = goog.createNewDriveFolder(drive, rootFolderTitle, null).getId();
			goog.insertPermission(drive, rootFolderId, "paperless-timesheet-test@googlegroups.com", AccountTypes.group, FileRoles.writer);
		}
		
		//find or create year folder
		//if new, share with group
		String yearFolderTitle = year.toString();
		String yearFolderId = goog.findFolderId(serviceCred.getAccessToken(), yearFolderTitle, rootFolderId);
		if(yearFolderId == ""){
			yearFolderId = goog.createNewDriveFolder(drive, yearFolderTitle, rootFolderId).getId();
			goog.insertPermission(drive, yearFolderId, "paperless-timesheet-test@googlegroups.com", AccountTypes.group, FileRoles.writer);
		}
		
		//find or create month folder
		//if new, share with group
		Calendar cal = Calendar.getInstance();
		cal.set(year, month - 1, 1);
		String monthFolderTitle = new SimpleDateFormat("MMMM").format(cal.getTime());
		String monthFolderId = goog.findFolderId(serviceCred.getAccessToken(), monthFolderTitle, yearFolderId);
		if(monthFolderId == ""){
			monthFolderId = goog.createNewDriveFolder(drive, monthFolderTitle, yearFolderId).getId();
			goog.insertPermission(drive, monthFolderId, "paperless-timesheet-test@googlegroups.com", AccountTypes.group, FileRoles.writer);
		}
		
		//find and delete existing timecard sheet file
		String timesheetId = goog.findSheetId(serviceCred.getAccessToken(), email, monthFolderId);
		if(timesheetId != null && timesheetId.length() > 0){
			goog.deleteFile(drive, timesheetId);
		}			
		
		//create new sheet file
		//share with group and supervisor
		String timesheetTitle = email + "_(Pending)";
		timesheetId = goog.createNewDriveSheet(drive, timesheetTitle, monthFolderId, 
				"Date,Work Hours,Annual,Sick,Holiday,Other Leave,Comp Used,Reported Hours, OT Earned, ST Hours,Shift Diff, On Call,Base,Callback").getId();
		goog.insertPermission(drive, timesheetId, "paperless-timesheet-test@googlegroups.com", AccountTypes.group, FileRoles.writer);
		goog.insertPermission(drive, timesheetId, email, AccountTypes.user, FileRoles.commenter);
		
		//get timecard data
		Timecard timecard = generateFakeTimecard(email, month, year);
		
		//insert data rows into worksheet
		SpreadsheetService service = goog.getServiceAccountSpreadsheetService();
		SpreadsheetEntry timecardSheet = goog.getSpreadsheetFromFileId(timesheetId, service);
		WorksheetEntry worksheet = goog.getDefaultWorksheet(service, timecardSheet);
		for (TimecardDay day : timecard.days) {
			goog.insertListRow(worksheet, service, day.generateDayData());		
		}
		
		MyBean response = new MyBean();
		response.setData("Success");
		return response;
	}

}
