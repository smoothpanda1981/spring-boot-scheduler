package com.yan.wang.googlesheet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleSheetService {

	@Autowired
	private GoogleSheetRepository googleSheetRepository;

	public String getGoogleSheetContent() {
		String result = "";
		System.out.println("2");

		try {
			NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
			String APPLICATION_NAME = "Google Sheets API Java Quickstart";
			// Build a new authorized API client service.
			String spreadsheetId = "1wfUwWiFrkC1rdkuRKwjWkLFdOuMTbVoMgWfag60nUVI-Z7xlShP9X0VY";
			String range = "Calcul!G4:I"; //A2:G176
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, JSON_FACTORY)).setApplicationName(APPLICATION_NAME).build();

			ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
			List<List<Object>> values = response.getValues();
			if (values == null || values.isEmpty()) {
				System.out.println("No data found.");
			} else {
				System.out.println("Major");

				for (List row : values) {
					System.out.printf("%s, %s, %s\n", row.get(0), row.get(1), row.get(2));
					result = row.get(0) + " - " + row.get(1) + " - " + row.get(2);
				}
			}
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//return googleSheetRepository.getGoogleSheetContent();
		return result;
	}

	private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT, JsonFactory JSON_FACTORY) {
		Credential credential = null;
		try {
			String TOKENS_DIRECTORY_PATH = "tokens";
			List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

			String credientialjson = "{\"installed\":{\"client_id\":\"680031201316-ctb077hvnmdqmkv5d5fi7fb7c38655na.apps.googleusercontent.com\",\"project_id\":\"yanweb1-1618434884982\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"33lc2miI2DdrwqxaITHh5S5F\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}";
			//String credientialjson = "{\"installed\":{\"client_id\":\"680031201316-ctb077hvnmdqmkv5d5fi7fb7c38655na.apps.googleusercontent.com\",\"project_id\":\"yanweb1-1618434884982\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"33lc2miI2DdrwqxaITHh5S5F\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://159.89.129.64\"]}}";
			InputStream in = new ByteArrayInputStream(credientialjson.getBytes());
			if (in == null) {
				System.out.println("inputStream is null");
			} else {
				System.out.println("inputStream not null");
			}
			GoogleClientSecrets clientSecrets = null;
			clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
			// Build flow and trigger user authorization request.

			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
					.setDataStoreFactory(new MemoryDataStoreFactory())
					.setAccessType("offline")
					.build();

//			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//					.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//					.setAccessType("offline")
//					.build();

			LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
			credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return credential;
	}
}
