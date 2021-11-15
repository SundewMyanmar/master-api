package com.sdm.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;

@Component
public class LocaleManager {
    private final ResourceBundleMessageSource messageSource;

    @Autowired
    LocaleManager(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessage(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, locale);
    }

    /**
     * Used TextFormatting https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/MessageFormat.html
     *
     * @param code
     * @param arguments
     * @return
     */
    public String getMessage(String code, Object... arguments) {
        Locale locale = LocaleContextHolder.getLocale();
        String pattern = getMessage(code);
        return MessageFormat.format(pattern, arguments);
    }
}
