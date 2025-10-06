package org.loamok.jobs.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import lombok.Data;
import org.loamok.jobs.enums.ContractEnum;
import org.loamok.jobs.enums.OfferStatusEnum;
import org.loamok.jobs.enums.WorkModeEnum;
import org.loamok.jobs.enums.WorkTimeEnum;

/**
 *
 * @author Huby Franck
 */
@Schema(description = "Requête de recherche")
@Data
public class JobSearchRequest {
    @Schema(description = "Type de contrat", example = "CDI", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ContractEnum contract;
    @Schema(description = "Évènement depuis", example = "2025-10-05", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate eventAfter;
    @Schema(description = "Évènement jusqu'au", example = "2025-10-05", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate eventBefore;
    @Schema(description = "Issue d'une source officielle (France Travail)", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean officialdom;
    @Schema(description = "Status de l'offre", example = "A_EN_COURS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private OfferStatusEnum offerStatus;
    @Schema(description = "Ordre de tri", example = "A-Z", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String sort;
    @Schema(description = "Recherche libre", example = "...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String textual;
    @Schema(description = "Mode de travail", example = "HYBRIDE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private WorkModeEnum workMode;
    @Schema(description = "Temps de travail", example = "PLEIN_TEMPS", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private WorkTimeEnum workTime;
    
    
    public Instant getEventAfterAsInstant() {
        if (eventAfter == null) {
            return null;
        }
        return eventAfter.atStartOfDay().toInstant(ZoneOffset.UTC);
    }
    
    public Instant getEventBeforeAsInstant() {
        if (eventBefore == null) {
            return null;
        }
        return eventBefore.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);
    }
}
