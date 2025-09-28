package org.loamok.jobs.event;

import org.loamok.jobs.entity.User;
import org.loamok.jobs.exceptions.EmailAlreadyExistsException;
import org.loamok.jobs.exceptions.InvalidPasswordException;
import org.loamok.jobs.exceptions.MissingFieldsException;
import org.loamok.jobs.manager.UserManager;
import org.loamok.jobs.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Huby Franck
 */
@Component
@RepositoryEventHandler(User.class)
public class UserEventHandler {

    @Autowired
    private UserManager userManager;
    
    @Autowired
    private RoleRepository rRepository;

    @HandleBeforeCreate
    public void handleUtilisateurCreate(User user) {
        
        if(user.getRole() == null || user.getRole().getRole() == null || user.getRole().getRole().isBlank()) {
            user.setRole(rRepository.findByRole("ROLE_USER"));
        }
        
        StringBuilder failedValidation = new StringBuilder();
        if (!userManager.doCheckUserRegistering(user, failedValidation)) {
            switch (failedValidation.toString()) {
                case "email" -> throw new EmailAlreadyExistsException(user.getEmail());
                case "password" -> throw new InvalidPasswordException();
                default -> throw new MissingFieldsException(failedValidation.toString());
            }
        }
        
        if (user.getPassword() != null && !user.getPassword().startsWith("{bcrypt}")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword("{bcrypt}" + encoder.encode(user.getPassword()));
        }
        
        user.setEnabled(Boolean.TRUE);
    }
}
