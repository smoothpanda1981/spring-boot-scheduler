package com.yan.wang.findata;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.yan.wang.account.Account;
import com.yan.wang.account.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Service
public class BuySellBtcUsdService {
	
	@Autowired
	private BuySellBtcUsdRepository buySellBtcUsdRepository;

	@PostConstruct
	protected void initialize() {
		System.out.println("test test");

		try {
			NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
			String APPLICATION_NAME = "Google Sheets API Java Quickstart";
			// Build a new authorized API client service.
			String spreadsheetId = "1568z5bdwt8kkXcH4iYjWYcGQQTJWvS-Z7xlShP9X0VY";
			String range = "Finance!A2:G"; //A2:G176
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, JSON_FACTORY)).setApplicationName(APPLICATION_NAME).build();

			ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
			List<List<Object>> values = response.getValues();
			if (values == null || values.isEmpty()) {
				System.out.println("No data found.");
			} else {
				System.out.println("Major");

				for (List row : values) {
					System.out.printf("%s, %s, %s, %s, %s, %s, %s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5), row.get(6));
					BuySellBtcUsd buySellBtcUsd = new BuySellBtcUsd();
					buySellBtcUsd.setDate(row.get(0).toString());
					buySellBtcUsd.setBtcUsdGoogleTrends(Integer.parseInt(row.get(1).toString()));
					buySellBtcUsd.setBuyBitcoinGoogleTrends(Integer.parseInt(row.get(2).toString()));
					buySellBtcUsd.setPrice(Double.parseDouble(row.get(3).toString()));
					buySellBtcUsd.setPercentageBtcUsdBuyBitcoin(Double.parseDouble(row.get(4).toString().substring(0, row.get(4).toString().length() - 1)));
					buySellBtcUsd.setDiffYesterdayAndToday(Integer.parseInt(row.get(6).toString()));
					buySellBtcUsd.setDecision(row.get(5).toString());
					buySellBtcUsdRepository.saveBuySellBtcUsd(buySellBtcUsd);
				}
			}
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Credential getCredentials(NetHttpTransport HTTP_TRANSPORT, JsonFactory JSON_FACTORY) {
		Credential credential = null;
		try {
			String TOKENS_DIRECTORY_PATH = "tokens";
			List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

			String credientialjson = "{\"installed\":{\"client_id\":\"680031201316-ctb077hvnmdqmkv5d5fi7fb7c38655na.apps.googleusercontent.com\",\"project_id\":\"yanweb1-1618434884982\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://oauth2.googleapis.com/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"33lc2miI2DdrwqxaITHh5S5F\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}";
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
					.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
					.setAccessType("offline")
					.build();
			LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
			credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return credential;
	}

	public List<BuySellBtcUsd> getListOfBuySellBtcUsd() {
		System.out.println("2");
		return buySellBtcUsdRepository.getListOfBuySellBtcUsd();
	}
}
