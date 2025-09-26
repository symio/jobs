package org.loamok.jobs.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Réponse contenant le token d'accès OAuth2")
@Data
public class TokenResponse {
    @Schema(description = "Token d'accès JWT", example = "eyJhbGciOiJIUzI1NiIs...")
    private String access_token;
    @Schema(description = "Token JWT pour reconnection automatique", example = "eyJhbGciOiJIUzI1NiIs...")
    private String remember_me_token;
    @Schema(description = "Type de token", example = "Bearer")
    private String token_type;
    @Schema(description = "Durée de validité en secondes", example = "3600")
    private Long expires_in;
    @Schema(description = "Portée du token", example = "access")
    private String scope;
    
}
