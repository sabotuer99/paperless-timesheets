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
import com.google.gdata.client.spreadsheet.*;
import com.google.gdata.data.*;
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

public class GoogleDriveHelper {
	
	public File createNewSheet(String accessToken) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
		
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
		
		File fileMetadata = new File();
		fileMetadata.setTitle("Test File " + strDate);
		fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
		
		String template = ",\n,";
		InputStreamContent mediaContent =
			    new InputStreamContent("text/csv",
			        new BufferedInputStream(new ByteArrayInputStream(template.getBytes(StandardCharsets.UTF_8))));		
		
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
		
		Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
		insert.setConvert(true);
		File sheet = insert.execute();
		return sheet;
		
	}
	
	public File createNewFolder(String accessToken, String parentId) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
		
		File fileMetadata = new File();
		fileMetadata.setTitle("Test Folder");
		if(!parentId.isEmpty())
		{
			fileMetadata.setTitle("Test SubFolder");
			ParentReference newParent = new ParentReference();
			newParent.setId(parentId);
			List<ParentReference> parents = new ArrayList<ParentReference>();
			parents.add(newParent);
			fileMetadata.setParents(parents);
		}
		fileMetadata.setMimeType("application/vnd.google-apps.folder");
		
		String empty = "";
		InputStreamContent mediaContent =
			    new InputStreamContent("application/json",
			        new BufferedInputStream(new ByteArrayInputStream(empty.getBytes(StandardCharsets.UTF_8))));		
		mediaContent.setLength(empty.length());
		
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
		
		Drive.Files.Insert insert = drive.files().insert(fileMetadata, mediaContent);
		File folder = insert.execute();
		
		return folder;		
	}
	
	public WorksheetEntry updateWorksheet(File sheetFile, String accessToken) throws IOException, ServiceException, MalformedURLException
	{	
		SpreadsheetService service = new SpreadsheetService(Constants.APPLICATION_NAME);
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
		service.setOAuth2Credentials(credential);	
		
		// Define the URL to request.  This should never change.
	    URL SPREADSHEET_FEED_URL = new URL(
	        "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

	    // Make a request to the API and get all spreadsheets.
	    SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL,
	        SpreadsheetFeed.class);
	    List<SpreadsheetEntry> spreadsheets = feed.getEntries();	
		
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
	
	public File transferOwnership(String accessToken, String newOwner){
		return new File();
	}
	
	public String getServiceAccoutAccessToken() throws GeneralSecurityException, IOException{
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		
		Collection<String> scopes = new ArrayList<String>();
		scopes.add("https://www.googleapis.com/auth/userinfo.profile");
		scopes.add("https://www.googleapis.com/auth/userinfo.email");
		//scopes.add("https://www.googleapis.com/auth/drive");
		//scopes.add("https://spreadsheets.google.com/feeds/");	
				
		InputStream keyStream = GoogleDriveHelper.class.getResourceAsStream("key.p12");
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
		
		return credential.getAccessToken();
	}
}
