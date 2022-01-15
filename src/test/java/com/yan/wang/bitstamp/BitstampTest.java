package com.yan.wang.bitstamp;

import com.yan.wang.account.Account;
import com.yan.wang.account.AccountRepository;
import com.yan.wang.account.AccountService;
import org.apache.commons.codec.binary.Hex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.ModelAndView;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitstampTest {


	@Test
	public void getAccountBalance() {
			System.out.println("test 1");

			String apiKey = String.format("%s %s", "BITSTAMP", "QQcYnUd7UrkQFQSSKaN5xbAHLv46EfdA");
			String apiKeySecret = "Ht7EuosIpKYzjqIzsgnQyuehfOQDmhut";
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

			try {
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

				System.out.println(response.body());

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
	}
}
