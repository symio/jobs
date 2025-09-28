package org.loamok.jobs.exceptions;

/**
 *
 * @author Huby Franck
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Password does not meet security requirements.");
    }
}
