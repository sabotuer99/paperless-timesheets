package gov.wyo.paperless;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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

public class GoogleDriveHelper {
	public File CreateNewSheet(String accessToken) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
		
		File fileMetadata = new File();
		fileMetadata.setTitle("Test File");
		
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
		insert.setConvert(true);
		File sheet = insert.execute();
		
		return sheet;
		
	}
	
	public File CreateNewFolder(String accessToken, String parentId) throws IOException{
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
		
/*		if(!parentId.isEmpty())
		{
			ParentReference newParent = new ParentReference();
		    newParent.setId(parentId);
		    drive.parents().insert(folder.getId(), newParent).execute();
		}*/
		
		return folder;
		
	}
}
