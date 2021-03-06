package com.sdm.core.util;

import com.google.myanmartools.TransliterateU2Z;
import com.google.myanmartools.TransliterateZ2U;
import com.google.myanmartools.ZawgyiDetector;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Myanmar Tools uses a machine learning model to give very accurate results
 * when detecting Zawgyi versus Unicode. Detectors that use hand-coded rules for
 * detection are susceptible to flagging content in other languages like Shan
 * and Mon as Zawgyi when it is actually Unicode. Ref =>
 * https://github.com/googlei18n/myanmar-tools
 */
@Log4j2
public class MyanmarFontManager {

    private static final double THRESHOLD = 0.2;
    private static final ZawgyiDetector zgDetector = new ZawgyiDetector();
    private static final TransliterateZ2U z2u = new TransliterateZ2U("Zawgyi to Unicode");
    private static final TransliterateU2Z u2z = new TransliterateU2Z("Unicode to Zawgyi");
    private static final String[] mm_numbers = new String[]{"၀", "၁", "၂", "၃", "၄", "၅", "၆", "၇", "၈", "၉"};

    public static String convertToMMNumber(String nums) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < nums.length(); i++) {
            //don't translate, if not number
            if (Character.isDigit(nums.charAt(i))) {
                builder.append(mm_numbers[(int) nums.charAt(i) - 48]);
            } else {
                builder.append(nums.charAt(i));
            }
        }
        return builder.toString();
    }

    // These Pattern Ref => http://novasteps.com/shake-n-break.html
    private static final Pattern IS_MYANMAR_PATTERN = Pattern.compile("[\u1000-\u1021]+|[\u1025-\u1027]");

    public static boolean isMyanmar(String input) {
        if (input == null) {
            return false;
        }

        Matcher matcher = IS_MYANMAR_PATTERN.matcher(input);
        return matcher.find();
    }

    public static boolean isZawgyi(String input) {
        if (StringUtils.isEmpty(input)) {
            return false;
        }

        double result = zgDetector.getZawgyiProbability(input);
        return result > THRESHOLD;
    }

    public static String toUnicode(String zawgyi) {
        if (MyanmarFontManager.isZawgyi(zawgyi)) {
            return z2u.convert(zawgyi);
        }
        return zawgyi;
    }

    public static String toZawgyi(String unicodeInput) {
        return u2z.convert(unicodeInput);
    }

    public static Object getResponseObject(String input) {
        if (MyanmarFontManager.isMyanmar(input)) {
            Map<String, String> output = new HashMap<>();
            output.put("zg", toZawgyi(input));
            output.put("uni", input);
            return output;
        } else {
            return input;
        }
    }
}