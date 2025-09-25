package org.loamok.jobs.security.oauth2;

import io.jsonwebtoken.Claims;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.repository.UserRepository;
import org.loamok.jobs.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
 */
@Service
@AllArgsConstructor
public class OAuth2Service {

    private UserRepository uR;
    private PasswordEncoder pE;
    private JwtService jwtService;

    public Optional<OAuth2TokenResponse> generateClientCredentialsToken(String email, String clientSecret, String requestedScopes, String clientSignature) {
        Set<String> scopeSet = Arrays.stream(requestedScopes.split("\\s+"))
                .map(String::trim)
                .collect(Collectors.toSet());

        String rememberMeToken = null;

        // Utiliser les utilisateurs existants comme "clients" OAuth2
        User user = uR.findByEmail(email);

        if (!scopeSet.contains("remembered")
                && !scopeSet.contains("refresh")
                && (user == null
                || !pE.matches(clientSecret, user.getPassword())
                || !(user.isAccountNonExpired() && user.isAccountNonLocked() && user.isEnabled()))) {
            if (user != null) {
                user.setAuthToken(null);
                user.setRememberMeToken(null);
                uR.save(user);
            }

            return Optional.empty();
        } else if (scopeSet.contains("refresh")) {
            if (!jwtService.isClientCredentialsTokenValid(clientSecret, email)
                    || user == null
                    || user.getRememberMeToken() == null
                    || !(user.isAccountNonExpired() && user.isAccountNonLocked() && user.isEnabled())) {
                return Optional.empty();
            }

            Claims refreshedClaims = jwtService.extractAllClaimsForced(user.getAuthToken());
            if (!clientSignature.equals(refreshedClaims.get("client-signature"))) {
                user.setAuthToken(null);
                user.setRememberMeToken(null);
                uR.save(user);
                return Optional.empty();
            }
        } else if (scopeSet.contains("remembered")) {
            scopeSet.add("rememberme");
            if (!jwtService.isClientCredentialsTokenValid(clientSecret, email)
                    || user == null
                    || user.getRememberMeToken() == null
                    || !(user.isAccountNonExpired() && user.isAccountNonLocked() && user.isEnabled())) {
                return Optional.empty();
            }

            Claims rememberedClaims = jwtService.extractAllClaims(user.getRememberMeToken());
            if (!clientSignature.equals(rememberedClaims.get("client-signature"))) {
                user.setAuthToken(null);
                user.setRememberMeToken(null);
                uR.save(user);
                return Optional.empty();
            }
        }

        // Scope unique "access" pour tous les utilisateurs authentifiés
        String scope = "access";

        // Créer les claims pour les tokens OAuth2
        Map<String, Object> claims = genClaimsForToken(email, scope, clientSignature, "client_credentials", user);
        Map<String, Object> claimsAuth = new HashMap<>();

        claimsAuth.putAll(claims);
        claimsAuth.remove("client-signature");

        // Générer les tokens
        final String storedToken = jwtService.generateClientCredentialsToken(claims, email, 2);
        user.setAuthToken(storedToken);

        claimsAuth.put("originally_expires", jwtService.extractExpiration(storedToken));

        int expirationHours;
        if (scopeSet.contains("refresh")) {
            // En cas de refresh, calculer le différentiel pour conserver la même expiration
            Date originalExpiration = jwtService.extractExpiration(clientSecret);
            long currentTimeMillis = System.currentTimeMillis();
            long remainingTimeMillis = originalExpiration.getTime() - currentTimeMillis;
            expirationHours = Math.max(2, (int) (remainingTimeMillis / (1000 * 60 * 60))); // Minimum 2 heures
        } else {
            expirationHours = 24; // Valeur par défaut
        }

        final String authToken = jwtService.generateClientCredentialsToken(claimsAuth, email, expirationHours);

        if (scopeSet.contains("rememberme")) {
            Map<String, Object> claimsRemember = new HashMap<>();
            claims.remove("scope");
            claims.put("scope", "rememberme");
            claimsRemember.putAll(claims);
            claimsRemember.remove("client-signature");
            final String storedRememberToken = jwtService.generateClientCredentialsToken(claims, email, expirationHours * 365);
            rememberMeToken = jwtService.generateClientCredentialsToken(claimsRemember, email, expirationHours * 365);
            user.setRememberMeToken(storedRememberToken);
        }

        uR.save(user);

        return Optional.of(new OAuth2TokenResponse(authToken, rememberMeToken, "Bearer", expirationHours * 60 * 60, jwtService.extractExpiration(storedToken).getTime(), scope));
    }
    private Map<String, Object> genClaimsForToken(String email, String scope, String clientSignature, String tokenType, User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", email);
        claims.put("scope", scope);
        claims.put("token_type", tokenType);
        claims.put("authority", user.getAuthority()); // Le rôle de l'utilisateur
        claims.put("isAdmin", user.getRole().getIsAdmin()); // Optionnel pour des vérifications rapides
        claims.put("client-signature", clientSignature);

        return claims;
    }
}
