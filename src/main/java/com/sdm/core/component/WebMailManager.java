package com.sdm.core.component;

import com.sdm.core.model.MailHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class WebMailManager {
    @Autowired
    protected JavaMailSender mailSender;

    @Autowired
    protected VelocityTemplateManager templateManager;

    public void send(MailHeader header, String body) {
        SimpleMailMessage message = new SimpleMailMessage();

        if (!header.getFrom().isEmpty()) {
            message.setFrom(header.getFrom());
        }

        if (!header.getCc().isEmpty()) {
            message.setCc(header.getCc());
        }

        if (!header.getBcc().isEmpty()) {
            message.setBcc(header.getBcc());
        }

        message.setTo(header.getTo());
        message.setSentDate(new Date());
        message.setSubject(header.getSubject());
        message.setText(body);
        mailSender.send(message);
    }

    public void sendByTemplate(MailHeader header, String template, Map<String, Object> data) {
        templateManager.buildTemplate(template, data);
    }
}
