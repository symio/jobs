package org.loamok.jobs.dto.email.interfaces;

/**
 *
 * @author Huby Franck
 */
public interface CategorizedMailMessage {
    String getEmailMessage(String messageKey);
    String getEmailTitle(String titleKey);
}
