package org.loamok.jobs.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Requête de nouveau mot de passe pour l'utilisateur")
@Data
public class UserNewPasswordAfterLostRequest {
    @Schema(description = "Clé de validation E-mail", example = "E0QXcvxuebajVfYezfYYbJEbbwAa2elB8eiqFBbI/Lk=", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String key;
    @Schema(description = "Nouveau mot de passe", example = "@Bcd1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String newPassword;
    @Schema(description = "Confirmation du nouveau mot de passe", example = "@Bcd1234", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String passwordConfirm;
}
