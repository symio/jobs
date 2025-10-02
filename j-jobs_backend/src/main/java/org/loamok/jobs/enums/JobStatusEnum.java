package org.loamok.jobs.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

/**
 *
 * ### TYPE_EVENEMENT 
 * - `CANDIDATURE_ENVOYEE` : Candidature envoyée 
 * - `RELANCE` : Relance effectuée 
 * - `REFUS` : Refus reçu 
 * - `ENTRETIEN` : Entretien programmé/passé 
 * - `AUTRE` : Autre type d'événement
 * 
 * @author Huby Franck 
 */
@ToString(of = {"name"})
public enum JobStatusEnum implements LabelledEnum {
    CANDIDATURE_ENVOYEE("Candidature envoyée"),
    RELANCE("Relance effectuée"),
    REFUS("Refus reçu"),
    ENTRETIEN("Entretien programmé/passé"),
    AUTRE("Autre type d'événement");
    
    private final String label;
    
    private JobStatusEnum(String label) {
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
