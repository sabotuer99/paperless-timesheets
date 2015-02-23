package gov.wyo.paperless;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
		
		File fileMetadata = new File();
		fileMetadata.setTitle("Test File");
		fileMetadata.setMimeType("application/vnd.google-apps.spreadsheet");
		
		String empty = "";
		InputStreamContent mediaContent =
			    new InputStreamContent("text/csv",
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
	
	public WorksheetEntry addWorksheet(File sheetFile, String accessToken) throws IOException, ServiceException, MalformedURLException
	{	
		SpreadsheetService service = new SpreadsheetService(Constants.APPLICATION_NAME);
			
		String accessQuery = "?access_token=" + accessToken;
		URL feedUrl = new URL("https://spreadsheets.google.com/feeds/worksheets/" 
		           + sheetFile.getId() + "/private/full" + accessQuery);
		SpreadsheetFeed feed = service.getFeed(feedUrl, SpreadsheetFeed.class);		
		
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		SpreadsheetEntry spreadsheet = spreadsheets.get(0);
		WorksheetEntry worksheet = new WorksheetEntry();
		worksheet.setTitle(new PlainTextConstruct("New Test Worksheet"));
		worksheet.setColCount(10);
		worksheet.setRowCount(20);
		
		URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
		WorksheetEntry wsResult = service.insert(worksheetFeedUrl, worksheet);
			
		return wsResult;
	}
	
}
