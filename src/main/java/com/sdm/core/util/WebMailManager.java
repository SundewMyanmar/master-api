package com.sdm.core.util;

import com.sdm.Constants;
import com.sdm.core.model.MailHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Map;

@Component
public class WebMailManager {
    private static final Logger logger = LoggerFactory.getLogger(WebMailManager.class);

    @Autowired
    protected JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void send(MailHeader header, String body) {
        MimeMessagePreparator mail = message -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            if (!StringUtils.isEmpty(header.getFrom())) {
                messageHelper.setFrom(header.getFrom());
            } else {
                messageHelper.setFrom(Constants.INFO_MAIL);
            }

            if (!StringUtils.isEmpty(header.getCc())) {
                messageHelper.setCc(header.getCc());
            }

            if (!StringUtils.isEmpty(header.getBcc())) {
                messageHelper.setBcc(header.getBcc());
            }

            messageHelper.setTo(header.getTo());
            messageHelper.setPriority(1);
            messageHelper.setSubject(header.getSubject());
            messageHelper.setText(body, true);
        };
        try {
            mailSender.send(mail);
        } catch (MailException ex) {
            logger.warn(ex.getLocalizedMessage());
        }
    }

    public void sendByTemplate(MailHeader header, String template, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        String body = templateEngine.process(template, context);
        this.send(header, body);
    }
}
