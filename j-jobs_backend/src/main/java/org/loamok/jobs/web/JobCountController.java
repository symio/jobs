package org.loamok.jobs.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.loamok.jobs.enums.LogicalStatusEnum;
import org.loamok.jobs.enums.OfferStatusEnum;
import org.loamok.jobs.repository.JobRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Huby Franck
 */
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Opérations sécurisées sur les offres d'emploi")
public class JobCountController {

    private final JobRepository jobRepository;
    
    @GetMapping("/countbystatus") 
    @Operation(summary = "Compte le nombre d'offres par statut pour l'utilisateur courant (ou Admin)")
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Statut de l'offre (ex: EN_COURS, REFUSE)")
            @RequestParam("status") LogicalStatusEnum logicalStatus) {
        
        long totalCount = 0;
        for (OfferStatusEnum offerStatus : logicalStatus.getOfferStatuses()) {
            totalCount += jobRepository.countFilteredForCurrentUserByOfferStatus(offerStatus);
        }
        
        return ResponseEntity.ok(totalCount);
    }
}
