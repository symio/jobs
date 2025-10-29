package org.loamok.jobs.exceptions;

/**
 *
 * @author Huby Franck
 */
public class EmailSendingException extends RuntimeException {
    public EmailSendingException(String email) {
        super("Error while Sending Mail to " + email);
    }
}
