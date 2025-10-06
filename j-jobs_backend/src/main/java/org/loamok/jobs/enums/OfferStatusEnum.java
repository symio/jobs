package org.loamok.jobs.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

/**
 * ### STATUT_OFFRE
 * #### Status d'un offre
 * - `EN_COURS` : Candidature en cours de traitement
 * - `EN_ATTENTE` : En attente de retour
 * - `REFUSE` : Candidature refusée
 * - `REFUS` : Offre refusée (offre institutionelle qui ne correspond pas aux critères)
 * - `ACCEPT` : Offre acceptée (offre institutionelle correspond aux critères)
 * - `RELANCE` : Relance nécessaire
 * - `ENTRETIEN` : Entretien programmé ou passé
 *
 * @author Huby Franck
 */
@ToString(of = {"name"})
public enum OfferStatusEnum implements LabelledEnum {
    A_EN_COURS("Candidature en cours de traitement", JobStatusEnum.CANDIDATURE_ENVOYEE),
    B_EN_ATTENTE("En attente de retour", JobStatusEnum.CANDIDATURE_ENVOYEE),
    B_RELANCE("Relance nécessaire", JobStatusEnum.RELANCE),
    C_REFUSE("Candidature refusée", JobStatusEnum.REFUS),
    D_ENTRETIEN("Entretien programmé ou passé", JobStatusEnum.ENTRETIEN),
    O_ACCEPT("Offre acceptée", JobStatusEnum.AUTRE),
    O_REFUS("Offre refusée", JobStatusEnum.AUTRE);
    
    private final String label;
    private final JobStatusEnum correspondingJobStatus;
    
    private OfferStatusEnum(String label, JobStatusEnum jobStatus) {
        this.label = label;
        this.correspondingJobStatus = jobStatus;
    }
    
    public JobStatusEnum toJobStatus() {
        return correspondingJobStatus;
    }
    
    @Override
    public String getLabel() {
        return label;
    }
    
    @JsonValue
    @Override
    public String getName() {
        return this.name();
    }
    
}
