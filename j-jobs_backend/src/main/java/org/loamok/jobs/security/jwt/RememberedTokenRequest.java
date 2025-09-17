package org.loamok.jobs.security.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Requête de token OAuth2 depuis un token Remember Me")
@Data
public class RememberedTokenRequest {
    
    @Schema(description = "Token rememberMe", example = "eyJhbGciOiJIUzI1NiIs...", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @JsonProperty("remember_me_token")
    private String rememberMeToken;
    
}
