package org.loamok.jobs.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

/**
 * ### STATUT_OFFRE
 * - `EN_COURS` : Candidature en cours de traitement
 * - `EN_ATTENTE` : En attente de retour
 * - `REFUSE` : Candidature refusée
 * - `RELANCE` : Relance nécessaire
 * - `ENTRETIEN` : Entretien programmé ou passé
 *
 * @author Huby Franck
 */
@ToString(of = {"name"})
public enum OfferStatusEnum implements LabelledEnum {
    EN_COURS("Candidature en cours de traitement"),
    EN_ATTENTE("En attente de retour"),
    REFUSE("Candidature refusée"),
    RELANCE("Relance nécessaire"),
    ENTRETIEN("Entretien programmé ou passé");
    
    private final String label;
    
    private OfferStatusEnum(String label) {
        this.label = label;
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
