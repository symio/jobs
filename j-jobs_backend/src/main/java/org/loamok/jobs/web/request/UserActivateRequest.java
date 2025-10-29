package org.loamok.jobs.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Requête de validation d'utilisateur")
@Data
public class UserActivateRequest {
    @Schema(description = "Clé de validation E-mail", example = "E0QXcvxuebajVfYezfYYbJEbbwAa2elB8eiqFBbI/Lk=", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String key;
}
