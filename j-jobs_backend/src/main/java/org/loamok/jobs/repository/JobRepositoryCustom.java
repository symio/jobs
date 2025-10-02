package org.loamok.jobs.repository;

import java.util.List;
import java.util.Optional;
import org.loamok.jobs.entity.Job;
import org.loamok.jobs.enums.OfferStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 *
 * @author Huby Franck
 */
public interface JobRepositoryCustom {

    List<Job> findAllFilteredForCurrentUser();
    Page<Job> findAllFilteredForCurrentUser(Pageable pageable);
    Optional<Job> findByIdFilteredForCurrentUser(Integer id);
    long countFilteredForCurrentUserByOfferStatus(OfferStatusEnum offerStatus);
}
