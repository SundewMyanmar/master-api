/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sdm.core.util;

import com.sdm.core.Constants;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Htoonlin
 */
public class Globalizer {
    public static String formatDecimal(String format, double amount) {
        DecimalFormat formatter = new DecimalFormat(format);
        return formatter.format(amount);
    }

    public static String formatPrice(Double amount) {
        if (amount == null) amount = 0.0;
        //DecimalFormat formatter = new DecimalFormat("#,###.00");
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    public static String camelToReadable(String input) {
        return capitalize(input.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
                "(?<=[A-Za-z])(?=[^A-Za-z])"), " "));
    }

    public static String camelToSnakeCase(String input) {
        if (Globalizer.isNullOrEmpty(input)) {
            return input;
        }
        String result = "";
        char c = input.charAt(0);
        result += Character.toLowerCase(c);
        for (int i = 1; i < input.length(); i++) {
            c = input.charAt(i);
            if (Character.isUpperCase(c)) {
                result += "_";
                result += Character.toLowerCase(c);
            } else {
                result += c;
            }
        }
        return result;
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

    public static String encodeUrl(String url) {
        return URLEncoder.encode(url, StandardCharsets.UTF_8).replace("+", "%20");
    }

    public static boolean isNullOrEmpty(Object data) {
        boolean result = ObjectUtils.isEmpty(data);

        if (!result && data instanceof String) {
            result = data.toString().trim().length() <= 0
                    || data.toString().equalsIgnoreCase("0")
                    || data.toString().equalsIgnoreCase("0.0")
                    || data.toString().equalsIgnoreCase("false");
        }

        if (!result && data instanceof Number) {
            result = ((Number) data).doubleValue() == 0;
        }

        if (!result && data instanceof Boolean) {
            result = !(Boolean) data;
        }

        return result;
    }

    public static Integer toInt(String input, int defaultValue) {
        Pattern pattern = Pattern.compile(Constants.Pattern.INTEGER);
        if (pattern.matcher(input).matches()) {
            return Integer.parseInt(input);
        }
        return defaultValue;
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

    //Minus date 1 < date 2
    //Plus date 1 > date 2
    public static Integer diffDays(Date date1, Date date2, boolean isDetail) {
        long diff = diffSeconds(date1, date2);
        double days = diff / (24.0 * 60 * 60);
        if (isDetail) {
            if (days < 0)
                days = Math.floor(days);//-0.123 to be -1
            else
                days = Math.ceil(days);//0.123 to be 1
        }
        return (int) days;// 0.123 default 0
    }

    public static Long diffSeconds(Date date1, Date date2) {
        Long from = date1.getTime() / 1000;
        Long to = date2.getTime() / 1000;
        return from - to;
    }

    public static Date addDate(Date date, Duration duration) {
        long seconds = duration.getSeconds();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, Integer.parseInt(String.valueOf(seconds)));
        return cal.getTime();
    }

    public static Date addDate(Date date, int type, int value) {
        Calendar cal = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        cal.setTime(date);
        cal.add(type, value);
        return cal.getTime();
    }

    public static Date addHour(Date date, int hours) {
        return addDate(date, Calendar.HOUR, hours);
    }

    public static Date addMinute(Date date, int minutes) {
        return addDate(date, Calendar.MINUTE, minutes);
    }

    public static Date addSecond(Date date, int seconds) {
        return addDate(date, Calendar.SECOND, seconds);
    }

    public static ServletUriComponentsBuilder getCurrentContextBuilder(boolean forceSsl) {
        ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
        if (forceSsl) builder.scheme("https");
        return builder;
    }

    public static String getCurrentContextPath(String path, boolean forceSsl) {
        ServletUriComponentsBuilder builder = getCurrentContextBuilder(forceSsl);
        return builder.path(path).toUriString();
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress = "";

        if (request != null) {
            remoteAddress = request.getHeader("x-forwarded-for");
            if (Globalizer.isNullOrEmpty(remoteAddress)) {
                remoteAddress = request.getRemoteAddr();
            }
        }
        return remoteAddress.split(",")[0];
    }

    public static String randomPassword(int length) {
        String passwordChars = "ABCDEFGHIJKLMNOPQRSTUVWHZ";
        passwordChars += passwordChars.toLowerCase();
        passwordChars += "0123456789";
        passwordChars += "!@#$%^&*";
        return Globalizer.generateToken(passwordChars, length);
    }

    public static String randomPassword(int min, int max) {
        Random rnd = new Random();
        int size = rnd.nextInt((max - min) + 1) + min;
        return randomPassword(size);
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

    public static String generateToken(String chars, int length) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder pass = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pass.append(chars.charAt(rnd.nextInt(chars.length())));
        }

        return pass.toString();
    }


    public static boolean isEmail(String email) {
        Pattern pattern = Pattern.compile(Constants.Pattern.EMAIL);
        return pattern.matcher(email).matches();
    }

    public static boolean isPhoneNo(String ph) {
        Pattern p = Pattern.compile(Constants.Pattern.MM_PHONE);

        // Pattern class contains matcher() method
        // to find matching between given number
        // and regular expression
        Matcher m = p.matcher(ph);
        return (m.find() && m.group().equals(ph));
    }

    public static String cleanPhoneNo(String ph) {
        if (Globalizer.isNullOrEmpty(ph) || !isPhoneNo(ph)) {
            return null;
        }

        if (ph.startsWith("09")) {
            ph = ph.substring(2, ph.length());
        } else if (ph.startsWith("959")) {
            ph = ph.substring(3, ph.length());
        } else if (ph.startsWith("+959")) {
            ph = ph.substring(4, ph.length());
        } else if (ph.startsWith("+9509")) {
            ph = ph.substring(5, ph.length());
        }

        return ph.trim();
    }
}
