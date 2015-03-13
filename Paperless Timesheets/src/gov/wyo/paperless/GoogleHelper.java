package gov.wyo.paperless;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

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
import com.google.gdata.data.spreadsheet.*;
import com.google.gdata.util.*;

public class GoogleHelper {

	public enum FileRoles {
		owner, reader, writer, commenter
	}

	public enum AccountTypes {
		user, group, domain, anyone
	}

	public File createNewTestSheet(String accessToken) throws IOException {

		GoogleCredential credential = new GoogleCredential()
				.setAccessToken(accessToken);
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// dd/MM/yyyy
		Date now = new Date();
		String strDate = sdfDate.format(now);
		String title = "Test File " + strDate;
		String metaMimeType = "application/vnd.google-apps.spreadsheet";
		String contentMimeType = "text/csv";
		String content = ",\n,";
		Drive drive = getDriveService(credential);
		return createNewDriveFile(drive, title, metaMimeType, contentMimeType,
				content, null, true);

	}

	public File createNewTestFolder(String accessToken, String parentId) {

		GoogleCredential credential = new GoogleCredential()
				.setAccessToken(accessToken);
		String title = "Test Folder";
		String metaMimeType = "application/vnd.google-apps.folder";
		String contentMimeType = "application/json";
		String content = ",\n,";
		Drive drive = getDriveService(credential);

		if (parentId != null && !parentId.isEmpty()) {
			title = "Test SubFolder";
		}

		return createNewDriveFile(drive, title, metaMimeType, contentMimeType,
				content, parentId, true);
	}

	public WorksheetEntry updateTestWorksheet(File sheetFile, String accessToken)
			throws IOException, ServiceException, MalformedURLException {

		GoogleCredential credential = new GoogleCredential()
				.setAccessToken(accessToken);
		SpreadsheetService service = getSpreadsheetService(credential);
		List<SpreadsheetEntry> spreadsheets = getAllSpreadsheets(service);

		SpreadsheetEntry spreadsheet = spreadsheets.get(0);
		System.out.println(spreadsheet.getTitle().getPlainText());

		// Get the first worksheet of the first spreadsheet.
		// Choose a worksheet more intelligently based on your
		// app's needs.
		WorksheetEntry worksheet = getDefaultWorksheet(service, spreadsheet);

		changeWorksheetDimensions(worksheet, 5, 15);

		return worksheet;
	}

	public void changeWorksheetDimensions(WorksheetEntry worksheet, Integer colCount, Integer rowCount){
		// Update the local representation of the worksheet.
		worksheet.setColCount(colCount);
		worksheet.setRowCount(rowCount);

		// Send the local representation of the worksheet to the API for
		// modification.
		try {
			worksheet.update();
		} catch (IOException | ServiceException e) {
			e.printStackTrace();
		}
	}

	public WorksheetEntry getDefaultWorksheet(SpreadsheetService service, SpreadsheetEntry spreadsheet) {
		WorksheetEntry worksheet;
		try {
			WorksheetFeed worksheetFeed = service.getFeed( spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
			List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
			worksheet = worksheets.get(0);
		} catch (IOException | ServiceException e) {
			worksheet = null;
			e.printStackTrace();
		}
		return worksheet;
	}

	//this doesn't seem to work...
	public ListEntry insertListRow(WorksheetEntry worksheet, SpreadsheetService service, HashMap<String, String> rowValues){

		// Create a local representation of the new row.
		ListEntry row = new ListEntry();;
		try {
			// Fetch the list feed of the worksheet.
			URL listFeedUrl = worksheet.getListFeedUrl();
			//System.out.print(listFeedUrl.toString());		
			
			for (String key : rowValues.keySet()) {
				String value = rowValues.get(key);
				key = key.replace(" ", "").toLowerCase();
				//System.out.print(key + "|" + value + "\n");
				row.getCustomElements().setValueLocal(key, value);
			}
			// Send the new row to the API for insertion.
			row = service.insert(listFeedUrl, row);
		} catch (IOException | ServiceException e) {
			e.printStackTrace();
		}
		System.out.print(row.toString() + "\n");
		return row;
	}
	
	public void updateRowCells(WorksheetEntry worksheet, SpreadsheetService service, Integer rowNumber, LinkedHashMap<String, String> cellValues){
		
	    try {
	    	// Fetch the cell feed of the worksheet.
		    URL cellFeedUrl = worksheet.getCellFeedUrl();
			CellFeed cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
			
			ArrayList<String> keys = new ArrayList<String>(cellValues.keySet());
		
			for (CellEntry cell : cellFeed.getEntries()) {
			    if(cell.getCell().getRow() == rowNumber){
			    	String key = keys.get(cell.getCell().getCol());
			    	String value = cellValues.get(key);
			    	cell.changeInputValueLocal(value);
			        cell.update();
			    }
			}		
		} catch (IOException | ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public File createNewDriveFile(Drive drive, String title,
			String metaMimeType, String contentMimeType, String content,
			String parentId, boolean doConvert) {

		File fileMetadata = new File();
		fileMetadata.setTitle(title);
		fileMetadata.setMimeType(metaMimeType);

		// set parents if present
		if (parentId != null && !parentId.isEmpty()) {
			ParentReference newParent = new ParentReference();
			List<ParentReference> parents = new ArrayList<ParentReference>();

			newParent.setId(parentId);
			parents.add(newParent);
			fileMetadata.setParents(parents);
		}

		InputStreamContent mediaContent = new InputStreamContent(
				contentMimeType, new BufferedInputStream(
						new ByteArrayInputStream(
								content.getBytes(StandardCharsets.UTF_8))));

		File sheet;
		try {
			Drive.Files.Insert insert = drive.files().insert(fileMetadata,
					mediaContent);
			insert.setConvert(doConvert);
			sheet = insert.execute();
		} catch (IOException e) {
			sheet = new File();
			e.printStackTrace();
		}
		return sheet;
	}

	public File createNewDriveFolder(Drive drive, String title,	String parentId){
		return createNewDriveFile(drive, title, "application/vnd.google-apps.folder", "application/json", "", parentId, false);
	}
	
	public File createNewDriveSheet(Drive drive, String title,	String parentId, String csvContent){
		return createNewDriveFile(drive, title, "application/vnd.google-apps.spreadsheet", "text/csv", csvContent, parentId, true);
	}
	
	public Drive getDriveService(GoogleCredential credential) {
		HttpTransport transport;
		try {
			transport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			transport = null;
			e.printStackTrace();
		}

		JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		Drive drive = new Drive.Builder(transport, jsonFactory, credential)
				.setApplicationName(Constants.APPLICATION_NAME).build();
		return drive;
	}

	public List<SpreadsheetEntry> getAllSpreadsheets(SpreadsheetService service)
			throws MalformedURLException, IOException, ServiceException {
		// Define the URL to request. This should never change.
		URL SPREADSHEET_FEED_URL = new URL(Constants.SPREADSHEET_FEED);

		// Make a request to the API and get all spreadsheets.
		SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL,
				SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		return spreadsheets;
	}

	public SpreadsheetEntry getNewestSpreadsheetFromTitle(
			SpreadsheetService service, String title) {
		return getSpreadsheetsFromQuery(service, "title", title).get(0);
	}

	public SpreadsheetEntry getSpreadsheetFromFileId(String fileId, SpreadsheetService service){
		SpreadsheetEntry entry;
		try {
			URL entryUrl = new URL(Constants.SPREADSHEET_FEED + "/" + fileId);
			entry = service.getEntry(entryUrl, SpreadsheetEntry.class);
		} catch (IOException | ServiceException e) {
			entry = null;
			e.printStackTrace();
		}
		return entry;
	}
	
	public List<SpreadsheetEntry> getSpreadsheetsFromQuery(
			SpreadsheetService service, String field, String key) {
		// Define the URL to request. Append the query string
		URL SPREADSHEET_FEED_URL;
		SpreadsheetFeed feed;
		List<SpreadsheetEntry> spreadsheets;
		try {
			SPREADSHEET_FEED_URL = new URL(Constants.SPREADSHEET_FEED + "?"
					+ field + "=" + key);
			feed = service.getFeed(SPREADSHEET_FEED_URL, SpreadsheetFeed.class);
			spreadsheets = feed.getEntries();
		} catch (IOException | ServiceException e) {
			spreadsheets = null;
			e.printStackTrace();
		}

		return spreadsheets;
	}

	public SpreadsheetService getSpreadsheetService(GoogleCredential credential) {
		SpreadsheetService service = new SpreadsheetService(
				Constants.APPLICATION_NAME);
		service.setOAuth2Credentials(credential);
		return service;
	}

	public SpreadsheetService getServiceAccountSpreadsheetService() {
		SpreadsheetService service = getSpreadsheetService(getServiceAccountCredential());
		return service;
	}

	/**
	 * Insert a new permission.
	 *
	 * @param service
	 *            Drive API service instance.
	 * @param fileId
	 *            ID of the file to insert permission for.
	 * @param targetId
	 *            User or group e-mail address, domain name or {@code null}
	 *            "default" type.
	 * @param type
	 *            The value "user", "group", "domain" or "default".
	 * @param role
	 *            The value "owner", "writer" or "reader".
	 * @return The inserted permission if successful, {@code null} otherwise.
	 */
	public Permission insertPermission(Drive service, String fileId,
			String targetId, AccountTypes type, FileRoles role) {
		Permission newPermission = new Permission();

		newPermission.setValue(targetId);
		newPermission.setType(type.toString());
		
		if (role == FileRoles.commenter) {
			ArrayList<String> additionalRoles = new ArrayList<String>();
			additionalRoles.add(role.toString());
			newPermission.setRole(FileRoles.reader.toString());
			newPermission.setAdditionalRoles(additionalRoles);
		} else {
			newPermission.setRole(role.toString());
		}

		try {
			return service.permissions().insert(fileId, newPermission)
					.execute();
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
		}
		return null;
	}

	public GoogleCredential getServiceAccountCredential(){
		try {
			HttpTransport httpTransport = GoogleNetHttpTransport
					.newTrustedTransport();
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

			Collection<String> scopes = new ArrayList<String>();
			scopes.add("https://www.googleapis.com/auth/userinfo.profile");
			scopes.add("https://www.googleapis.com/auth/userinfo.email");
			scopes.add("https://www.googleapis.com/auth/drive");
			scopes.add("https://spreadsheets.google.com/feeds/");

			InputStream keyStream = GoogleHelper.class
					.getResourceAsStream("key.p12");
			KeyStore keystore = KeyStore.getInstance("PKCS12");
			String p12Password = "notasecret";
			keystore.load(keyStream, p12Password.toCharArray());
			PrivateKey key = (PrivateKey) keystore.getKey("privatekey",
					p12Password.toCharArray());

			// Build service account credential.
			GoogleCredential credential = new GoogleCredential.Builder()
					.setTransport(httpTransport).setJsonFactory(jsonFactory)
					.setServiceAccountId(Constants.SERVICE_ACCOUNT_ID)
					.setServiceAccountScopes(scopes)
					.setServiceAccountPrivateKey(key)
					// .setServiceAccountUser("troy.whorten@wyo.gov")
					.build();

			credential.refreshToken();
			return credential;
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block	
			e.printStackTrace();
			return null;
		}
	}

	public Drive getServiceAccountDriveService() {
		Drive drive = getDriveService(getServiceAccountCredential());

		return drive;
	}

	public File createNewServiceAcccountSheet() {
		// TODO fill in the code!
		return null;
	}

	public String validateEmailFromToken(String token) {
		String validateToken = new HttpHelper().executePost(
				"https://www.googleapis.com/oauth2/v1/tokeninfo?access_token="
						+ token, "");
		String email;

		try {
			JSONObject json = new JSONObject(new JSONTokener(validateToken));

			if (json.has("email")) {
				email = json.getString("email");
			} else {
				email = "";
			}

		} catch (Exception e) {
			email = "";
			e.printStackTrace();
		}
		return email;
	}

	public String findFolderId(String accessToken, String folderName) {
		return findFolderId(accessToken, folderName, null);
	}
	
	public String findFolderId(String accessToken, String folderName, String parentId){
		return findFileId(accessToken, folderName, parentId, "application/vnd.google-apps.folder", true );
	}
	
	public String findSheetId(String accessToken, String fileName, String parentId){
		return findFileId(accessToken, fileName, parentId, "application/vnd.google-apps.spreadsheet", false);
	}
	
	public String findFileId(String accessToken, String fileName, String parentId, String mimeType, boolean exactTitle) {
		String query;
		try {
			String titlePredicate = exactTitle ? "=" : "contains" ;
			//fileName = URLDecoder.decode(fileName, "UTF-8");
			String queryUnencoded = "mimeType = '"+ mimeType + "' and title " + titlePredicate + " '" + fileName + "'";
			
			
			if(parentId != null && parentId.length() > 0){
				queryUnencoded += " and '"+ parentId +"' in parents";
			}			
			
			query = URLEncoder.encode(queryUnencoded, "UTF-8");
			
			System.out.print(query);
			
		} catch (UnsupportedEncodingException e) {
			// query should be such that the result is empty
			query = new Random().toString();
			e.printStackTrace();
		}

		String url = "https://www.googleapis.com/drive/v2/files?access_token="
				+ accessToken + "&q=" + query;

		String fileList = "";
		for (int i = 0; fileList == "" && i < 5; i++) {
			try {
				fileList = new HttpHelper().sendGet(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.print(fileList);

		String id;

		try {
			JSONObject json = new JSONObject(new JSONTokener(fileList));

			if (json.has("items") && json.getJSONArray("items").length() > 0) {
				id = json.getJSONArray("items").getJSONObject(0)
						.getString("id");
			} else {
				id = "";
			}

		} catch (JSONException e) {
			id = "";
			e.printStackTrace();
		}

		return id;
	}

	public void deleteAllWorksheetRows(WorksheetEntry worksheet, SpreadsheetService service) {
			
		try {

			URL listFeedUrl = worksheet.getListFeedUrl();
			ListFeed listFeed  = service.getFeed(listFeedUrl, ListFeed.class);
			for (int i = 0; i < listFeed.getEntries().size(); i++) {
				ListEntry row = listFeed.getEntries().get(i);
				row.delete();
			}		

		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	public void deleteFile(Drive drive, String fileId){
		try {
			drive.files().delete(fileId).execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
