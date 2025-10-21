package org.loamok.jobs.entity;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.loamok.jobs.repository.RoleRepository;
import org.loamok.jobs.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.assertj.core.api.Assertions;

/**
 *
 * @author Huby Franck
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
@Transactional
public class UserTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    private Role myRole;
    private User myUser;

    public UserTest() {
    }

    private Role buildARole() {
        Role newRole = Role.builder()
                .role("ROLE_USER")
                .isAdmin(Boolean.FALSE)
                .build();
        roleRepository.save(newRole);

        return newRole;
    }

    private User buildAUser() {
        return buildAUser("bip.bip@acme.com");
    }

    private User buildAUser(String email) {
        Role attachedRole = roleRepository.findById(myRole.getId()).orElseThrow();
        
        User newUser = User.builder()
                .email(email)
                .name("Runner")
                .firstname("road")
                .password("bip-bipMotherF!")
                .enabled(true)
                .gdproptin(true)
                .role(attachedRole)
                .build();

        userRepository.save(newUser);

        return newUser;
    }

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        myRole = buildARole();
        myUser = buildAUser();
    }

    @AfterEach
    public void tearDown() {
        myUser = null;
        myRole = null;

        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * Test of getAuthorities method, of class User.
     */
    @Test
    public void test_01_GetAuthorities() {
        System.out.println("getAuthorities");
        User instance = buildAUser("bip.bip2@acme.com");

        GrantedAuthority expectedAuthority = new SimpleGrantedAuthority(instance.getRole().getRole());
        Collection<? extends GrantedAuthority> expResult = Collections.singletonList(expectedAuthority);
        Collection<? extends GrantedAuthority> result = instance.getAuthorities();

        assertEquals(expResult, result);
    }

    /**
     * Test of getAuthority method, of class User (méthode utilitaire).
     */
    @Test
    public void test_02_GetAuthority() {
        System.out.println("getAuthority");
        User instance = buildAUser("bip.bip2@acme.com");
        String expResult = instance.getRole().getRole();
        String result = instance.getAuthority();

        assertEquals(expResult, result);
    }

    /**
     * Test of getUsername method (utilisant l'email), of class User.
     */
    @Test
    public void test_03_GetUsername() {
        System.out.println("getUsername");
        User instance = buildAUser("bip.bip2@acme.com");
        String expResult = instance.getEmail();
        String result = instance.getUsername();

        assertEquals(expResult, result);
    }

    /**
     * Test of isAccountNonExpired method, of class User.
     */
    @Test
    public void test_04_IsAccountNonExpired() {
        System.out.println("isAccountNonExpired");
        User instance = buildAUser("bip.bip2@acme.com");

        boolean expResult = true;
        boolean result = instance.isAccountNonExpired();

        assertEquals(expResult, result);
    }

    /**
     * Test of isAccountNonLocked method, of class User.
     */
    @Test
    public void test_05_IsAccountNonLocked() {
        System.out.println("isAccountNonLocked");
        User instance = buildAUser("bip.bip2@acme.com");

        boolean expResult = true;
        boolean result = instance.isAccountNonLocked();

        assertEquals(expResult, result);
    }

    /**
     * Test of isCredentialsNonExpired method, of class User.
     */
    @Test
    public void test_06_IsCredentialsNonExpired() {
        System.out.println("isCredentialsNonExpired");
        User instance = buildAUser("bip.bip2@acme.com");

        boolean expResult = false;
        boolean result = instance.isCredentialsNonExpired();

        assertEquals(expResult, result);
    }

    /**
     * Test of isEnabled method, of class User.
     */
    @Test
    public void test_07_IsEnabled() {
        System.out.println("isEnabled");
        User instance = buildAUser("bip.bip2@acme.com");

        boolean expResult = true;
        boolean result = instance.isEnabled();

        assertEquals(expResult, result);
    }

    /**
     * Test of getRoleId method, of class User.
     */
    @Test
    public void test_08_GetRoleId() {
        System.out.println("getRoleId");
        User instance = buildAUser("bip.bip2@acme.com");

        Integer expResult = myRole.getId();
        Integer result = instance.getRoleId();

        assertEquals(expResult, result);
    }

    /**
     * Test of builder method, of class User.
     */
    @Test
    public void test_09_Builder() {
        System.out.println("builder");
        User instance = buildAUser("bip.bip2@acme.com");
        Integer instId = instance.getId();
        String instEmail = instance.getEmail();
        
        Assertions.assertThat(instance).isNotNull();
        
        instance.setId(myUser.getId());
        instance.setEmail(myUser.getEmail());

        Assertions.assertThat(instance).isEqualTo(myUser);

        instance.setId(instId);
        instance.setEmail(instEmail);
    }

    /**
     * Test of getId method, of class User.
     */
    @Test
    public void test_10_GetId() {
        System.out.println("getId");

        Integer expResult = 2000;
        User instance = User.builder().id(expResult).build();

        assertEquals(expResult, instance.getId());
        
        instance = null;
    }

    /**
     * Test of getName method, of class User.
     */
    @Test
    public void test_11_GetName() {
        System.out.println("getName");
        User instance = buildAUser("bip.bip2@acme.com");
        String expResult = myUser.getName();
        String result = instance.getName();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getFirstname method, of class User.
     */
    @Test
    public void test_12_GetFirstname() {
        System.out.println("getFirstname");
        User instance = buildAUser("bip.bip2@acme.com");
        String expResult = myUser.getFirstname();
        String result = instance.getFirstname();

        assertEquals(expResult, result);
    }

    /**
     * Test of getEmail method, of class User.
     */
    @Test
    public void test_13_GetEmail() {
        System.out.println("getEmail");
        String instEmail = "bip.bip2@acme.com";
        User instance = buildAUser(instEmail);
        instance.setEmail(myUser.getEmail());
        String expResult = myUser.getEmail();
        String result = instance.getEmail();
        
        assertEquals(expResult, result);
        
        instance.setEmail(instEmail);
    }

    /**
     * Test of isGdproptin method, of class User.
     */
    @Test
    public void test_14_IsGdproptin() {
        System.out.println("isGdproptin");
        
        boolean expResult = true;
        boolean result = myUser.isGdproptin();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getCreatedAt method, of class User.
     */
    @Test
    public void test_15_GetCreatedAt() {
        System.out.println("getCreatedAt");
        User instanceA = buildAUser("bip.bipA@acme.com");

        Instant result = instanceA.getCreatedAt();
        
        Assertions.assertThat(result).isNotNull();
    }

    /**
     * Test of getUpdatedAt method, of class User.
     */
    @Test
    public void test_16_GetUpdatedAt() {
        System.out.println("getUpdatedAt");
        User instanceA = buildAUser("bip.bipA@acme.com");

        Instant result = instanceA.getUpdatedAt();
        
        Assertions.assertThat(result).isNotNull();
    }

    /**
     * Test of getRole method, of class User.
     */
    @Test
    public void test_17_GetRole() {
        System.out.println("getRole");
        Role attachedRole = roleRepository.findById(myRole.getId()).orElseThrow();
        User instance = buildAUser("bip.bipA@acme.com");
        
        Role expResult = myRole;
        Role result = instance.getRole();
        
        Assertions.assertThat(expResult).isEqualTo(result);
    }

    /**
     * Test of getPassword method, of class User.
     */
    @Test
    public void test_18_GetPassword() {
        System.out.println("getPassword");
        User instance = buildAUser("bip.bipA@acme.com");
        String expResult = myUser.getPassword();
        String result = instance.getPassword();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsAdmin method, of class User.
     */
    @Test
    public void test_19_GetIsAdmin() {
        System.out.println("getIsAdmin");
        User instance = buildAUser("bip.bipA@acme.com");
        
        Boolean expResult = myUser.getIsAdmin();
        Boolean result = instance.getIsAdmin();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getEnabled method, of class User.
     */
    @Test
    public void test_20_GetEnabled() {
        System.out.println("getEnabled");
        User instance = buildAUser("bip.bipA@acme.com");
        Boolean expResult = true;
        Boolean result = instance.getEnabled();
        
        assertEquals(expResult, result);
    }

    // --- Tests de Setters ---
    /**
     * Test of setId method, of class User.
     */
    @Test
    public void test_21_SetId() {
        System.out.println("setId");
        Integer id = 99;
        User instance = buildAUser("bip.bipA@acme.com");
        Integer instId = instance.getId();
        instance.setId(id);

        assertEquals(id, instance.getId());
        
        instance.setId(instId);
    }

    /**
     * Test of setName method, of class User.
     */
    @Test
    public void test_22_SetName() {
        System.out.println("setName");
        String name = "TestName";
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setName(name);

        assertEquals(name, instance.getName());
    }

    /**
     * Test of setFirstname method, of class User.
     */
    @Test
    public void test_23_SetFirstname() {
        System.out.println("setFirstname");
        String firstname = "TestFirstname";
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setFirstname(firstname);

        assertEquals(firstname, instance.getFirstname());
    }

    /**
     * Test of setEmail method, of class User.
     */
    @Test
    public void test_24_SetEmail() {
        System.out.println("setEmail");
        String email = "new.email@test.com";
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setEmail(email);

        assertEquals(email, instance.getEmail());
    }

    /**
     * Test of setGdproptin method, of class User.
     */
    @Test
    public void test_25_SetGdproptin() {
        System.out.println("setGdproptin");
        boolean gdproptin = false;
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setGdproptin(gdproptin);

        assertEquals(gdproptin, instance.isGdproptin());
    }

    /**
     * Test of setCreatedAt method, of class User.
     */
    @Test
    public void test_26_SetCreatedAt() {
        System.out.println("setCreatedAt");
        Instant createdAt = Instant.EPOCH;
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setCreatedAt(createdAt);

        assertEquals(createdAt, instance.getCreatedAt());
    }

    /**
     * Test of setUpdatedAt method, of class User.
     */
    @Test
    public void test_27_SetUpdatedAt() {
        System.out.println("setUpdatedAt");
        Instant updatedAt = Instant.EPOCH;
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setUpdatedAt(updatedAt);

        assertEquals(updatedAt, instance.getUpdatedAt());
    }

    /**
     * Test of setRole method, of class User.
     */
    @Test
    public void test_28_SetRole() {
        System.out.println("setRole");
        Role newRole = Role.builder().role("ROLE_ADMIN").isAdmin(true).build();
        User instance = buildAUser("bip.bipA@acme.com");
        Role oldRole = instance.getRole();
        instance.setRole(newRole);

        assertEquals(newRole, instance.getRole());
        
        newRole = null;
        instance.setRole(oldRole);
    }

    /**
     * Test of setPassword method, of class User.
     */
    @Test
    public void test_29_SetPassword() {
        System.out.println("setPassword");
        String password = "new-hashed-password";
        User instance = buildAUser("bip.bipA@acme.com");
        instance.setPassword(password);

        assertEquals(password, instance.getPassword());
    }

    // --- Tests d'utilité et de convention (equals, hashCode, toString) ---
    /**
     * Test of equals method, of class User (vs null et vs lui-même).
     */
    @Test
    public void test_30_Equals() {
        System.out.println("equals");
        User instance = buildAUser("bip.bipA@acme.com");

        assertTrue(instance.equals(instance));
        assertFalse(instance.equals(new Object()));

        User identicalInstance = myUser;
        Integer instId = instance.getId();
        String instEmail = instance.getEmail();
        
        instance.setId(myUser.getId());
        instance.setEmail(myUser.getEmail());
        
        assertTrue(instance.equals(identicalInstance));
        
        instance.setId(instId);
        instance.setEmail(instEmail);
    }

    /**
     * Test of canEqual method, of class User.
     */
    @Test
    public void test_31_CanEqual() {
        System.out.println("canEqual");
        User instance = buildAUser("bip.bipA@acme.com");
        Integer instId = instance.getId();
        String instEmail = instance.getEmail();
        
        instance.setId(myUser.getId());
        instance.setEmail(myUser.getEmail());

        assertTrue(instance.canEqual(myUser));
        assertFalse(instance.canEqual(new Object()));
        
        instance.setId(instId);
        instance.setEmail(instEmail);
    }

    /**
     * Test of hashCode method, of class User.
     */
    @Test
    public void test_32_HashCode() {
        System.out.println("hashCode");
        User instance = buildAUser("bip.bipA@acme.com");
        Integer instId = instance.getId();
        String instEmail = instance.getEmail();
        
        instance.setId(myUser.getId());
        instance.setEmail(myUser.getEmail());

        int result = instance.hashCode();
        int expResult = myUser.hashCode();

        assertEquals(expResult, result);
        Assertions.assertThat(result).isNotEqualTo(0);
        
        instance.setId(instId);
        instance.setEmail(instEmail);
    }

    /**
     * Test of toString method, of class User.
     */
    @Test
    public void test_33_ToString() {
        System.out.println("toString");
        User instance = buildAUser("bip.bipA@acme.com");
        Integer instId = instance.getId();
        String instEmail = instance.getEmail();
        
        instance.setId(myUser.getId());
        instance.setEmail(myUser.getEmail());

        int result = instance.hashCode();
        int expResult = myUser.hashCode();

        Assertions.assertThat(result).isEqualTo(expResult);
        
        instance.setId(instId);
        instance.setEmail(instEmail);

    }

}
