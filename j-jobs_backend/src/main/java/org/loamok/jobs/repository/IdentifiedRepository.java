package org.loamok.jobs.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collection;
import java.util.List;
import java.util.function.LongSupplier;
import org.loamok.jobs.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Huby Franck
 */
public abstract class IdentifiedRepository {
    @PersistenceContext
    protected EntityManager em;
    protected final UserRepository userRepository;

    protected IdentifiedRepository(UserRepository userRepository) {
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

    // 🔧 Pagination utilitaire
    protected <T> Page<T> paginateQuery(TypedQuery<T> query,
                                        Pageable pageable,
                                        LongSupplier countSupplier) {
        List<T> content = query
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        long total = countSupplier.getAsLong();
        return new PageImpl<>(content, pageable, total);
    }
}
