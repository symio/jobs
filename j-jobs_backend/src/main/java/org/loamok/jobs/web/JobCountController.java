package org.loamok.jobs.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.loamok.jobs.dto.JobsDto;
import org.loamok.jobs.entity.enums.LogicalStatusEnum;
import org.loamok.jobs.entity.enums.OfferStatusEnum;
import org.loamok.jobs.repository.JobRepository;
import org.loamok.jobs.dto.response.StatusCountResponse;
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

    @GetMapping("/statuscount")
    @Operation(summary = "Compte le nombre d'offres par statut pour l'utilisateur courant (ou Admin)")
    public ResponseEntity<StatusCountResponse> countAll() {
        StatusCountResponse response = new StatusCountResponse();

        List<JobsDto.StatusCountProjection> dbCounts = jobRepository.countAllGroupedByStatusForCurrentUser();

        Map<OfferStatusEnum, Long> countMap = dbCounts.stream()
                .collect(Collectors.toMap(
                        JobsDto.StatusCountProjection::status,
                        JobsDto.StatusCountProjection::count
                ));

        for (LogicalStatusEnum logicalStatus : LogicalStatusEnum.values()) {
            long totalCount = 0;

            for (OfferStatusEnum offerStatus : logicalStatus.getOfferStatuses()) {
                totalCount += countMap.getOrDefault(offerStatus, 0L);
            }

            switch (logicalStatus) {
                case EN_COURS -> response.setEN_COURS(totalCount);
                case EN_ATTENTE -> response.setEN_ATTENTE(totalCount);
                case ENTRETIEN -> response.setENTRETIEN(totalCount);
                case REFUSE -> response.setREFUSE(totalCount);
            }
        }

        return ResponseEntity.ok(response);
    }

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
