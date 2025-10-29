package org.loamok.jobs.manager;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.loamok.jobs.entity.EmailDetails;
import org.loamok.jobs.exceptions.EmailSendingException;
import org.loamok.jobs.exceptions.EmailWithAttachmentSendingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
 */
@Service
@ConfigurationProperties(prefix = "server")
public class EmailManager implements EmailService {

    @Autowired private JavaMailSender javaMailSender;
    @Value("${spring.mail.username}") private String sender;
    protected final Log logger = LogFactory.getLog(getClass());
    
    private String baseurl;

    @Override
    public String getBaseurl() {
        return baseurl;
    }
    
    @Override
    public void setBaseurl(String baseurl) {
        this.logger.info("----");
        this.logger.info("setBaseurl");
        this.logger.info("baseurl = " + baseurl);
        this.baseurl = baseurl;
    }
    
    @Override
    public String sendSimpleMail(EmailDetails details) {
        try {
            SimpleMailMessage mailMessage
                = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());

            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        catch (Exception e) {
            logger.info("sendSimpleMail : ");
            logger.info(e.toString());
            throw new EmailSendingException(details.getRecipient());
        }
        
    }

    @Override
    public String sendMailWithAttachment(EmailDetails details) {
        MimeMessage mimeMessage
            = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody());
            mimeMessageHelper.setSubject(details.getSubject());

            // Adding the attachment
            FileSystemResource file = new FileSystemResource(new File(details.getAttachment()));

            mimeMessageHelper.addAttachment(file.getFilename(), file);

            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully";
        }

        catch (MessagingException e) {
            throw new EmailWithAttachmentSendingException(details.getRecipient());
        }
    }
    
}
