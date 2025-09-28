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
        User cleanUser = userManager.registerUser(user, false);
        // Copier les champs autorisés sur l'objet original
        user.setName(cleanUser.getName());
        user.setFirstname(cleanUser.getFirstname());
        user.setEmail(cleanUser.getEmail());
        user.setPassword(cleanUser.getPassword());
        user.setRole(cleanUser.getRole());
        user.setEnabled(cleanUser.getEnabled());
        user.setGdproptin(cleanUser.isGdproptin());
        
        user.setAuthToken(null);
        user.setRememberMeToken(null);
        user.setEmailVerificationKey(null);
    }
}
