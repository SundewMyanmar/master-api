package com.sdm.payment.service;

import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

@Service
public class PaymentService {
    private SSLContext trustAllCerts() throws NoSuchAlgorithmException, KeyManagementException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        return sc;
    }

    public String requestApi_POST_SSL(URL API_URL, String jsonString, String tokenString) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        SSLContext sc = this.trustAllCerts();

        //SSL Socket Factory
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        HttpsURLConnection _CONNECTION = (HttpsURLConnection) API_URL.openConnection();
        _CONNECTION.setDoOutput(true);
        _CONNECTION.setRequestMethod("POST");
        _CONNECTION.setRequestProperty("Accept", "application/json");
        _CONNECTION.setRequestProperty("Content-Type", "application/json; utf-8");
        if (tokenString != null) {
            _CONNECTION.setRequestProperty("Authorization", tokenString);
        }

        return requestApi_POST(_CONNECTION, jsonString);
    }

    public String requestApi_POST(URL API_URL, String jsonString, String tokenString) throws IOException {
        HttpURLConnection _CONNECTION = (HttpURLConnection) API_URL.openConnection();
        _CONNECTION.setDoOutput(true);
        _CONNECTION.setRequestMethod("POST");
        _CONNECTION.setRequestProperty("Accept", "application/json");
        _CONNECTION.setRequestProperty("Content-Type", "application/json; utf-8");
        if (tokenString != null) {
            _CONNECTION.setRequestProperty("Authorization", tokenString);
        }

        return requestApi_POST(_CONNECTION, jsonString);
    }

    public String requestApi_POST(HttpURLConnection _CONNECTION, String jsonString) throws IOException {
        try (OutputStream os = _CONNECTION.getOutputStream()) {
            byte[] input = jsonString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (_CONNECTION.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + _CONNECTION.getResponseCode());
        }

        BufferedReader RESPONSE_BUFFER = new BufferedReader(new InputStreamReader(_CONNECTION.getInputStream()));
        String result = "", output = "";

        while ((output = RESPONSE_BUFFER.readLine()) != null) {
            result += output;
        }
        RESPONSE_BUFFER.close();
        _CONNECTION.disconnect();

        return result;
    }
}
