package com.sdm.core.util;

import com.sdm.core.exception.GeneralException;
import com.sdm.core.model.response.HttpResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Service
public class HttpRequestManager {
    private final String DEFAULT_LANG = "en-US,en;q=0.8";
    private final String USER_AGENT = "MasterApi; SUNDEW MYANMAR";
    private final String REFERRER = "https://www.sundewmyanmar.com/";
    private final int NETWORK_TIMEOUT = 60000;

    private void installSSL() {
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
        try {
            // Install the all-trusting trust manager
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            //SSL Socket Factory
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            HttpsURLConnection.setFollowRedirects(true);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
        }
    }

    private HttpURLConnection openConnection(URL url, boolean useSSL) throws IOException {
        HttpURLConnection connection;
        if (!useSSL) {
            HttpURLConnection.setFollowRedirects(true);
        } else {
            this.installSSL();
        }
        connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(this.NETWORK_TIMEOUT);
        connection.addRequestProperty("Accept-Language", this.DEFAULT_LANG);
        connection.addRequestProperty("User-Agent", this.USER_AGENT);
        connection.addRequestProperty("Referer", this.REFERRER);

        return connection;
    }

    public HttpResponse getRedirectHttpRequest(String urlString) throws MalformedURLException {
        URL url = new URL(urlString);
        if ("https".equals(url.getProtocol())) {
            return this.formGetRequest(url, null, true);
        }
        return this.formGetRequest(url, null, false);
    }

    public HttpResponse getHttpResult(HttpURLConnection connection, String method, String body, String authToken) throws IOException {
        if (!Globalizer.isNullOrEmpty(authToken)) {
            connection.setRequestProperty("Authorization", authToken);
        }

        if (!method.equalsIgnoreCase("get")) {
            OutputStream outputStream = connection.getOutputStream();
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            outputStream.write(input, 0, input.length);
            outputStream.close();
        }

        connection.connect();

        int responseCode = connection.getResponseCode();
        String resultString = "";
        if (responseCode == 302) {
            String redirectUrl = connection.getHeaderField("Location");
            return this.getRedirectHttpRequest(redirectUrl);
        } else {
            InputStream inputStream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
            BufferedInputStream responseReader = new BufferedInputStream(inputStream);
            resultString = new String(responseReader.readAllBytes(), StandardCharsets.UTF_8);
            responseReader.close();
        }
        connection.disconnect();
        return new HttpResponse(responseCode, resultString);
    }

    public HttpResponse httpRequest(HttpURLConnection connection, String method, String body, String authToken) throws IOException {
        connection.setDoOutput(true);
        connection.setRequestMethod(method);
        connection.setInstanceFollowRedirects(true);

        return getHttpResult(connection, method, body, authToken);
    }

    private String buildFormBody(Map<String, String> params) {
        if (params == null) return "";

        StringBuilder bodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> param : params.entrySet()) {
            if (bodyBuilder.length() > 0)
                bodyBuilder.append("&");
            bodyBuilder.append(Globalizer.encodeUrl(param.getKey()));
            bodyBuilder.append("=");
            bodyBuilder.append(Globalizer.encodeUrl(param.getValue()));
        }
        return bodyBuilder.toString();
    }

    public HttpResponse formGetRequest(URL apiURL, boolean useSSL) {
        return this.formGetRequest(apiURL, null, useSSL);
    }

    public HttpResponse formGetRequest(URL apiURL, String tokenString, boolean useSSL) {
        try {
            HttpURLConnection connection = this.openConnection(apiURL, useSSL);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            return this.httpRequest(connection, "GET", "", tokenString);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public HttpResponse formPostRequest(URL apiUrl, Map<String, String> body, boolean useSSL) {
        String bodyString = this.buildFormBody(body);
        return this.formPostRequest(apiUrl, bodyString, "", useSSL);
    }

    public HttpResponse formPostRequest(URL apiUrl, Map<String, String> body, String tokenString, boolean useSSL) {
        String bodyString = this.buildFormBody(body);
        return this.formPostRequest(apiUrl, bodyString, tokenString, useSSL);
    }

    public HttpResponse formPostRequest(URL apiUrl, String body, String tokenString, boolean useSSL) {
        try {
            HttpURLConnection connection = this.openConnection(apiUrl, useSSL);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

            return this.httpRequest(connection, "POST", body, tokenString);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public HttpResponse jsonGetRequest(URL apiUrl, String tokenString, boolean useSSL) {
        try {
            HttpURLConnection connection = this.openConnection(apiUrl, useSSL);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            return this.httpRequest(connection, "GET", "", tokenString);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public HttpResponse jsonPostRequest(URL apiUrl, String jsonString, boolean useSSL) {
        return this.jsonPostRequest(apiUrl, jsonString, null, useSSL);
    }

    public HttpResponse jsonPostRequest(URL apiUrl, String jsonString, String tokenString, boolean useSSL) {
        try {
            HttpURLConnection connection = this.openConnection(apiUrl, useSSL);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");

            return this.httpRequest(connection, "POST", jsonString, tokenString);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }

    public HttpResponse jsonPostRequest(URL apiUrl, String jsonString, String tokenString, boolean useSSL, Map<String, String> headers) {
        try {
            HttpURLConnection connection = this.openConnection(apiUrl, useSSL);
            for (Map.Entry<String, String> param : headers.entrySet()) {
                connection.setRequestProperty(param.getKey(), param.getValue());
            }

            return this.httpRequest(connection, "POST", jsonString, tokenString);
        } catch (IOException ex) {
            throw new GeneralException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage());
        }
    }
}
