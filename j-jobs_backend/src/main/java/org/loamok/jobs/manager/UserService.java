package org.loamok.jobs.manager;

import org.loamok.jobs.entity.User;

/**
 *
 * @author Huby Franck
 */
public interface UserService {
    User registerUser(User u, boolean toSave);
    User registerUser(User u, Boolean isAdmin, boolean toSave);
    Boolean deactivateRegisteredUser(String emailKey, String emailMessageKey);
    Boolean activateRegisteredUser(String emailKey);
    User resetChallengeRegisteredUser1(String email);
    Boolean disableResetChallenge1(String emailKey, Boolean resetOnly);
    Boolean resetChallengeRegisteredUser2(String emailKey, String newPassword, String newPasswordVerification);
    Boolean disableResetChallenge2(String emailKey);
    Boolean doCheckUserRegistering(User u, StringBuilder failedValidation);
    Boolean checkEmailUnique(String email);
    Boolean checkPasswordCorrect(String password);
    Boolean checkNonNullFields(User u, StringBuilder fieldName);
}
