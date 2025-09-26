package org.loamok.jobs.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Requête de token OAuth2")
@Data
public class TokenRequest {
    
    @Schema(description = "Type de grant", example = "client_credentials", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String grant_type;
    @Schema(description = "Identifiant du client", example = "my-client-id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String client_id;
    @Schema(description = "Secret du client", example = "my-client-secret", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String client_secret;
    @Schema(description = "Portée demandée", example = "access rememberme")
    private String scope;
    
}
