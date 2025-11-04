package org.loamok.jobs.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.ToString;

/**
 *
 * ### MODALITE
 * - `DISTANCIEL` : Travail à distance
 * - `HYBRIDE` : Travail hybride (présentiel + distanciel)
 * - `SUR_SITE` : Travail sur site uniquement
 * 
 * @author Huby Franck 
 */
@ToString(of = {"name"})
public enum WorkModeEnum implements LabelledEnum {
    DISTANCIEL("Travail à distance"),
    HYBRIDE("Travail hybride (présentiel + distanciel)"),
    SUR_SITE("Travail sur site uniquement");
    
    private final String label;
    
    private WorkModeEnum(String label) {
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
