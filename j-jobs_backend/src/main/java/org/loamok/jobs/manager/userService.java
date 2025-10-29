package org.loamok.jobs.manager;

import org.loamok.jobs.entity.User;

/**
 *
 * @author Huby Franck
 */
public interface userService {
    User registerUser(User u, boolean toSave);
    User registerUser(User u, Boolean isAdmin, boolean toSave);
    Boolean deactivateRegisteredUser(String emailKey);
    Boolean checkRegisteredUser(String emailKey);
    Boolean doCheckUserRegistering(User u, StringBuilder failedValidation);
    Boolean checkEmailUnique(String email);
    Boolean checkPasswordCorrect(String password);
    Boolean checkNonNullFields(User u, StringBuilder fieldName);
}
