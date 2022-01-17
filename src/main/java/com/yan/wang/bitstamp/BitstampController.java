package com.yan.wang.bitstamp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yan.wang.findata.BuySellBtcUsd;
import com.yan.wang.findata.BuySellBtcUsdService;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Controller
public class BitstampController {

    @GetMapping("/bitstamp")
    public ModelAndView getGeneralData() {

        try {
            Properties props = new Properties();
            props.load(new FileInputStream("/tmp/bitstamp/bitstampapi.properties"));

            String result = getBalance(props).replace(" ", "");

            String subResult = result.substring(1, result.length()-1);
            String str[] = subResult.split(",");
            List<String> arrayList = new ArrayList<String>();
            arrayList = Arrays.asList(str);
            List<Balance> balanceList = new ArrayList<Balance>();
            for (String s : arrayList) {
                String tab[] = s.split(":");
                String name = tab[0].substring(1, tab[0].length()-1);
                String value = tab[1].substring(1, tab[1].length()-1);
                if (name.contains("_balance")) {
                    if (!value.equals("0.00000000") && !value.equals("0.00000") && !value.equals("0.00") && !value.equals("0")) {
                        Balance balance = new Balance();
                        balance.setName(name);
                        balance.setValue(value);
                        balanceList.add(balance);
                    }
                }
            }

            List<Balance> balanceList2 = new ArrayList<Balance>();
            for (Balance balance : balanceList) {
                if (!balance.getName().contains("usd") && !balance.getName().contains("eur")) {
                    Balance balance2 = new Balance();
                    balance2.setName(balance.getName());
                    balance2.setValue(balance.getValue());
                    balance2.setCurrentPrice(getCurrentPrice(balance));
                    balanceList2.add(balance2);
                } else {
                    balance.setCurrentPrice("-");
                    balanceList2.add(balance);
                }
            }

            List<Balance> balanceList3 = new ArrayList<Balance>();
            for (Balance balance : balanceList2) {
                Balance balance3 = new Balance();
                balance3.setName(balance.getName());
                balance3.setValue(balance.getValue());
                balance3.setCurrentPrice(balance.getCurrentPrice());
                balance3.setPaidPrice(getPaidPrice(props, balance.getName()));
                balanceList3.add(balance3);
            }

            List<Balance> balanceList4 = new ArrayList<Balance>();
            for (Balance balance : balanceList3) {
                Balance balance4 = new Balance();
                balance4.setName(balance.getName());
                balance4.setValue(balance.getValue());
                balance4.setCurrentPrice(balance.getCurrentPrice());
                balance4.setPaidPrice(balance.getPaidPrice());
                balance4.setProfitOrLoss(computeProfitOrLossCurrentPrice(balance.getCurrentPrice(), balance.getPaidPrice()));
                balanceList4.add(balance4);
            }

            ModelAndView modelAndView = new ModelAndView("bitstamp/bitstamp");
            modelAndView.addObject("balanceList", balanceList4);
            return modelAndView;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getBalance(Properties props) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException {
        String apiKey = String.format("%s %s", "BITSTAMP", props.getProperty("api.key"));
        String apiKeySecret = props.getProperty("api.secret");
        String httpVerb = "POST";
        String urlHost = "www.bitstamp.net";
        String urlPath = "/api/v2/balance/";
        String urlQuery = "";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        String contentType = "application/x-www-form-urlencoded";
        String version = "v2";
        String payloadString = "offset=1";
        String signature = apiKey +
                httpVerb +
                urlHost +
                urlPath +
                urlQuery +
                contentType +
                nonce +
                timestamp +
                version +
                payloadString;

        SecretKeySpec secretKey = new SecretKeySpec(apiKeySecret.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] rawHmac = mac.doFinal(signature.getBytes());
        signature = new String(Hex.encodeHex(rawHmac)).toUpperCase();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.bitstamp.net/api/v2/balance/"))
                .POST(HttpRequest.BodyPublishers.ofString(payloadString))
                .setHeader("X-Auth", apiKey)
                .setHeader("X-Auth-Signature", signature)
                .setHeader("X-Auth-Nonce", nonce)
                .setHeader("X-Auth-Timestamp", timestamp)
                .setHeader("X-Auth-Version", version)
                .setHeader("Content-Type", contentType)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Status code not 200");
        }

        String serverSignature = response.headers().map().get("x-server-auth-signature").get(0);
        String responseContentType = response.headers().map().get("Content-Type").get(0);
        String stringToSign = nonce + timestamp + responseContentType + response.body();

        mac.init(secretKey);
        byte[] rawHmacServerCheck = mac.doFinal(stringToSign.getBytes());
        String newSignature = new String(Hex.encodeHex(rawHmacServerCheck));

        if (!newSignature.equals(serverSignature)) {
            throw new RuntimeException("Signatures do not match");
        }

        return response.body();
    }

    private String getCurrentPrice(Balance balance) throws IOException {
        // Create a neat value object to hold the URL
        String crypto = balance.getName().replace("_balance", "");
        String composeUrl = "https://www.bitstamp.net/api/v2/ticker/" + crypto + "usd/";
        URL url = new URL(composeUrl);

        // Open a connection(?) on the URL(??) and cast the response(???)
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Now it's "open", we can set the request method, headers etc.
        connection.setRequestProperty("accept", "application/json");

        // This line makes the request
        InputStream responseStream = connection.getInputStream();

        // Manually converting the response body InputStream to CurrentPrice using Jackson
        ObjectMapper mapper = new ObjectMapper();
        CurrentPrice currentPrice = mapper.readValue(responseStream, CurrentPrice.class);

        // Finally we have the response
        return currentPrice.getLast();

    }

    private String getPaidPrice(Properties props, String name) {
        String result;
        String crypto = name.replace("_balance", "");
        String keySecondPart = ".paid.price";
        if (props.getProperty(crypto+keySecondPart) !=  null) {
            result = props.getProperty(crypto+keySecondPart);
        } else {
            result = "-";
        }
        return result;
    }

    private String computeProfitOrLossCurrentPrice(String currentPrice, String paidPrice) {
        String result;
        if (paidPrice.equals("-")) {
            result = "black";
        } else if (paidPrice.contains(",")) {
            String[] tab = paidPrice.split(",");
            Double[] tabD = new Double[tab.length];
            for (int i = 0; i < tab.length; i++) {
                tabD[i] = Double.parseDouble(tab[i]);
            }

            Double maxPaidPriceD = tabD[0];
            for (int counter = 1; counter < tabD.length; counter++) {
                if (tabD[counter] > maxPaidPriceD) {
                    maxPaidPriceD = tabD[counter];
                }
            }

            Double currentPriceD = Double.parseDouble(currentPrice);
            if (maxPaidPriceD > currentPriceD) {
                result = "red";
            } else if (maxPaidPriceD == currentPriceD) {
                result = "black";
            } else {
                result = "green";
            }
        } else {
            Double currentPriceD = Double.parseDouble(currentPrice);
            Double paidPriceD = Double.parseDouble(paidPrice);
            if (paidPriceD > currentPriceD) {
                result = "red";
            } else if (paidPriceD == currentPriceD) {
                result = "black";
            } else {
                result = "green";
            }
        }

        return result;
    }
}
