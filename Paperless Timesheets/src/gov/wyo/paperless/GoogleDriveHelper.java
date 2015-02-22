package gov.wyo.paperless;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class GoogleDriveHelper {
	public File CreateNewSheet(String accessToken) throws IOException{
		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
		
		File fileMetadata = new File();
		fileMetadata.setTitle("Test File");
		
		String empty = "";
		InputStreamContent mediaContent =
			    new InputStreamContent("application/vnd.google-apps.spreadsheet",
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
		return insert.execute();
	}
}
