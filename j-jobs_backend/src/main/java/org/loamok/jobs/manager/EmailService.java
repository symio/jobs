package org.loamok.jobs.manager;

import org.loamok.jobs.entity.EmailDetails;


/**
 *
 * @author Huby Franck
 */
public interface EmailService {
    String getBaseurl();
    void setBaseurl(String baseurl);
    String sendSimpleMail(EmailDetails details);
    String sendMailWithAttachment(EmailDetails details);
}
