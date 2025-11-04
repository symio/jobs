package org.loamok.jobs.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

/**
 *
 * ### TYPE_CONTRAT
 * - `CDD` : Contrat à Durée Déterminée
 * - `CDI` : Contrat à Durée Indéterminée
 * - `FREELANCE` : Travail en freelance
 * - `INTERIM` : Contrat d'intérim
 * - `MISSION` : Mission ponctuelle
 * 
 * @author Huby Franck 
 */
@ToString(of = {"name"})
public enum ContractEnum implements LabelledEnum {
    CDD("Contrat à Durée Déterminée"),
    CDI("Contrat à Durée Indéterminée"),
    FREELANCE("Travail en freelance"),
    INTERIM("Contrat d'intérim"),
    MISSION("Mission ponctuelle");
    
    private final String label;
    
    private ContractEnum(String label) {
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
