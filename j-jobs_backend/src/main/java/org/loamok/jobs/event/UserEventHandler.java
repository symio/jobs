package org.loamok.jobs.event;

import org.loamok.jobs.entity.User;
import org.loamok.jobs.manager.UserManager;
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

    @HandleBeforeCreate
    public void handleUtilisateurCreate(User user) {
        if (!userManager.doCheckUserRegistering(user))
            throw new RuntimeException("User invalide");
        
        if (user.getPassword() != null && !user.getPassword().startsWith("{bcrypt}")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            user.setPassword("{bcrypt}" + encoder.encode(user.getPassword()));
        }
    }
}
