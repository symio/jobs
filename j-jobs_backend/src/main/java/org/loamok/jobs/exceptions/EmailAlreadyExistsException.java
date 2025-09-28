package org.loamok.jobs.exceptions;

/**
 *
 * @author Huby Franck
 */
public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
