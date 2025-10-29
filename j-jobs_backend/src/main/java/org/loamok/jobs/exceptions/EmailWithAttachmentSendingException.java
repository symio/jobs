package org.loamok.jobs.exceptions;

/**
 *
 * @author Huby Franck
 */
public class EmailWithAttachmentSendingException extends RuntimeException {
    public EmailWithAttachmentSendingException(String email) {
        super("Error while Sending Mail with attachent to " + email);
    }
}
