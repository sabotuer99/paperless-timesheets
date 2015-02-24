package gov.wyo.paperless;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.labs.repackaged.org.json.JSONTokener;
import com.google.api.services.drive.model.File;
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
		String harvestData = new HttpHelper().excutePost(targetURL,
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

		String email = validateEmailFromToken(token);
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
		GoogleDriveHelper goog = new GoogleDriveHelper();
		WorksheetEntry worksheet = new WorksheetEntry();
		
		//Create a new spreadsheet
		try {
			sheet = goog.createNewSheet(token);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Update the default worksheet
		try {
			worksheet = goog.updateWorksheet(sheet, token);
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
	 * 
	 * @throws ParseException
	 */
	@ApiMethod(name = "dummyGoogleFolder")
	public MyBean dummyGoogleFolder(
			@Named("token") String token, 
			@Named("month") int month, 
			@Named("year") int year) 
	{

		//String email = validateEmailFromToken(token);
		//Timecard timecard = generateFakeTimecard(email, month, year);
		
		GoogleDriveHelper goog = new GoogleDriveHelper();
		
		//return timecard;
		File folder = new File();
		File subfolder = new File();
		File sharedsubfolder = new File();
		
		try {
			folder = goog.createNewFolder(token, "");
			subfolder = goog.createNewFolder(token, folder.getId());
			sharedsubfolder = goog.createNewFolder(token, "0BxN4AmtAyCpGfnljblM5d3N6aWh3VlR4ZWR4eDFiN2phR0NUVGdtVV9ING02dHFnc1pteGM");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	private String validateEmailFromToken(String token) {
		String validateToken = new HttpHelper().excutePost(
				"https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="
						+ token, "");
		String email = "";

		try {
			JSONObject json = new JSONObject(new JSONTokener(validateToken));

			if (json.has("email")) {
				email = json.getString("email");
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return email;
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
			token = new GoogleDriveHelper().getServiceAccoutAccessToken();
			System.out.println("Service Account access_token: " + token);
		} catch (GeneralSecurityException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		MyBean response = new MyBean();
		response.setData(token);
		return response;
	}

}
