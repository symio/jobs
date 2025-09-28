package org.loamok.jobs.manager;

import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;
import org.loamok.jobs.entity.Role;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.exceptions.EmailAlreadyExistsException;
import org.loamok.jobs.exceptions.InvalidPasswordException;
import org.loamok.jobs.exceptions.MissingFieldsException;
import org.loamok.jobs.repository.RoleRepository;
import org.loamok.jobs.repository.UserRepository;
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
            = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&\\-_|~#])[A-Za-z\\d@$!%*?&\\-_|~#]{8,}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository uR;
    @Autowired
    private RoleRepository rR;

    @Override
    public User registerUser(User u, boolean toSave) {
        return registerUser(u, Boolean.FALSE, toSave);
    }
    
    @Override
    @Transactional
    public User registerUser(User u, Boolean isAdmin, boolean toSave) {
        System.out.println("org.loamok.jobs.manager.UserManager.registerUser()(étendu)");
        Role roleUser = null;
        
        if(!isAdmin) {
            roleUser = rR.findByRole("ROLE_USER");
        } else {
            if(u.getRole() == null || u.getRole().getRole() == null || u.getRole().getRole().isBlank())
                throw new RuntimeException("user must have a Role but user.role is null.");
            roleUser = rR.findByRole(u.getRole().getRole());
        }
        
        User user = User.builder()
                .password(u.getPassword())
                .email(u.getEmail())
                .name(u.getName())
                .firstname(u.getFirstname())
                .role(roleUser)
                .gdproptin(u.isGdproptin())
                .enabled(true)
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

        return pattern.matcher(password).matches();
    }

}
