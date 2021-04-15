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
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration("file:src/main/webapp/WEB-INF/HelloWeb-servlet.xml")
public class LoadGoogleSheetIntoDB {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

//    @Autowired
//    private SessionFactory sessionFactory;

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = LoadGoogleSheetIntoDB.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    @Test
    public void testSampleService() throws GeneralSecurityException, IOException {
        System.out.println("test test");
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1568z5bdwt8kkXcH4iYjWYcGQQTJWvS-Z7xlShP9X0VY";
        final String range = "Finance!A2:G176"; //A2:G176
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            System.out.println("Major");

            try{
                Class.forName("com.mysql.jdbc.Driver");
                Connection conn= DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/FinData?useSSL=false&amp;serverTimezone=UTC","ywang","ouafahwafa79");
                // the mysql insert statement
                String query = " insert into buy_sell_btc_usd (btc_usd_google_trends, buy_bitcoin_google_trends, date, decision, diff_yesterday_today, percentage_btc_usd_buy_bitcoin, price) values (?, ?, ?, ?, ?,?,?)";

                // create the mysql insert preparedstatement
                PreparedStatement preparedStatement = null;

                for (List row : values) {
                    // Print columns A and E, which correspond to indices 0 and 4.
                    System.out.printf("%s, %s, %s, %s, %s, %s, %s\n", row.get(0), row.get(1), row.get(2), row.get(3), row.get(4), row.get(5), row.get(6));
//                    BuySellBtcUsd buySellBtcUsd = new BuySellBtcUsd();
//                    buySellBtcUsd.setDate(row.get(0).toString());
//                    buySellBtcUsd.setBtcUsdGoogleTrends(Integer.parseInt(row.get(1).toString()));
//                    buySellBtcUsd.setBuyBitcoinGoogleTrends(Integer.parseInt(row.get(2).toString()));
//                    buySellBtcUsd.setPrice(Double.parseDouble(row.get(3).toString()));
//                    buySellBtcUsd.setPercentageBtcUsdBuyBitcoin(Double.parseDouble(row.get(4).toString()));
//                    buySellBtcUsd.setDiffYesterdayAndToday(Integer.parseInt(row.get(6).toString()));
//                    buySellBtcUsd.setDecision(row.get(5).toString());

                    preparedStatement = conn.prepareStatement(query);
                    preparedStatement.setInt(1, Integer.parseInt(row.get(1).toString()));
                    preparedStatement.setInt(2, Integer.parseInt(row.get(2).toString()));
                    preparedStatement.setString(3, row.get(0).toString());
                    preparedStatement.setString(4, row.get(5).toString());
                    preparedStatement.setInt(5, Integer.parseInt(row.get(6).toString()));
                    String percentageString = row.get(4).toString().substring(0, row.get(4).toString().length()-1);
                    preparedStatement.setDouble(6, Double.parseDouble(percentageString));
                    preparedStatement.setDouble(7, Double.parseDouble(row.get(3).toString()));

                    preparedStatement.execute();

                }
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        }
}
