package org.loamok.jobs.event;

import java.util.Collection;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author symio
 */
public abstract class IdentifiedHandler {
    protected final UserRepository userRepository;

    protected IdentifiedHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 🔑 Auth helpers
    protected Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    protected String getClientId() {
        return (String) getAuth().getPrincipal();
    }

    protected Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuth().getAuthorities();
    }

    protected boolean hasScope(String scope) {
        return getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_" + scope));
    }

    protected User getCurrentUser() {
        return userRepository.findByEmail(getClientId());
    }

    protected boolean isAdminWithScopeAdmin() {
        User user = getCurrentUser();
        return Boolean.TRUE.equals(user.getRole().getIsAdmin()) && hasScope("admin");
    }
}
