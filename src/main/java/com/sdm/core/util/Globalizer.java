/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.util;

import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author Htoonlin
 */
public class Globalizer {

    public static String camelToReadable(String input) {
        return capitalize(input.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"), " "));
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

    public static String getDateString(String format, Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static Date toDate(String format, String value) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(value);
        } catch (ParseException exception) {
            return new Date();
        }
    }

    public static Date addDate(Date date, Duration duration) {
        long seconds = duration.toSeconds();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, Integer.parseInt(String.valueOf(seconds)));
        return cal.getTime();
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress = "";

        if (request != null) {
            remoteAddress = request.getHeader("x-forwarded-for");
            if (StringUtils.isEmpty(remoteAddress)) {
                remoteAddress = request.getRemoteAddr();
            }
        }
        return remoteAddress.split(",")[0];
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

    public static String generateCodeWithTime(String maxCode, String prefix, Date date, int incLength) {
        String dateString = getDateString("yyyyMMdd", date);
        int startIdx = prefix.length() + dateString.length();
        int incValue = 0;
        if (maxCode.length() == startIdx + incLength) {
            String incString = maxCode.substring(startIdx);
            incValue = Integer.parseInt(incString);
        }
        String incString = String.format("%0" + incLength + "d", incValue + 1);

        return prefix + dateString + incString;
    }

    public static String generateCode(String maxCode, String prefix, int incLength) {
        int startIdx = prefix.length();
        int incValue = 0;
        if (maxCode.length() == startIdx + incLength) {
            String incString = maxCode.substring(startIdx);
            incValue = Integer.parseInt(incString);
        }
        String incString = String.format("%0" + incLength + "d", incValue + 1);

        return prefix + incString;
    }

}
