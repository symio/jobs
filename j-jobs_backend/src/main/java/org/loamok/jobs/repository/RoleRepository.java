package org.loamok.jobs.repository;

import org.loamok.jobs.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 *
 * @author Huby Franck
 */
@RepositoryRestResource(collectionResourceRel = "roles", path = "roles")
public interface RoleRepository extends JpaRepository<Role, Integer> {
    @Query
    @RestResource(path = "findByRole", rel = "findByRole")
    Role findByRole(@Param("role") String role);
}
