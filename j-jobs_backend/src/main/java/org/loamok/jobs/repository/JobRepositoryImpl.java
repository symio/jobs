package org.loamok.jobs.repository;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import java.util.Optional;
import org.loamok.jobs.dto.JobsDto;
import org.loamok.jobs.entity.Job;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.enums.OfferStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 *
 * @author Huby Franck
 */
public class JobRepositoryImpl extends IdentifiedRepository implements JobRepositoryCustom {

    public JobRepositoryImpl(UserRepository userRepository) {
        super(userRepository);
    }

    @Override
    public List<JobsDto.StatusCountProjection> countAllGroupedByStatusForCurrentUser() {
        User user = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();

        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(JobsDto.StatusCountProjection.class);
        var root = query.from(Job.class);

        Predicate securityPredicate = SecuritySpecifications
                .<Job>belongsToUserOrAdmin(user, adminAccess)
                .toPredicate(root, query, cb);

        query.where(securityPredicate);

        query.groupBy(root.get("offerStatus"));

        query.select(cb.construct(
                JobsDto.StatusCountProjection.class,
                root.get("offerStatus"),
                cb.count(root)
        ));

        return em.createQuery(query).getResultList();
    }

    private long count(Specification<Job> spec) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var root = query.from(Job.class);
        query.select(cb.count(root));

        // Applique la Specification pour filtrer le compte
        query.where(spec.toPredicate(root, query, cb));

        return em.createQuery(query).getSingleResult();
    }

    @Override
    public Page<Job> findBySearch(Specification<Job> spec, Pageable pageable) {
        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Job.class);
        var root = query.from(Job.class);

        query.where(spec.toPredicate(root, query, cb));

        query.orderBy(
                pageable.getSort().stream()
                        .map(order -> order.isAscending() ? cb.asc(root.get(order.getProperty())) : cb.desc(root.get(order.getProperty())))
                        .toList()
        );

        TypedQuery<Job> typedQuery = em.createQuery(query);

        return paginateQuery(typedQuery, pageable, () -> count(spec));
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

    @Override
    public long countFilteredForCurrentUserByOfferStatus(OfferStatusEnum offerStatus) {
        User user = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();

        var cb = em.getCriteriaBuilder();
        var query = cb.createQuery(Long.class);
        var root = query.from(Job.class);
        query.select(cb.count(root));

        Predicate securityPredicate = SecuritySpecifications
                .<Job>belongsToUserOrAdmin(user, adminAccess)
                .toPredicate(root, query, cb);

        Predicate statusPredicate = cb.equal(root.get("offerStatus"), offerStatus);
        Predicate finalPredicate = cb.and(securityPredicate, statusPredicate);

        query.where(finalPredicate);

        return em.createQuery(query).getSingleResult();
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
