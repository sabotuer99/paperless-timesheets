package gov.wyo.paperless;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.appengine.labs.repackaged.org.json.JSONTokener;
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

public class GoogleHelper {
	
	public enum FileRoles {
		owner,
		reader,
		writer,
		commenter
	}
	
	public enum AccountTypes {
		user,
		group,
		domain,
		anyone
	}
	
	public File createNewTestSheet(String accessToken) throws IOException{

		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);		
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
		String title = "Test File " + strDate;
		String metaMimeType = "application/vnd.google-apps.spreadsheet";
		String contentMimeType = "text/csv";
		String content = ",\n,";
		Drive drive = getDriveService(credential);			
		
		return createNewDriveFile(drive, title, metaMimeType, contentMimeType, content, null, true);
		
	}
	
	public File createNewTestFolder(String accessToken, String parentId){
		
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);			    
		String title = "Test Folder";
		String metaMimeType = "application/vnd.google-apps.folder";
		String contentMimeType = "application/json";
		String content = ",\n,";
		Drive drive = getDriveService(credential);			
		
		if(parentId != null && !parentId.isEmpty())
		{
			title = "Test SubFolder";		
		}
		
		return createNewDriveFile(drive, title, metaMimeType, contentMimeType, content, parentId, true);
	}
	
	public WorksheetEntry updateTestWorksheet(File sheetFile, String accessToken) throws IOException, ServiceException, MalformedURLException
	{	
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);		
		SpreadsheetService service = getSpreadsheetService(credential);			
		List<SpreadsheetEntry> spreadsheets = getAllSpreadsheets(service);	
		
		SpreadsheetEntry spreadsheet = spreadsheets.get(0);
		System.out.println(spreadsheet.getTitle().getPlainText());
	    
		// Get the first worksheet of the first spreadsheet.
	    // TODO: Choose a worksheet more intelligently based on your
	    // app's needs.
	    WorksheetFeed worksheetFeed = service.getFeed(
	        spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
	    List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
	    WorksheetEntry worksheet = worksheets.get(0);

	    // Update the local representation of the worksheet.
	    worksheet.setTitle(new PlainTextConstruct("Updated Worksheet"));
	    worksheet.setColCount(5);
	    worksheet.setRowCount(15);

	    // Send the local representation of the worksheet to the API for
	    // modification.
	    worksheet.update();
	    
		return worksheet;
	}
	
	public ListEntry insertListRow(WorksheetEntry worksheet, SpreadsheetService service, HashMap<String, String> rowValues) throws IOException, ServiceException {
		
		// Fetch the list feed of the worksheet.
	    URL listFeedUrl = worksheet.getListFeedUrl();

	    // Create a local representation of the new row.
	    ListEntry row = new ListEntry();
	    
	    for (String key : rowValues.keySet())
	    {
	    	String value = rowValues.get( key );
	    	row.getCustomElements().setValueLocal(key, value);
	    }
	    // Send the new row to the API for insertion.
	    row = service.insert(listFeedUrl, row); 
	    return null;
	}
	
	public File createNewDriveFile(Drive drive, String title, String metaMimeType, String contentMimeType, String content, String parentId, boolean doConvert){	
		
		File fileMetadata = new File();
		fileMetadata.setTitle(title);
		fileMetadata.setMimeType(metaMimeType);
		
		//set parents if present
		if(parentId != null && !parentId.isEmpty())
		{
			ParentReference newParent = new ParentReference();
			List<ParentReference> parents = new ArrayList<ParentReference>();
			
			newParent.setId(parentId);		
			parents.add(newParent);
			fileMetadata.setParents(parents);
		}
		
		InputStreamContent mediaContent =
			    new InputStreamContent(contentMimeType,
			        new BufferedInputStream(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))));		
				
		File sheet = new File();
		try {
			Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
			insert.setConvert(doConvert);
			sheet = insert.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sheet;	
	}

	public Drive getDriveService(GoogleCredential credential) {
		HttpTransport transport = null;
		try {
			transport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		
		
		Drive drive = new Drive.Builder(transport, jsonFactory, credential)
        	.setApplicationName(Constants.APPLICATION_NAME)
        	.build();
		return drive;
	}
	
	public List<SpreadsheetEntry> getAllSpreadsheets(SpreadsheetService service)
			throws MalformedURLException, IOException, ServiceException {
		// Define the URL to request.  This should never change.
	    URL SPREADSHEET_FEED_URL = new URL(
	        "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

	    // Make a request to the API and get all spreadsheets.
	    SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL,
	        SpreadsheetFeed.class);
	    List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		return spreadsheets;
	}

	public SpreadsheetService getSpreadsheetService(GoogleCredential credential) {
		SpreadsheetService service = new SpreadsheetService(Constants.APPLICATION_NAME);		
		service.setOAuth2Credentials(credential);
		return service;
	}
	
	public SpreadsheetService getServiceAccountSpreadsheetService()
	{
		SpreadsheetService service = null;
		try {
			service = getSpreadsheetService(getServiceAccountCredential());
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return service;
	}
	
	/**
	   * Insert a new permission.
	   *
	   * @param service Drive API service instance.
	   * @param fileId ID of the file to insert permission for.
	   * @param value User or group e-mail address, domain name or {@code null}
	                  "default" type.
	   * @param type The value "user", "group", "domain" or "default".
	   * @param role The value "owner", "writer" or "reader".
	   * @return The inserted permission if successful, {@code null} otherwise.
	   */
 	public Permission insertPermission(Drive service, String fileId, String value, AccountTypes type, FileRoles role) {
	    Permission newPermission = new Permission();

	    newPermission.setValue(value);
	    newPermission.setType(type.toString());
	    newPermission.setRole(role.toString());
	    try {
	      return service.permissions().insert(fileId, newPermission).execute();
	    } catch (IOException e) {
	      System.out.println("An error occurred: " + e);
	    }
	    return null;
	  }
	
	public GoogleCredential getServiceAccountCredential() throws GeneralSecurityException, IOException{
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		
		Collection<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/userinfo.profile");
		scopes.add("https://www.googleapis.com/auth/userinfo.email");
		scopes.add("https://www.googleapis.com/auth/drive");
		scopes.add("https://spreadsheets.google.com/feeds/");	
				
		InputStream keyStream = GoogleHelper.class.getResourceAsStream("key.p12");
		KeyStore keystore = KeyStore.getInstance("PKCS12");
		String p12Password = "notasecret";
		keystore.load(keyStream, p12Password.toCharArray());
		PrivateKey key = (PrivateKey)keystore.getKey("privatekey", p12Password.toCharArray());
		
		// Build service account credential.
		GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
		    .setJsonFactory(jsonFactory)
		    .setServiceAccountId(Constants.SERVICE_ACCOUNT_ID)
		    .setServiceAccountScopes(scopes)
		    .setServiceAccountPrivateKey(key)
		    //.setServiceAccountUser("troy.whorten@wyo.gov")
		    .build();
		
		return credential;
	}

	
	public Drive getServiceAccountDriveService(){
		Drive drive = null;	
		try {
			drive = getDriveService(getServiceAccountCredential());
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return drive;
	}
	
	public File createNewServiceAcccountSheet(){
		return null;}
	
	
	public String validateEmailFromToken(String token) {
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

}
