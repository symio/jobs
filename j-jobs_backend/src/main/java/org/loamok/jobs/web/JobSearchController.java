package org.loamok.jobs.web;

import org.loamok.jobs.dto.request.JobSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.loamok.jobs.manager.JobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Opérations sécurisées sur les offres d'emploi")
public class JobSearchController {
    private final JobService jobService;
    
    @PostMapping("/search") 
    @Operation(summary = "Recherche multicritères d'offres pour l'utilisateur courant (ou Admin)")
    public ResponseEntity<?> search(
        @Parameter(description = "Objet contenant la recherche", required = true)
        @Valid @RequestBody JobSearchRequest searchRequest,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "3") int size
    ) {
        try {
            Map<String, Object> response = jobService.searchJobsForCurrentUser(searchRequest, page, size);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "invalid_request",
                "error_description", "Requête de recherche incorrecte: " + e.getMessage()
            ));
        }
    }
}