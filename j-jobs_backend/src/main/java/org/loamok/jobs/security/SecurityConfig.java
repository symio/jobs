package org.loamok.jobs.security;

import java.util.function.Supplier;
import org.apache.commons.logging.*;
import org.loamok.jobs.repository.UserRepository;
import org.loamok.jobs.security.jwt.JwtAuthenticationFilter;
import org.loamok.jobs.security.jwt.JwtService;
import org.loamok.jobs.util.ClientSignatureBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Value("${springdoc.api-docs.enabled:false}")
    private boolean swaggerEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            UserRepository userRepository,
            ClientSignatureBuilder csb
    ) throws Exception {
        logger.info("============================================================");
        logger.info("CHARGEMENT SECURITY FILTER CHAIN UNIQUE");
        logger.info("============================================================");

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(
            jwtService,
            userDetailsService,
            userRepository,
            csb
        );

        http
            .authorizeHttpRequests(auth -> {
                logger.info("Configuration des règles d'autorisation...");
                
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                
                auth.requestMatchers(HttpMethod.POST, 
                    "/authorize/token",
                    "/authorize/refresh", 
                    "/authorize/cleanup",
                    "/authorize/remembered",
                    "/profil/register",
                    "/users"
                ).permitAll();
                
                if (swaggerEnabled) {
                    auth.requestMatchers(
                        "/v3/api-docs/**", 
                        "/swagger-ui/**", 
                        "/swagger-ui.html", 
                        "/swagger-resources/**", 
                        "/webjars/**"
                    ).permitAll();
                }
                
                // 4. Routes ADMIN uniquement (nécessitent ROLE_ADMIN)
                auth.requestMatchers(HttpMethod.GET, "/roles/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.POST, "/roles/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PUT, "/roles/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.PATCH, "/roles/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/roles/**").hasRole("ADMIN");
                
                auth.requestMatchers(HttpMethod.GET, "/profile/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.GET, "/users/**").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN");
                
                // 5. Toutes les autres requêtes nécessitent le scope "access"
                auth.anyRequest().access(this::hasAccessScopeAndAuthenticated);
            })
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource));

        return http.build();
    }

    /**
     * Vérifie que l'utilisateur a le scope "access" ET est authentifié Cette
     * méthode ajoute une couche de sécurité supplémentaire
     */
    private AuthorizationDecision hasAccessScopeAndAuthenticated(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // Vérifier que l'utilisateur a le scope "access"
        boolean hasAccessScope = auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("SCOPE_access"));

        return new AuthorizationDecision(hasAccessScope);
    }

}
