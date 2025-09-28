package org.loamok.jobs.exceptions;

/**
 *
 * @author Huby Franck
 */
public class MissingFieldsException extends RuntimeException {
    public MissingFieldsException(String field) {
        super("Missing required field: " + field);
    }
}
