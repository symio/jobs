package org.loamok.jobs.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.loamok.jobs.entity.enums.ContractEnum;
import org.loamok.jobs.entity.enums.OfferStatusEnum;
import org.loamok.jobs.entity.enums.WorkModeEnum;
import org.loamok.jobs.entity.enums.WorkTimeEnum;
import org.loamok.jobs.entity.enums.LabelledEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import org.loamok.jobs.entity.enums.JobStatusEnum;

/**
 *
 * @author Huby Franck
 */
@RestController
@RequestMapping("/labels")
public class LabelsController {

    private Map<String, String> getEnumLabels(Class<? extends LabelledEnum> enumClass) {
        Map<String, String> labelsMap = new LinkedHashMap<>();

        Arrays.stream(enumClass.getEnumConstants())
                .forEach(e -> labelsMap.put(e.getName(), e.getLabel()));

        return labelsMap;
    }

    @Operation(summary = "Récupère les labels de tous les Enums métier",
            description = "Retourne un Map global contenant, pour chaque Enum pertinent, le mapping entre la constante (clé) et le label descriptif (valeur).") // 👈 Ajout
    @ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Liste des labels d'Enums récupérée avec succès.",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(example = "{\n  \"WorkTimeEnum\": {\n    \"PLEIN_TEMPS\" : \"Temps plein\",\n    \"TEMPS_PARTIEL\": \"Temps partiel\"\n  },\n  \"ContractEnum\": { \n    \"CDI\": \"Contrat à Durée Indéterminée\"\n  }\n}") // 👈 Ajout d'un exemple explicite
                )
        )
    })
    @GetMapping
    public ResponseEntity<Map<String, Map<String, String>>> getAllEnumLabels() {
        Map<String, Map<String, String>> response = new HashMap<>();

        List<Class<? extends LabelledEnum>> enumClasses = List.of(
                ContractEnum.class,
                WorkModeEnum.class,
                WorkTimeEnum.class,
                OfferStatusEnum.class,
                JobStatusEnum.class
        );

        enumClasses.forEach(enumClass
                -> response.put(enumClass.getSimpleName(), getEnumLabels(enumClass))
        );

        return ResponseEntity.ok(response);
    }
}
