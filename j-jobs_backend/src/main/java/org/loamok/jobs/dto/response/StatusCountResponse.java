package org.loamok.jobs.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Réponse contenant les compteurs d'offres par status")
@Data
public class StatusCountResponse {
    @JsonProperty("EN_COURS")
    @Schema(name = "EN_COURS", description = "Compteur d'offres << en cours >>", example = "6")
    private long EN_COURS;
    @JsonProperty("EN_ATTENTE")
    @Schema(name = "EN_ATTENTE", description = "Compteur d'offres << en attente >>", example = "4")
    private long EN_ATTENTE;
    @JsonProperty("ENTRETIEN")
    @Schema(name = "ENTRETIEN", description = "Compteur d'offres << entretien programmé >>", example = "1")
    private long ENTRETIEN;
    @JsonProperty("REFUSE")
    @Schema(name = "REFUSE", description = "Compteur d'offres << refusées (candidature ou offre) >>", example = "2")
    private long REFUSE;
}
