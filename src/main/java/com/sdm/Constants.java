package com.sdm;

/**
 * Global constants data.
 *
 * @author Htoonlin
 */
public interface Constants {

    String APP_NAME = "MasterAPI";
    String INFO_MAIL = "info@sundewmyanmar.com";

    /**
     * Regex Patterns
     *
     * @author Htoonlin
     */
    interface Pattern {

        /**
         * Support Alpha numeric only
         */
        String ALPHA_NUMERIC = "^[a-zA-Z0-9]*$";
        /**
         * Support Character only without number
         */
        String CHARACTER_ONLY = "^[a-zA-Z]*$";

        /**
         * Must be integer
         */
        String INTEGER = "^-?\\d+$";

        /**
         * Must be positive integer
         */
        String POSITIVE_INTEGER = "^\\d+$";

        /**
         * Must be negative integer
         */
        String NEGATIVE_INTEGER = "^-\\d+$";

        /**
         * Must be number
         */
        String NUMBER = "^-?\\d*\\.?\\d+$";

        /**
         * Must be positive number
         */
        String POSITIVE_NUMBER = "^\\d*\\.?\\d+$";

        /**
         * Must be negative number
         */
        String NEGATIVE_NUMBER = "^-\\d*\\.?\\d+$";

        /**
         * Password validation
         */
        String STRONG_PASSWORD = "^(?=^.{6,}$)((?=.*[A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z]))^.*$";
        /**
         * Color hexa code. example => #FF0000, #ff0000, #fff, #FFF
         */
        String COLOR_HEX = "^#?([a-f0-9]{8}|[a-f0-9]{6}|[a-f0-9]{4}|[a-f0-9]{3})$";
        /**
         * Email format
         */
        String EMAIL = "^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$";
        /**
         * URL format
         */
        String URL = "^(((http|https|ftp):\\/\\/)?([[a-zA-Z0-9]\\-\\.])+(\\.)([[a-zA-Z0-9]]){2,4}([[a-zA-Z0-9]\\/+=%&_\\.~?\\-]*))*$";
        /**
         * IP Format
         */
        String IP = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        /**
         * All credit card (Master, Visa, American Express)
         */
        String CREDIT_CARD = "^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|6011[0-9]{12}|622((12[6-9]|1[3-9][0-9])|([2-8][0-9][0-9])|(9(([0-1][0-9])|(2[0-5]))))[0-9]{10}|64[4-9][0-9]{13}|65[0-9]{14}|3(?:0[0-5]|[68][0-9])[0-9]{11}|3[47][0-9]{13})*$";
        /**
         * Master card
         */
        String MASTER_CARD = "^(5[1-5][0-9]{14})*$";
        /**
         * Visa card
         */
        String VISA_CARD = "^(4[0-9]{12}(?:[0-9]{3})?)*$";
        /**
         * American express card
         */
        String AMERICAN_EXPRESS_CARD = "^(3[47][0-9]{13})*$";
        /**
         * Other credit/debit cards
         */
        String UNKNOWN_CARD = "^(3(?:0[0-5]|[68][0-9])[0-9]{11})*$";
    }

    interface Auth {
        /**
         * Prefix of Authorization Token String
         */
        String TYPE = "Bearer";

        /**
         * request Parameter Token Name
         */
        String PARAM_NAME = "accessToken";

        /**
         * User Key of JWT Subject
         */
        String DEFAULT_USER_ROLE = "BASIC_USER";

        /**
         * Super Admin role for all permission
         */
        String ROOT_ROLE = "SUPER_USER";
    }
}
