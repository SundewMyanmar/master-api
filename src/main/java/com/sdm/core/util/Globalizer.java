/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.util;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Htoonlin
 */
public class Globalizer {

    public static String camelToLowerUnderScore(String input) {
        return (new PropertyNamingStrategy.SnakeCaseStrategy()).translate(input);
    }

    public static String camelToReadable(String input) {
        return input.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
            "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    }

    public static String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

    public static boolean isHttpSuccess(String code) {
        if (code.matches("\\d{3}")) {
            int status = Integer.parseInt(code);
            return (status >= 100 && status <= 511);
        }
        return false;
    }

    public static String getSystemURL(HttpServletRequest request) {
        String url = "";
        String schema = request.getScheme();
        String server = request.getServerName();
        String contextPath = request.getContextPath();
        int port = request.getServerPort();
        if (port == 80 || port == 443) {
            url = String.format("%s://%s", schema, server);
        } else {
            url = String.format("%s://%s:%s", schema, server, port);
        }

        return url + contextPath;
    }

    public static String getDateString(String format, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static String generateToken(String chars, int length) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder pass = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pass.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return pass.toString();
    }

    public static boolean isEmail(String email) {
        Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        return pattern.matcher(email).matches();
    }

}
