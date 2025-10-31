package org.loamok.jobs.manager;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 *
 * @author Huby Franck
 */
@Service
public class UserManager implements userService {

    // Regex pour valider le mot de passe
    private static final String PASSWORD_PATTERN
            = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-|~#])[A-Za-z\\d@$!%*?&_\\-|~#]{8,}$";
    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private UserRepository uR;
    @Autowired
    private RoleRepository rR;
    @Autowired
    private ClientSignatureUtil csb;
    @Autowired
    private EmailService emailManager;

    private User getUserByEmailKey(String emailKey) {
        User u = uR.findByEmailVerificationKey(emailKey);
        
        return u;
    }
    
    @Override
    public Boolean deactivateRegisteredUser(String emailKey) {
        User u = getUserByEmailKey(emailKey);
        
        if(u != null && u.getEmailVerificationKey() != null && u.getEmailVerificationKey().equals(emailKey)) {
            u.setEnabled(Boolean.FALSE);
            logger.info("unRegistering User with " + u.getEmailVerificationKey() + " Email Key.");
            logger.info("(" + u.getEmail() + ")");
            u.setEmailVerificationKey(null);
            String originalMail = u.getEmail();
            u.setEmail("disabled_"+ originalMail);
            
            try {
                // send email to user
                String status = emailManager.sendSimpleMail(EmailDetails.builder()
                        .recipient(originalMail)
                        .subject("Invalidation de la demande d'inscription à l'application Jobs.")
                        .msgBody("Bonjour " + u.getName() + " " + u.getFirstname() + ",\n\n"
                                + "Vous avez demandé l'invalidation d'un profil sur l'application Jobs avec votre adresse e-mail.\n\n"
                                + "Ce message vous indique que votre demande à bien été pris en compte." + "\n\n"
                                + "Si vous n'étiez pas à l'origine de cette demande nous vous prions de nous excuser pour la gêne occasionnée." + "\n"
                                + "Nous vous remercions pour votre vigilance." + "\n\n"
                                + "Cordialement" + ",\n\n"
                                + "--" + ",\n"
                                + "L'équipe de Jobs" + "\n"
                                + emailManager.getBaseurl() + "/mentions-legales" + "\n"
                        )
                        .build());
                logger.info("Email sent to user with " + status);
                
                // send Email to admin
                String statusAdmin = emailManager.sendSimpleMail(EmailDetails.builder()
                        .recipient("admin@loamok.org")
                        .subject("Invalidation de la demande d'inscription à l'application Jobs.")
                        .msgBody("Bonjour,\n\n"
                                + "L'utilisateur " + u.getName() + " " + u.getFirstname() + "(" + originalMail + ")"
                                + " a demandé l'annulation de sa souscription" + "\n\n"
                                + "Cordialement" + ",\n\n"
                                + "--" + ",\n"
                                + "L'équipe de Jobs" + "\n"
                                + emailManager.getBaseurl() + "/mentions-legales" + "\n"
                        )
                        .build());

                logger.info("Email sent to admin with " + statusAdmin);
            } catch (EmailSendingException e) {
                logger.info("Email not sent : " + e.toString());
                return Boolean.FALSE;
            }
            
            
            uR.save(u);
            return Boolean.TRUE;
        }
        
        return Boolean.FALSE;
    }
    
    @Override
    public Boolean activateRegisteredUser(String emailKey) {
        User u = getUserByEmailKey(emailKey);
        
        if(u != null && u.getEmailVerificationKey() != null && u.getEmailVerificationKey().equals(emailKey)) {
            u.setEnabled(Boolean.TRUE);
            u.setEmailVerificationKey(null);
            
            uR.save(u);
            
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
        
        if(isAdmin) {
            if(u.getRole() == null || u.getRole().getRole() == null || u.getRole().getRole().isBlank())
                throw new RuntimeException("user must have a Role but user.role is null.");
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
                .enabled((isAdmin)? true : false)
                .build();
        
        StringBuilder failedValidation = new StringBuilder();
        if(!doCheckUserRegistering(user, failedValidation)) {
            switch (failedValidation.toString()) {
                case "email" -> throw new EmailAlreadyExistsException(user.getEmail());
                case "password" -> throw new InvalidPasswordException();
                default -> throw new MissingFieldsException(failedValidation.toString());
            }
        }
        
        user.setPassword("{bcrypt}" + passwordEncoder.encode(u.getPassword()));
        
        if(u.getEmailVerificationKey() == null) {
            String randomToken = UUID.randomUUID().toString();
            
            String rawSignature = String.join("|",
                u.getEmail(), u.getName(),
                u.getFirstname(), roleUser.getRole(),
                "" + System.currentTimeMillis(), randomToken
            );
            
            String hashedSignature = csb.buildHashedSignature(rawSignature);
            String urlSafeSignature = hashedSignature
                .replace('+', '-')
                .replace('/', '_')
                .replace("=", "");

            user.setEmailVerificationKey(urlSafeSignature);

            String status = emailManager.sendSimpleMail(EmailDetails.builder()
                    .recipient(user.getEmail())
                    .subject("Validez votre inscription à l'application Jobs.")
                    .msgBody("Bonjour " + user.getName() + " " + user.getFirstname() + ",\n\n"
                            + "Vous avez créé un profil sur l'application Jobs.\n\n"
                            + "Afin de valider votre adresse e-mail merci de cliquer sur le lien suivant : "
                            + emailManager.getBaseurl() + "/register/activate?key="+ user.getEmailVerificationKey() + "\n"
                            + "Vous pouvez copier votre clé d'enregistrement manuellement dans le formulaire :" + "\n"
                            + "\t- Clé d'enregistrement :" + user.getEmailVerificationKey() + "\n"
                            + "\t- Url d'activation : "+ emailManager.getBaseurl() + "/register/activate" + "\n\n"
                            + "Si vous n'avez pas ouvert de profil sur Jobs vous pouvez nous le signaler." + "\n"
                            + "Pour cela utilisez l'url ci-dessous : "  + "\n"
                            + emailManager.getBaseurl() + "/register/deactivate?key="+ user.getEmailVerificationKey() + "\n\n"
                            + "Cordialement" + ",\n\n"
                            + "--" + ",\n"
                            + "L'équipe de Jobs" + "\n"
                            + emailManager.getBaseurl() + "/mentions-legales" + "\n"
                    )
                    .build());

            logger.info("Registering User with " + user.getEmailVerificationKey() + " Email Key.");
            logger.info("Email sent with " + status);
        }
        
        if(toSave) {
            try {
                uR.saveAndFlush(user);
                return user;
            } catch (RuntimeException e) {
                throw new RuntimeException("Error registering user : " + e.getMessage(), e);
            }
        }
        
        return user;
    }

    @Override
    public Boolean doCheckUserRegistering(User u, StringBuilder failedValidation) {
        Boolean isEmailUnique = checkEmailUnique(u.getEmail());
        if(!isEmailUnique) {
            failedValidation.setLength(0);
            failedValidation.append("email");
            return false;
        }

        Boolean isPasswordCorrect = checkPasswordCorrect(u.getPassword());
        if(!isPasswordCorrect) {
            failedValidation.setLength(0);
            failedValidation.append("password");
            return false;
        }
        
        StringBuilder fieldName = new StringBuilder();
        Boolean areFieldsFilled = checkNonNullFields(u, fieldName);
        if(!areFieldsFilled) {
            failedValidation.setLength(0);
            failedValidation.append(fieldName);
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public Boolean checkEmailUnique(String email) {
        if(email == null || email.isBlank()) 
            return false;

        final User u = uR.findByEmail(email);

        Boolean isUnique = false;

        if(u == null)
            isUnique = true;

        return isUnique;
    }

    @Override
    public Boolean checkNonNullFields(User u, StringBuilder fieldName) {
        if(u.getName() == null || u.getName().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("name");
            return false;
        }
        
        if(u.getFirstname() == null || u.getFirstname().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("firstname");
            return false;
        }
        
        return true;
    }
    
    @Override
    public Boolean checkPasswordCorrect(String password) {
        if(password == null || password.isBlank()) 
            return false;

        return PATTERN.matcher(password).matches();
    }

}
