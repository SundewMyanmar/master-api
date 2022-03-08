package com.sdm.core.service;

import com.sdm.core.Constants;
import com.sdm.core.model.MailHeader;
import com.sdm.core.util.Globalizer;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Map;

@Service
@Log4j2
public class MailService {

    @Autowired(required = false)
    protected JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void send(MailHeader header, String body) {
        MimeMessagePreparator mail = message -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true);
            if (!Globalizer.isNullOrEmpty(header.getFrom())) {
                messageHelper.setFrom(header.getFrom());
            } else {
                messageHelper.setFrom(Constants.INFO_MAIL);
            }

            if (!Globalizer.isNullOrEmpty(header.getCc())) {
                messageHelper.setCc(header.getCc());
            }

            if (!Globalizer.isNullOrEmpty(header.getBcc())) {
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
            log.warn(ex.getLocalizedMessage());
        }
    }

    public void sendByTemplate(MailHeader header, String template, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        String body = templateEngine.process(template, context);
        this.send(header, body);
    }
}
