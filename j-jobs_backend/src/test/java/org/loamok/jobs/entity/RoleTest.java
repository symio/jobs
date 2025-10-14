package org.loamok.jobs.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.loamok.jobs.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Huby Franck
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
public class RoleTest {
    
    @Autowired
    RoleRepository roleRepository;
    
    @Test
    void test01_populateRoles() {
        roleRepository.deleteAll();
        
        List<Role> entities = new ArrayList<>();
        
        Role roleUser = Role.builder()
                .role("ROLE_USER")
                .isAdmin(Boolean.FALSE)
                .build();
        
        entities.add(roleUser);
        
        Role roleAdmin = Role.builder()
                .role("ROLE_ADMIN")
                .isAdmin(Boolean.TRUE)
                .build();
        
        entities.add(roleAdmin);
        
        roleRepository.saveAllAndFlush(entities);
        
        final Role userRoleDB = roleRepository.findByRole(roleUser.getRole());
        assertThat(userRoleDB).isNotNull();
        assertThat(userRoleDB.getId()).isGreaterThan(0);
        
        final Role adminRoleDB = roleRepository.findByRole(roleAdmin.getRole());
        assertThat(adminRoleDB).isNotNull();
        assertThat(adminRoleDB.getId()).isGreaterThan(0);
    
    }

}
