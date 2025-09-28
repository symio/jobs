package org.loamok.jobs.manager;

import org.loamok.jobs.entity.User;

/**
 *
 * @author Huby Franck
 */
public interface userService {
    User registerUser(User u);
    User registerUser(User u, Boolean isAdmin);
    Boolean doCheckUserRegistering(User u, StringBuilder failedValidation);
    Boolean checkEmailUnique(String email);
    Boolean checkPasswordCorrect(String password);
    Boolean checkNonNullFields(User u, StringBuilder fieldName);
}
