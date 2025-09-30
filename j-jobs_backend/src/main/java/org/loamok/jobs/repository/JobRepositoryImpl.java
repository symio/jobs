package org.loamok.jobs.repository;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;
import org.loamok.jobs.entity.Job;
import org.loamok.jobs.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Huby Franck
 */
public class JobRepositoryImpl extends IdentifiedRepository implements JobRepositoryCustom {

    public JobRepositoryImpl(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public List<Job> findAllFilteredForCurrentUser() {
        return findAllFilteredForCurrentUser(Pageable.unpaged()).getContent();
    }

    @Override
    public Page<Job> findAllFilteredForCurrentUser(Pageable pageable) {
        User user = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();

        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Job.class);
        var root = query.from(Job.class);

        // Utilise ta Specification existante
        Predicate predicate = SecuritySpecifications
                .<Job>belongsToUserOrAdmin(user, adminAccess)
                .toPredicate(root, query, cb);

        query.where(predicate);

        TypedQuery<Job> typedQuery = em.createQuery(query);
        return paginateQuery(typedQuery, pageable, () -> countJobs(user, adminAccess));
    }

    @Override
    public Optional<Job> findByIdFilteredForCurrentUser(Integer id) {
        User user = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();

        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Job.class);
        var root = query.from(Job.class);

        Predicate predicate = SecuritySpecifications
                .<Job>byIdAndBelongsToUserOrAdmin(id, user, adminAccess)
                .toPredicate(root, query, cb);

        query.where(predicate);

        return em.createQuery(query).getResultStream().findFirst();
    }

    private long countJobs(User user, boolean adminAccess) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var root = query.from(Job.class);
        query.select(cb.count(root));

        Predicate predicate = SecuritySpecifications
                .<Job>belongsToUserOrAdmin(user, adminAccess)
                .toPredicate(root, query, cb);

        query.where(predicate);

        return em.createQuery(query).getSingleResult();
    }

}
