package org.loamok.jobs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.loamok.jobs.security.jwt.JwtService;
import org.loamok.jobs.util.SpringContextUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author Huby Franck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id", "name", "firstname", "email"})
@Inheritance(strategy = InheritanceType.JOINED)
@SuperBuilder
@Entity
@Table(name = "users", indexes = {
    @Index(columnList = "email"),
    @Index(columnList = "auth_token"),
    @Index(columnList = "email_verification_key"),
    @Index(columnList = "remember_me_token")
})
public class User implements UserDetails {
    @Id
    @Column(name = "id_user", length = 30)
    private Integer id;
    @Column(name = "name", nullable = false, length = 40)
    private String name;
    @Column(name = "firstname", nullable = false, length = 50)
    private String firstname;
    @Column(name = "email", nullable = false, length = 100, unique = true)
    private String email;
    @Column(name = "gdpr_optin", nullable = true)
    private Boolean gdprOptin;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    // -- relations
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role", nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Role role;
    // sécurité (Mot de passe, tokens, ...)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false, length = 69)
    private String password;
    @JsonIgnore
    @Column(name = "auth_token", nullable = true, columnDefinition = "TEXT")
    private String authToken;
    @JsonIgnore
    @Column(name = "remember_me_token", nullable = true, columnDefinition = "TEXT")
    private String rememberMeToken;
    @JsonIgnore
    @Column(name = "email_verification_key", nullable = true, columnDefinition = "TEXT")
    private String emailVerificationKey;
    @JsonIgnore
    @Transient
    private String authority;
    @JsonIgnore
    @Transient
    private Boolean isAdmin;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean enabled;

    @Override
    @Transient
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        authority = role.getRole();
        isAdmin = role.getIsAdmin();
        return Arrays.asList(new SimpleGrantedAuthority(authority));
    }

    public String getAuthority() {
        return role.getRole();
    }
    
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return (enabled != null && enabled) && (gdprOptin != null && gdprOptin);
    }

    @Override
    public boolean isAccountNonLocked() {
        return (enabled != null && enabled) && (gdprOptin != null && gdprOptin);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        if (authToken != null && !authToken.isEmpty()) {
            try {
                JwtService jwtService = SpringContextUtil.getBean(JwtService.class);
                boolean nonExpired = jwtService.isClientCredentialsTokenValid(this.authToken, this.email);
        
                return nonExpired;
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }

    @Override
    public boolean isEnabled() {
        return (enabled != null && enabled) && (gdprOptin != null && gdprOptin);
    }
    
    @JsonProperty("roleId")
    public Integer getRoleId() {
        return role != null ? role.getId() : null;
    }
}
