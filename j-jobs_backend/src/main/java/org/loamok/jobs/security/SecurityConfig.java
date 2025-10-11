package org.loamok.jobs.security;

import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.logging.*;
import org.loamok.jobs.security.jwt.JwtAuthenticationFilter;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${CORS_ALLOWED_ORIGINS}")
    private String allowedOriginsEnv;

    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain oauth2ApiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/jobs/**")
                .authorizeHttpRequests(auth -> auth
                // APIs ouvertes au public sans authentification
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                .requestMatchers(HttpMethod.POST, "/profil/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/authorize/token").permitAll()
                .requestMatchers(HttpMethod.POST, "/authorize/refresh").permitAll()
                // Sauf pour le cleanup qui sert de déconnexion
                .requestMatchers(HttpMethod.POST, "/authorize/cleanup").permitAll()
                //                .requestMatchers(HttpMethod.POST, "/authorize/remembered").permitAll()
                // SpringDoc OpenAPI / Swagger UI endpoints - Documentation API accessible publiquement
                .requestMatchers("/v3/api-docs/**").permitAll() // Spécification OpenAPI 3.0 en JSON/YAML
                .requestMatchers("/swagger-ui/**").permitAll() // Interface utilisateur Swagger (HTML, CSS, JS)
                .requestMatchers("/swagger-ui.html").permitAll() // Page principale de l'interface Swagger
                .requestMatchers("/swagger-resources/**").permitAll() // Métadonnées et configuration Swagger
                .requestMatchers("/webjars/**").permitAll() // Librairies JavaScript/CSS (Bootstrap, jQuery, etc.)

                // Rôles - Administration des rôles (Admin uniquement)
                .requestMatchers(HttpMethod.GET, "/roles", "/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/roles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/roles/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/roles/**").hasRole("ADMIN")
                // Profils - Consultation des profils (Admin seulement pour la gestion globale)
                .requestMatchers(HttpMethod.GET, "/profile").hasRole("ADMIN")
                // Utilisateurs - Gestion des entités en général
                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("USER", "ADMIN")
                // Tout le reste nécessite authentification + scope
                .anyRequest().access(this::hasAccessScopeAndAuthenticated)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));
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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = List.of(allowedOriginsEnv.split(","));
        config.setAllowedOrigins(allowedOrigins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Cache-Control",
                "Pragma",
                "X-Forwarded-For",
                "X-Forwarded-Proto",
                "X-Real-IP"
        ));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
