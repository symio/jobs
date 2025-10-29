package org.loamok.jobs.web;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.loamok.jobs.security.jwt.JwtService;
import org.loamok.jobs.security.jwt.RememberedTokenRequest;
import org.loamok.jobs.security.jwt.TokenRequest;
import org.loamok.jobs.security.jwt.TokenResponse;
import org.loamok.jobs.security.oauth2.OAuth2Service;
import org.loamok.jobs.security.oauth2.OAuth2TokenResponse;
import org.loamok.jobs.util.ClientSignatureUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Huby Franck
 */
@RestController
@RequestMapping("/authorize")
@AllArgsConstructor
public class AuthenticationController {

    private final OAuth2Service oauth2Service;
    private final ClientSignatureUtil csb;
    private final JwtService jwtService;
    
    @Operation(
            summary = "Obtenir un token d'accès OAuth2",
            description = "Authentifie un client via le flow Client Credentials et retourne un JWT access_token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Paramètres OAuth2 au format application/x-www-form-urlencoded",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema = @Schema(implementation = TokenRequest.class)
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Token généré avec succès",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = TokenResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Paramètres manquants ou invalides"
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Client ID ou secret invalide"
        )
    })
    @PostMapping(value = "/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> oauth2Token(
            @Parameter(description = "Données d'authentification OAuth2", required = true)
            @Valid TokenRequest tokenRequest,
            HttpServletRequest request
    ) {
        String grantType = tokenRequest.getGrant_type();
        String clientId = tokenRequest.getClient_id();
        String clientSecret = tokenRequest.getClient_secret();
        String scope = tokenRequest.getScope();

        // Validation du grant type
        if (!"client_credentials".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "unsupported_grant_type",
                    "error_description", "Grant type must be client_credentials"
            ));
        }

        // Validation des paramètres
        if (clientId == null || clientSecret == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "invalid_request",
                    "error_description", "client_id and client_secret are required"
            ));
        }

        // Générer le token
        String clientSignature = csb.buildClientSignature(request);
        Optional<OAuth2TokenResponse> tokenOpt = oauth2Service.generateClientCredentialsToken(clientId, clientSecret, scope, clientSignature);

        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "invalid_client",
                    "error_description", "Invalid client credentials or unauthorized scopes or disabled client"
            ));
        }

        // Réponse OAuth2 standard
        OAuth2TokenResponse tokenResponse = tokenOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", tokenResponse.getAccessToken());
        response.put("remember_me_token", tokenResponse.getRememberMeToken());
        response.put("token_type", tokenResponse.getTokenType());
        response.put("expires_in", tokenResponse.getExpiresIn());
        response.put("originally_expires", tokenResponse.getOriginallyExpires());
        
        if (tokenResponse.getScope() != null) {
            response.put("scope", tokenResponse.getScope());
        }

        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Rafraîchir un token d'accès via un access_token non expiré",
            description = "Permet de générer un nouveau `auth stored_token` OAuth2 à partir d'un `auth_access_token` précédemment émis.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Objet JSON contenant le remember_me_token",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RememberedTokenRequest.class)
                )
            )
        )
        @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200",
                description = "Token généré avec succès",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = TokenResponse.class)
                )
            ),
            @ApiResponse(
            responseCode = "400",
            description = "Paramètres manquants ou invalides",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(
                    type = "object",
                    example = "{ \"error\": \"invalid_request\", \"error_description\": \"client_id and client_secret are required\" }"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token invalide",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(
                    type = "object",
                    example = "{ \"error\": \"invalid_client\", \"error_description\": \"Invalid client credentials or unauthorized scopes or disabled client\" }"
                )
            )
        )
    })
    @PostMapping(value = "/refresh",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> oauth2Refresh(
        @Parameter(description = "Objet contenant le auth_token valide", required = true)
        @Valid @RequestBody RememberedTokenRequest rememberedTokenRequest,
        HttpServletRequest request
    ) {
        try {
            Claims claims = jwtService.extractAllClaims(rememberedTokenRequest.getRememberMeToken());

            TokenRequest tokenRequest = new TokenRequest();
            tokenRequest.setGrant_type(claims.get("token_type", String.class));
            tokenRequest.setClient_id(claims.get("client_id", String.class));
            tokenRequest.setClient_secret(rememberedTokenRequest.getRememberMeToken());
            tokenRequest.setScope("access refresh");

            return oauth2Token(tokenRequest, request);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_request",
                "error_description", "remember_me_token manquant ou invalide - " + e.getMessage()
            ));
        }
    }
    
    @Operation(
            summary = "Rafraîchir un token d'accès via un remember_me_token",
            description = "Permet d'obtenir un nouveau `access_token` OAuth2 à partir d'un `remember_me_token` précédemment émis.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Objet JSON contenant le remember_me_token",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RememberedTokenRequest.class)
                    )
            )
    )
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Token généré avec succès",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = TokenResponse.class)
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Paramètres manquants ou invalides"
        )
    })
    @PostMapping(value = "/remembered",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> oauth2Remembered(
            @Parameter(description = "Objet contenant le remember_me_token valide", required = true)
            @Valid @RequestBody RememberedTokenRequest rememberedTokenRequest,
            HttpServletRequest request
    ) {
        Claims claims = jwtService.extractAllClaims(rememberedTokenRequest.getRememberMeToken());
        
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setGrant_type(claims.get("token_type", String.class));
        tokenRequest.setClient_id(claims.get("client_id", String.class));
        tokenRequest.setClient_secret(rememberedTokenRequest.getRememberMeToken());
        tokenRequest.setScope("access remembered");
        
        return oauth2Token(tokenRequest, request);
    }
    
    @Operation(
        summary = "Déconnexion - Nettoie les tokens stockés",
        description = "Supprime tous les tokens d'authentification stockés côté serveur (authToken et rememberMeToken) pour déconnecter l'utilisateur. Cette opération est irréversible.",
        security = { @SecurityRequirement(name = "Bearer Authentication") }, 
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Objet JSON contenant un token d'authentification (access_token ou remember_me_token)",
                required = true,
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = RememberedTokenRequest.class)
                )
        )
)
@ApiResponses(value = {
    @ApiResponse(
            responseCode = "200",
            description = "Déconnexion effectuée avec succès. Tous les tokens ont été supprimés.",
            content = @Content()
    ),
    @ApiResponse(
            responseCode = "400",
            description = "Token manquant ou invalide",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(
                            type = "object",
                            example = "{ \"error\": \"invalid_request\", \"error_description\": \"Token manquant ou invalide\" }"
                    )
            )
    )
})
@PostMapping(value = "/cleanup",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Void> oauth2Cleanup(
        @Parameter(description = "Objet contenant le token d'authentification", required = true)
        @Valid @RequestBody RememberedTokenRequest rememberedTokenRequest,
        HttpServletRequest request
) {
    try {
        Claims claims = jwtService.extractAllClaimsForced(rememberedTokenRequest.getRememberMeToken());
        
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setGrant_type(claims.get("token_type", String.class));
        tokenRequest.setClient_id(claims.get("client_id", String.class));
        tokenRequest.setClient_secret(rememberedTokenRequest.getRememberMeToken());
        tokenRequest.setScope("cleanup");
        
        String clientSignature = csb.buildClientSignature(request);
        oauth2Service.generateClientCredentialsToken(
                tokenRequest.getClient_id(), 
                tokenRequest.getClient_secret(), 
                tokenRequest.getScope(), 
                clientSignature
        );
        
        return ResponseEntity.ok().build();
        
    } catch (Exception e) {
        return ResponseEntity.badRequest().build();
    }
}
}
