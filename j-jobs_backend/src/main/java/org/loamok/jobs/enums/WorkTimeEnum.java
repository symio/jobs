package org.loamok.jobs.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

/**
 *
 * ### TEMPS_TRAVAIL
 * - `PLEIN_TEMPS` : Temps plein
 * - `TEMPS_PARTIEL` : Temps partiel
 * 
 * @author Huby Franck 
 */
@ToString(of = {"name"})
public enum WorkTimeEnum implements LabelledEnum {
    PLEIN_TEMPS("Temps plein"),
    TEMPS_PARTIEL("Temps partiel");
    
    private final String label;
    
    WorkTimeEnum(String label) {
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
