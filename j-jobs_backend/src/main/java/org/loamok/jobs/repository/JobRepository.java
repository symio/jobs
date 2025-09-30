package org.loamok.jobs.repository;

import java.util.List;
import java.util.Optional;
import org.loamok.jobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 *
 * @author Huby Franck
 */
@RepositoryRestResource(collectionResourceRel = "jobs", path = "jobs")
public interface JobRepository extends JpaRepository<Job, Integer>, 
                                        JpaSpecificationExecutor<Job>, 
                                        JobRepositoryCustom {
    @Override
    @RestResource(exported = false)
    List<Job> findAll();

    @RestResource(path = "secure")
    default List<Job> secureFindAll() {
        return findAllFilteredForCurrentUser();
    }

    @Override
    default Page<Job> findAll(Pageable pageable) {
        return findAllFilteredForCurrentUser(pageable);
    }

    @Override
    default Optional<Job> findById(Integer id) {
        return findByIdFilteredForCurrentUser(id);
    }
}
