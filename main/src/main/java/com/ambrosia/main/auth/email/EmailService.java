package com.ambrosia.main.auth.email;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service

public class EmailService implements EmailSender{

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
    private JavaMailSender awsMailSender;

    @Autowired
    EmailService(JavaMailSender mailSender) {
        this.awsMailSender = mailSender;
    }

    @Override
    @Async
    public void send(String to, String email) {
        try {
            MimeMessage mimeMessage = awsMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(email, true);
            helper.setTo(to);
            helper.setSubject("Ambroisa");
            helper.setFrom("friendscommon3.aot@gmail.com");
            awsMailSender.send(mimeMessage);
        }
        catch(MessagingException e) {
            LOGGER.error("failed to send email: " + e);
            throw new IllegalStateException("failed to send email");
        }
    }

}
