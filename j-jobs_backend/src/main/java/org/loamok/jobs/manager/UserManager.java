package org.loamok.jobs.manager;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.loamok.jobs.dto.email.constants.EmailMessageConstants;
import org.loamok.jobs.dto.email.constants.RegisterEmailConstants;
import org.loamok.jobs.dto.email.constants.ResetChallengeConstants;
import org.loamok.jobs.entity.EmailDetails;
import org.loamok.jobs.entity.Role;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.exceptions.EmailAlreadyExistsException;
import org.loamok.jobs.exceptions.EmailSendingException;
import org.loamok.jobs.exceptions.InvalidPasswordException;
import org.loamok.jobs.exceptions.MissingFieldsException;
import org.loamok.jobs.repository.RoleRepository;
import org.loamok.jobs.repository.UserRepository;
import org.loamok.jobs.util.ClientSignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.loamok.jobs.dto.email.interfaces.EmailMessage;

/**
 *
 * @author Huby Franck
 */
@Service
public class UserManager implements UserService {

    // Regex pour valider le mot de passe
    private static final String PASSWORD_PATTERN
            = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-|~#])[A-Za-z\\d@$!%*?&_\\-|~#]{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private EmailMessage messageGetter;
    @Autowired
    private UserRepository uR;
    @Autowired
    private RoleRepository rR;
    @Autowired
    private ClientSignatureUtil csb;
    @Autowired
    private EmailService emailManager;

    @Override
    @Transactional
    public User resetChallengeRegisteredUser1(String email) {
        User u = uR.findByEmail(email);

        if (u == null) {
            return u;
        }

        if (u.getEmailVerificationKey() != null && u.getKeyValidity() != null) {
            return u;
        }

        Map<String, String> substitutions = new HashMap<>();
        substitutions.put("BASE_URL", emailManager.getBaseurl());
        substitutions.put("U_NAME", u.getName());
        substitutions.put("U_FIRSTNAME", u.getFirstname());

        String message = messageGetter.getEmailMessage(ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_KEY_GENERATION", substitutions);

        resetKeyEmail(u, Boolean.FALSE);
        String status = generateAndSetEmailVerificationKey(
                u,
                messageGetter.getEmailTitle(ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_KEY_GENERATION_TITLE", null),
                message
        );

        uR.save(u);
        logger.info("Registering security challenge for User " + u.getEmail() + " with " + u.getEmailVerificationKey() + " Email Key.");
        logger.info("Email sent with " + status);

        return u;
    }

    @Override
    @Transactional
    public Boolean disableResetChallenge1(String emailKey, Boolean resetOnly) {
        if (Objects.equals(resetOnly, Boolean.FALSE)) {
            return deactivateRegisteredUser(emailKey, RegisterEmailConstants.EMAIL_MESSAGE_PREFIX + "_INVALIDATION");
        }

        User u = getUserByEmailKey(emailKey);

        if (u != null && u.getEmailVerificationKey() != null && u.getEmailVerificationKey().equals(emailKey)) {
            if (Objects.equals(deactivateRegisteredUser(emailKey, ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_INVALIDATION"), Boolean.TRUE)) {
                u.setEnabled(Boolean.TRUE);
                u.setEmail(u.getEmail().replace("disabled_", ""));

                uR.save(u);
            } else {
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    @Transactional
    public Boolean resetChallengeRegisteredUser2(String emailKey, String newPassword, String passwordConfirm) {
        User u = getUserByEmailKey(emailKey);

        if (u == null) {
            return Boolean.FALSE;
        }

        if (u.getEmailVerificationKey() == null || !u.getEmailVerificationKey().equals(emailKey)) {
            resetKeyEmail(u, Boolean.FALSE);
            uR.save(u);

            return Boolean.FALSE;
        }
        if (u.getEnabled().equals(Boolean.TRUE)) {
            resetKeyEmail(u, Boolean.TRUE);
            uR.save(u);

            return Boolean.FALSE;
        }

        if (Duration.between(u.getKeyValidity(), Instant.now()).toMillis() > 0) {
            resetKeyEmail(u, Boolean.FALSE);
            uR.save(u);

            return Boolean.FALSE;
        }

        if (newPassword.equals(passwordConfirm)) {
            if (!checkPasswordCorrect(newPassword)) {
                return Boolean.FALSE;
            }

            resetKeyEmail(u, Boolean.TRUE);

            String storedPassword = u.getPassword();
            String newPasswordEncoded = passwordEncoder.encode(newPassword);

            if (storedPassword.startsWith("{bcrypt}")) {
                storedPassword = storedPassword.substring(8); 
            }
            
            if (passwordEncoder.matches(newPasswordEncoded, storedPassword)) {
                resetKeyEmail(u, Boolean.FALSE);
                uR.save(u);
                return Boolean.FALSE;
            }
            
            u.setPassword("{bcrypt}" + passwordEncoder.encode(newPassword));
            
            Map<String, String> substitutions = new HashMap<>();
            substitutions.put("BASE_URL", emailManager.getBaseurl());
            substitutions.put("U_NAME", u.getName());
            substitutions.put("U_FIRSTNAME", u.getFirstname());

            String urlSafeSignature = generateKey(u);
            u.setEmailVerificationKey(urlSafeSignature);
            u.setKeyValidity(null);

            String message = messageGetter.getEmailMessage(ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_VALIDATION", substitutions);

            String separator = EmailMessageConstants.EMAIL_MESSAGE_SUBSTITUTION_SEPARATOR;
            message = message.replace(separator + "EMAIL_VERIFICATION_KEY" + separator, u.getEmailVerificationKey());

            String status = emailManager.sendSimpleMail(EmailDetails.builder()
                    .recipient(u.getEmail())
                    .subject(messageGetter.getEmailTitle(ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_VALIDATION_TITLE", null))
                    .msgBody(message)
                    .build());

            uR.save(u);

            logger.info("Registering security challenge 2 for User " + u.getEmail() + " with " + u.getEmailVerificationKey() + " Email Key.");
            logger.info("Email sent with " + status);
        } else {
            return Boolean.FALSE;
        }

        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean disableResetChallenge2(String emailKey) {
        User u = getUserByEmailKey(emailKey);

        if (u != null && u.getEmailVerificationKey() != null && u.getEmailVerificationKey().equals(emailKey) && u.getKeyValidity() == null) {
            resetKeyEmail(u, Boolean.FALSE);
            String originalMail = u.getEmail();
            u.setEmail("security_disabled_" + u.getEmail());
            uR.save(u);

            try {
                String title = null;
                Map<String, String> substitutions = new HashMap<>();
                substitutions.put("BASE_URL", emailManager.getBaseurl());
                substitutions.put("U_NAME", u.getName());
                substitutions.put("U_FIRSTNAME", u.getFirstname());

                title = messageGetter.getEmailTitle(ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_DISABLING_TITLE", null);

                String message = messageGetter.getEmailMessage(ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_DISABLING", substitutions);
                // send email to user
                String status = emailManager.sendSimpleMail(EmailDetails.builder()
                        .recipient(originalMail)
                        .subject(title)
                        .msgBody(message)
                        .build());
                logger.info("Email sent to user with " + status);

                // send Email to admin
                substitutions.put("ORIGINAL_MAIL", originalMail);
                String messageAdmin = messageGetter.getEmailMessage(
                        ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_DISABLING" + EmailMessageConstants.EMAIL_ADMIN_SUFFIX,
                        substitutions
                );
                String statusAdmin = emailManager.sendSimpleMail(EmailDetails.builder()
                        .recipient("admin@loamok.org")
                        .subject(title)
                        .msgBody(messageAdmin)
                        .build());

                logger.info("Email sent to admin with " + statusAdmin);
            } catch (EmailSendingException e) {
                logger.info("Email not sent : " + e.toString());
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    @Transactional
    public Boolean deactivateRegisteredUser(String emailKey, String emailMessageKey) {
        User u = getUserByEmailKey(emailKey);

        if (u != null && u.getEmailVerificationKey() != null && u.getEmailVerificationKey().equals(emailKey) && u.getKeyValidity() != null) {
            if (u.getKeyValidity() != null && Duration.between(u.getKeyValidity(), Instant.now()).toMillis() > 0) {
                resetKeyEmail(u, Boolean.FALSE);
                uR.save(u);

                return Boolean.FALSE;
            }

            logger.info("unRegistering User with " + u.getEmailVerificationKey() + " Email Key.");
            logger.info("(" + u.getEmail() + ")");

            resetKeyEmail(u, Boolean.FALSE);
            String originalMail = u.getEmail();
            u.setEmail("disabled_" + originalMail);

            try {
                String title = null;
                Map<String, String> substitutions = new HashMap<>();
                substitutions.put("BASE_URL", emailManager.getBaseurl());
                substitutions.put("U_NAME", u.getName());
                substitutions.put("U_FIRSTNAME", u.getFirstname());

                switch (emailMessageKey) {
                    case RegisterEmailConstants.EMAIL_MESSAGE_PREFIX + "_INVALIDATION" ->
                        title = messageGetter.getEmailTitle(emailMessageKey + "_TITLE", null);
                    case ResetChallengeConstants.EMAIL_MESSAGE_PREFIX + "_INVALIDATION" ->
                        title = messageGetter.getEmailTitle(emailMessageKey + "_TITLE", null);
                    default ->
                        throw new AssertionError();
                }

                String message = messageGetter.getEmailMessage(emailMessageKey, substitutions);
                // send email to user
                String status = emailManager.sendSimpleMail(EmailDetails.builder()
                        .recipient(originalMail)
                        .subject(title)
                        .msgBody(message)
                        .build());
                logger.info("Email sent to user with " + status);

                // send Email to admin
                substitutions.put("ORIGINAL_MAIL", originalMail);
                String messageAdmin = messageGetter.getEmailMessage(emailMessageKey + EmailMessageConstants.EMAIL_ADMIN_SUFFIX,
                        substitutions
                );
                String statusAdmin = emailManager.sendSimpleMail(EmailDetails.builder()
                        .recipient("admin@loamok.org")
                        .subject(title)
                        .msgBody(messageAdmin)
                        .build());

                logger.info("Email sent to admin with " + statusAdmin);
            } catch (EmailSendingException e) {
                uR.save(u);
                logger.info("Email not sent : " + e.toString());
                return Boolean.FALSE;
            }

            uR.save(u);
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    @Transactional
    public Boolean activateRegisteredUser(String emailKey) {
        User u = getUserByEmailKey(emailKey);

        if (u != null && u.getEmailVerificationKey() != null && u.getEmailVerificationKey().equals(emailKey) && u.getKeyValidity() != null) {
            if (Duration.between(u.getKeyValidity(), Instant.now()).toMillis() > 0) {
                resetKeyEmail(u, Boolean.FALSE);
                emailKeyForRegisterUser(u);

                uR.save(u);
                return Boolean.FALSE;
            }

            resetKeyEmail(u, Boolean.TRUE);

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public User registerUser(User u, boolean toSave) {
        return registerUser(u, Boolean.FALSE, toSave);
    }

    @Override
    @Transactional
    public User registerUser(User u, Boolean isAdmin, boolean toSave) {
        System.out.println("org.loamok.jobs.manager.UserManager.registerUser()(étendu)");
        Role roleUser = rR.findByRole("ROLE_USER");

        if (isAdmin) {
            if (u.getRole() == null || u.getRole().getRole() == null || u.getRole().getRole().isBlank()) {
                throw new RuntimeException("user must have a Role but user.role is null.");
            }
            roleUser = rR.findByRole(u.getRole().getRole());
        }
        System.out.println("roleUser : " + roleUser);

        User user = User.builder()
                .password(u.getPassword())
                .email(u.getEmail())
                .name(u.getName())
                .firstname(u.getFirstname())
                .role(roleUser)
                .gdproptin(u.isGdproptin())
                .enabled((isAdmin) ? true : false)
                .build();

        StringBuilder failedValidation = new StringBuilder();
        if (!doCheckUserRegistering(user, failedValidation)) {
            switch (failedValidation.toString()) {
                case "email" ->
                    throw new EmailAlreadyExistsException(user.getEmail());
                case "password" ->
                    throw new InvalidPasswordException();
                default ->
                    throw new MissingFieldsException(failedValidation.toString());
            }
        }

        user.setPassword("{bcrypt}" + passwordEncoder.encode(u.getPassword()));

        emailKeyForRegisterUser(user);

        if (toSave) {
            try {
                uR.saveAndFlush(user);
                return user;
            } catch (RuntimeException e) {
                throw new RuntimeException("Error registering user : " + e.getMessage(), e);
            }
        }

        return user;
    }

    private User getUserByEmailKey(String emailKey) {
        User u = uR.findByEmailVerificationKey(emailKey);

        return u;
    }

    private void resetKeyEmail(User u, Boolean setEnabled) {
        u.setEnabled(setEnabled);
        u.setEmailVerificationKey(null);
        u.setKeyValidity(null);
        u.setAuthToken(null);
        u.setRememberMeToken(null);
    }

    private void emailKeyForRegisterUser(User u) {
        if (u.getEmailVerificationKey() == null) {
            Map<String, String> substitutions = new HashMap<>();
            substitutions.put("BASE_URL", emailManager.getBaseurl());
            substitutions.put("U_NAME", u.getName());
            substitutions.put("U_FIRSTNAME", u.getFirstname());

            String status = generateAndSetEmailVerificationKey(
                    u,
                    messageGetter.getEmailTitle(RegisterEmailConstants.EMAIL_MESSAGE_PREFIX + "_VALIDATION_TITLE", null),
                    messageGetter.getEmailMessage(RegisterEmailConstants.EMAIL_MESSAGE_PREFIX + "_VALIDATION", substitutions)
            );

            logger.info("Registering User with " + u.getEmailVerificationKey() + " Email Key.");
            logger.info("Email sent with " + status);
        }
    }

    private String generateAndSetEmailVerificationKey(User u, String title, String message) {
        String urlSafeSignature = generateKey(u);
        u.setEmailVerificationKey(urlSafeSignature);

        Instant actualDate = Instant.now();
        u.setKeyValidity(Date.from(actualDate.plus(Duration.ofHours(1))).toInstant());

        String separator = EmailMessageConstants.EMAIL_MESSAGE_SUBSTITUTION_SEPARATOR;
        message = message.replace(separator + "EMAIL_VERIFICATION_KEY" + separator, u.getEmailVerificationKey());

        String status = emailManager.sendSimpleMail(EmailDetails.builder()
                .recipient(u.getEmail())
                .subject(title)
                .msgBody(message)
                .build());

        return status;
    }

    private String generateKey(User u) {
        String randomToken = UUID.randomUUID().toString();

        String rawSignature = String.join("|",
                u.getEmail(), u.getName(),
                u.getFirstname(), u.getRole().getRole(),
                "" + System.currentTimeMillis(), randomToken
        );

        String hashedSignature = csb.buildHashedSignature(rawSignature);
        String urlSafeSignature = hashedSignature
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "");

        return urlSafeSignature;
    }

    @Override
    public Boolean doCheckUserRegistering(User u, StringBuilder failedValidation) {
        Boolean isEmailUnique = checkEmailUnique(u.getEmail());
        if (!isEmailUnique) {
            failedValidation.setLength(0);
            failedValidation.append("email");
            return false;
        }

        Boolean isPasswordCorrect = checkPasswordCorrect(u.getPassword());
        if (!isPasswordCorrect) {
            failedValidation.setLength(0);
            failedValidation.append("password");
            return false;
        }

        StringBuilder fieldName = new StringBuilder();
        Boolean areFieldsFilled = checkNonNullFields(u, fieldName);
        if (!areFieldsFilled) {
            failedValidation.setLength(0);
            failedValidation.append(fieldName);
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean checkEmailUnique(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        final User u = uR.findByEmail(email);

        Boolean isUnique = false;

        if (u == null) {
            isUnique = true;
        }

        return isUnique;
    }

    @Override
    public Boolean checkNonNullFields(User u, StringBuilder fieldName) {
        if (u.getName() == null || u.getName().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("name");
            return false;
        }

        if (u.getFirstname() == null || u.getFirstname().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("firstname");
            return false;
        }

        return true;
    }

    @Override
    public Boolean checkPasswordCorrect(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }

        return PATTERN.matcher(password).matches();
    }

}
