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

import javax.mail.Message;
import java.util.Map;

@Component
public class WebMailManager {
    private static final Logger logger = LoggerFactory.getLogger(WebMailManager.class);
    @Autowired
    protected JavaMailSender mailSender;

    @Autowired
    protected VelocityTemplateManager templateManager;

    public void send(MailHeader header, String body) {
        MimeMessagePreparator mail = message -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(message);
            if (!header.getFrom().isEmpty()) {
                message.setFrom(header.getFrom());
            } else {
                message.setFrom(Constants.INFO_MAIL);
            }

            if (!header.getCc().isEmpty()) {
                message.addRecipients(Message.RecipientType.CC, header.getCc());
            }

            if (!header.getBcc().isEmpty()) {
                message.addRecipients(Message.RecipientType.BCC, header.getBcc());
            }

            message.addRecipients(Message.RecipientType.TO, header.getTo());
            message.setSubject(header.getSubject());
            message.setText(body);
        };
        try {
            mailSender.send(mail);
        } catch (MailException ex) {
            logger.warn(ex.getLocalizedMessage());
        }
    }

    public void sendByTemplate(MailHeader header, String template, Map<String, Object> data) {
        String body = templateManager.buildTemplate(template, data);
        this.send(header, body);
    }
}
