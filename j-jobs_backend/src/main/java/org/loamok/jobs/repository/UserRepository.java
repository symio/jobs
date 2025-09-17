package org.loamok.jobs.repository;

import java.util.List;
import org.loamok.jobs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 *
 * @author Huby Franck
 */
@RepositoryRestResource(collectionResourceRel = "users", path = "users")
public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);
    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
    @RestResource(path = "findByEmailContaining", rel = "findByEmailContaining")
    List<User> findByEmailContaining(@Param("email") String email);
    @RestResource(path = "findByNameContaining", rel = "findByNameContaining") 
    List<User> findByNameContaining(@Param("name") String name);
    @RestResource(path = "findByRole", rel = "findByRole")
    List<User> findByRole_Role(@Param("role") String role);
}
