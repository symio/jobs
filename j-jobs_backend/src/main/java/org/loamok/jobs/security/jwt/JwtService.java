package org.loamok.jobs.security.jwt;

import java.security.Key;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String SECRET_KEY;

    // Signature transmise pour la création du jeton.
    // Et chiffrer/déchiffrer les données du jeton
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaimsForced(String token) {
        try {
            return Jwts.parser()
                    .verifyWith((SecretKey) getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException expired) {
            return expired.getClaims();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JWT token: " + e.getMessage(), e);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSignInKey()).build()
                .parseSignedClaims(token).getPayload();
    }

    // Extraire 1 « claims » du jeton
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extraire le pseudo du jeton
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateClientCredentialsToken(Map<String, Object> claims, String email, int hours) {
        JwtBuilder builder = Jwts.builder();

        builder.subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ((1000L * 60 * 60) * hours))); // date et heure actuel + hours heures

        if (claims != null && !claims.isEmpty()) {
            builder.claims(claims);
        }

        return builder.signWith(getSignInKey()).compact();
    }

    // Vérifier si c'est un token OAuth2 Client Credentials
    public boolean isClientCredentialsToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "client_credentials".equals(claims.get("token_type"));
        } catch (Exception e) {
            return false;
        }
    }

    // Valider un token client credentials
    public boolean isClientCredentialsTokenValid(String token, String email) {
        try {
            final String tokenClientId = extractUserName(token);
            return (tokenClientId.equals(email) && !isTokenExpired(token) && isClientCredentialsToken(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Extraire les scopes OAuth2
    public String extractScopes(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.get("scope");
        } catch (Exception e) {
            return null;
        }
    }

    // Extraire l'authority original du token OAuth2
    public String extractAuthority(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.get("authority");
        } catch (Exception e) {
            return null;
        }
    }

}
