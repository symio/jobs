package org.loamok.jobs.repository;

import org.loamok.jobs.entity.User;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author Huby Franck
 */
public class SecuritySpecifications {
    private SecuritySpecifications() {}

    public static <T> Specification<T> belongsToUserOrAdmin(User user, boolean isAdmin) {
        return (root, query, cb) -> {
            if (isAdmin) {
                return cb.conjunction();
            }
            return cb.equal(root.get("user"), user);
        };
    }

    public static <T> Specification<T> byIdAndBelongsToUserOrAdmin(Integer id, User user, boolean isAdmin) {
        return (root, query, cb) -> {
            if (isAdmin) {
                return cb.equal(root.get("id"), id);
            }
            return cb.and(
                cb.equal(root.get("id"), id),
                cb.equal(root.get("user"), user)
            );
        };
    }
}
