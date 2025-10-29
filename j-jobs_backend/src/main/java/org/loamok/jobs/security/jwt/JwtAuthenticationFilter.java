package org.loamok.jobs.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.repository.UserRepository;
import org.loamok.jobs.util.ClientSignatureBuilder;
import org.loamok.jobs.util.ClientSignatureUtil;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 *
 * @author Huby Franck
 */
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private UserRepository userRepository;
    private ClientSignatureUtil csb;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        logger.info("========== JWT FILTER ==========");
        logger.info("URI: " + request.getRequestURI());
        logger.info("Method: " + request.getMethod());
        logger.info("Authorization header: " + request.getHeader("Authorization"));

        // Vérifier si l'authentification existe déjà
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        if (existingAuth != null) {
            logger.info("Authentication déjà présente: " + existingAuth.getName());
            logger.info("Authorities: " + existingAuth.getAuthorities());
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        boolean isAuth = handleClientCredentialsToken(jwt, request);

        if (!isAuth) {
            filterChain.doFilter(request, response);
            return;
        }

        setAuthentication(jwt);

        filterChain.doFilter(request, response);
    }

    private boolean handleClientCredentialsToken(String jwt, HttpServletRequest request) {
        try {
            final String clientId = jwtService.extractUserName(jwt);
            String scopes = jwtService.extractScopes(jwt);

            if (clientId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByEmail(clientId);

                String clientSignature = csb.buildClientSignature(request);

                String authTokenSignature = jwtService.extractAllClaims(user.getAuthToken()).get("client-signature", String.class);

                if (user.getAuthToken() != null
                        && jwtService.isClientCredentialsTokenValid(jwt, clientId)
                        && jwtService.isClientCredentialsTokenValid(user.getAuthToken(), clientId)
                        && (authTokenSignature != null && authTokenSignature.equals(clientSignature))) {
                    Collection<GrantedAuthority> authorities = Arrays.stream(scopes.split(" "))
                            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken
                            = new UsernamePasswordAuthenticationToken(clientId, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification JWT", e);
        }

        return false;
    }

    private void setAuthentication(String token) {
        Claims claims = jwtService.extractAllClaims(token);
        String clientId = claims.get("client_id", String.class);
        String scope = claims.get("scope", String.class);
        String authority = claims.get("authority", String.class); // Le rôle

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        if ("access".equals(scope)) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_access"));
        }
        if ("admin".equals(scope)) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_admin"));
        }

        if (authority != null) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                clientId, null, authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
