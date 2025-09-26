package org.loamok.jobs.manager;

import org.springframework.transaction.annotation.Transactional;
import java.util.regex.Pattern;
import org.loamok.jobs.entity.Role;
import org.loamok.jobs.entity.User;
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
            = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository uR;
    @Autowired
    private RoleRepository rR;

    @Override
    public User registerUser(User u) {
        return registerUser(u, Boolean.FALSE);
    }
    
    @Override
    @Transactional
    public User registerUser(User u, Boolean isAdmin) {
        Role roleUser = null;
        
        if(!isAdmin) {
            roleUser = rR.findByRole("ROLE_USER");
        } else {
            if(u.getRole() == null || u.getRole().getRole() == null | u.getRole().getRole().isBlank())
                throw new RuntimeException("user must have a Role but user.role is null.");
            roleUser = rR.findByRole(u.getRole().getRole());
        }
        
        User user = User.builder()
                .password(u.getPassword())
                .email(u.getEmail())
                .name(u.getName())
                .firstname(u.getFirstname())
                .role(roleUser)
                .build();
        
        if(!doCheckUserRegistering(user)) 
            throw new RuntimeException("user is not unique or wrong password. : " + user.toString());
        
        user.setPassword("{bcrypt}" + passwordEncoder.encode(u.getPassword()));
        
        try {
            uR.saveAndFlush(user);
            return user;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error registering user : " + e.getMessage(), e);
//            throw new RuntimeException("Last name and First name are mandatory parameters. : " + user.toString());
        }
    }

    @Override
    public Boolean doCheckUserRegistering(User u) {
        Boolean isEmailUnique = checkEmailUnique(u.getEmail());
        if(!isEmailUnique) 
            return false;

        Boolean isPasswordCorrect = checkPasswordCorrect(u.getPassword());
        if(!isPasswordCorrect) 
            return false;
        
        Boolean areFieldsFilled = checkNonNullFields(u);
        if(!areFieldsFilled) 
            return false;

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
    public Boolean checkNonNullFields(User u) {
        if(u.getName() == null || u.getName().isBlank())
            return false;
        
        if(u.getFirstname() == null || u.getFirstname().isBlank())
            return false;
        
        return true;
    }
    
    @Override
    public Boolean checkPasswordCorrect(String password) {
        if(password == null || password.isBlank()) 
            return false;

        return pattern.matcher(password).matches();
    }

}
