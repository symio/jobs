package org.loamok.jobs.manager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.loamok.jobs.entity.Job;
import org.loamok.jobs.entity.JobHasStatus;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.enums.OfferStatusEnum;
import org.loamok.jobs.event.IdentifiedHandler;
import org.loamok.jobs.exceptions.MissingFieldsException;
import org.loamok.jobs.repository.JobRepository;
import org.loamok.jobs.repository.SecuritySpecifications;
import org.loamok.jobs.repository.UserRepository;
import org.loamok.jobs.web.request.JobSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 *
 * @author Huby Franck
 */
@Service
public class JobManager extends IdentifiedHandler implements JobService {

    private final JobRepository jobRepository;
    private User u;
    @PersistenceContext
    private EntityManager entityManager;

    public JobManager(JobRepository jobRepository, UserRepository userRepository) {
        super(userRepository);
        this.jobRepository = jobRepository;
    }

    private void loadCurrentUserAndIsAdmin(Job j) {
        u = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();

        if (u == null) {
            throw new SecurityException("Utilisateur introuvable");
        }

        if (!adminAccess) {
            j.setUser(u);
        } else {
            if (j.getUser() == null) {
                j.setUser(u);
            }
        }
    }

    private void addJobStatus(Job j) {
        j.getJobHasStatuses().add(
                JobHasStatus.builder()
                        .jobStatus(j.getOfferStatus().toJobStatus())
                        .offerStatus(j.getOfferStatus())
                        .job(j)
                        .build()
        );
    }

    private Map<String, Object> jobToMap(Job job) {
        Map<String, Object> jobMap = new HashMap<>();
        jobMap.put("position", job.getPosition());
        jobMap.put("description", job.getDescription());
        jobMap.put("compagny", job.getCompagny());
        jobMap.put("city", job.getCity());
        jobMap.put("from_official_dom", job.isFromOfficialDom());
        jobMap.put("created_at", job.getCreatedAt());
        jobMap.put("updated_at", job.getUpdatedAt());
        jobMap.put("contract", job.getContract().getName());
        jobMap.put("workTime", job.getWorkTime().getName());
        jobMap.put("workMode", job.getWorkMode().getName());
        jobMap.put("offerStatus", job.getOfferStatus().getName());

        if (job.getJobHasStatuses() != null && !job.getJobHasStatuses().isEmpty()) {
            List<Map<String, Object>> statuses = job.getJobHasStatuses().stream()
                    .map(jhs -> {
                        Map<String, Object> statusMap = new HashMap<>();
                        statusMap.put("id", jhs.getId());
                        statusMap.put("job_status", jhs.getJobStatus().getName());
                        statusMap.put("offer_status", jhs.getOfferStatus().getName());
                        statusMap.put("applied_at", jhs.getAppliedAt());

                        return statusMap;
                    })
                    .toList();
            jobMap.put("job_has_statuses", statuses);
        }

        Map<String, Map<String, String>> links = new HashMap<>();
        links.put("self", Map.of("href", "/jobs/" + job.getId()));
        jobMap.put("_links", links);

        return jobMap;
    }

    private OfferStatusEnum getOldOfferStatus(Integer jobId) {
        return (OfferStatusEnum) entityManager.createQuery(
                    "SELECT j.offerStatus FROM Job j WHERE j.id = :id"
                )
                .setParameter("id", jobId)
                .getSingleResult();
    }

    private Map<String, Object> buildHalResponse(Page<Job> jobPage, int page, int size) {
        Map<String, Object> response = new HashMap<>();

        // _embedded
        Map<String, Object> embedded = new HashMap<>();
        List<Map<String, Object>> jobsWithLinks = jobPage.getContent().stream()
                .map(this::jobToMap)
                .toList();
        embedded.put("jobs", jobsWithLinks);
        response.put("_embedded", embedded);

        Map<String, Map<String, String>> links = new HashMap<>();

        String selfHref = String.format("/jobs/search?page=%d&size=%d", page, size);
        links.put("self", Map.of("href", selfHref));

        links.put("first", Map.of("href", String.format("/jobs/search?page=0&size=%d", size)));

        int lastPage = jobPage.getTotalPages() - 1;
        links.put("last", Map.of("href", String.format("/jobs/search?page=%d&size=%d", lastPage, size)));

        if (page > 0) {
            links.put("prev", Map.of("href", String.format("/jobs/search?page=%d&size=%d", page - 1, size)));
        }

        if (page < lastPage) {
            links.put("next", Map.of("href", String.format("/jobs/search?page=%d&size=%d", page + 1, size)));
        }

        response.put("_links", links);

        // page info
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("size", jobPage.getSize());
        pageInfo.put("total_elements", jobPage.getTotalElements());
        pageInfo.put("total_pages", jobPage.getTotalPages());
        pageInfo.put("number", jobPage.getNumber());
        response.put("page", pageInfo);

        return response;
    }

    private Specification<Job> buildSpecification(JobSearchRequest request) {
        Specification<Job> spec = (root, query, cb) -> cb.conjunction();

        if (request.getContract() != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("contract"), request.getContract()));
        }

        if (request.getWorkMode() != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("workMode"), request.getWorkMode()));
        }

        if (request.getWorkTime() != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("workTime"), request.getWorkTime()));
        }

        if (request.getOfferStatus() != null) {
            spec = spec.and((root, query, cb)
                    -> cb.equal(root.get("offerStatus"), request.getOfferStatus()));
        }

        if (request.isOfficialdom()) {
            spec = spec.and((root, query, cb)
                    -> cb.isTrue(root.get("fromOfficialDom")));
        }

        if (request.getEventAfter() != null) {
            Instant eventAfter = request.getEventAfterAsInstant();
            spec = spec.and((root, query, cb)
                    -> cb.greaterThanOrEqualTo(root.get("createdAt"), eventAfter));
        }

        if (request.getEventBefore() != null) {
            Instant eventBefore = request.getEventBeforeAsInstant();
            spec = spec.and((root, query, cb)
                    -> cb.lessThanOrEqualTo(root.get("createdAt"), eventBefore));
        }

        if (request.getTextual() != null && !request.getTextual().isBlank()) {
            String searchPattern = "%" + request.getTextual().toLowerCase() + "%";
            spec = spec.and((root, query, cb)
                    -> cb.or(
                            cb.like(cb.lower(root.get("position")), searchPattern),
                            cb.like(cb.lower(root.get("compagny")), searchPattern),
                            cb.like(cb.lower(root.get("city")), searchPattern),
                            cb.like(cb.lower(root.get("description")), searchPattern)
                    )
            );
        }

        return spec;
    }

    private Sort buildSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "updatedAt", "createdAt");
        }

        return switch (sortParam.toUpperCase()) {
            case "A-Z" ->
                Sort.by(Sort.Direction.ASC, "position");
            case "Z-A" ->
                Sort.by(Sort.Direction.DESC, "position");
            case "DATE_ASC" ->
                Sort.by(Sort.Direction.ASC, "updatedAt", "createdAt");
            case "DATE_DESC" ->
                Sort.by(Sort.Direction.DESC, "updatedAt", "createdAt");
            default ->
                Sort.by(Sort.Direction.DESC, "updatedAt", "createdAt");
        };
    }

    @Override
    public Map<String, Object> searchJobsForCurrentUser(JobSearchRequest searchRequest, int page, int size) {
        User currentUser = getCurrentUser();
        boolean isAdminWithScope = isAdminWithScopeAdmin();

        Specification<Job> securitySpec = SecuritySpecifications
                .<Job>belongsToUserOrAdmin(currentUser, isAdminWithScope);
        Specification<Job> searchSpec = buildSpecification(searchRequest);
        Specification<Job> finalSpec = securitySpec.and(searchSpec);

        Sort sort = buildSort(searchRequest.getSort());
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Job> jobPage = jobRepository.findBySearch(finalSpec, pageable);

        return buildHalResponse(jobPage, page, size);
    }

    @Override
    public void registerJob(Job j) {
        loadCurrentUserAndIsAdmin(j);

        StringBuilder failedValidation = new StringBuilder();
        if (!doCheckJobRegistering(j, failedValidation)) {
            throw new MissingFieldsException(failedValidation.toString());
        }

        if (!j.isFromOfficialDom()) {
            switch (j.getOfferStatus().getName()) {
                case "O_ACCEPT":
                    j.setOfferStatus(OfferStatusEnum.A_EN_COURS);
                    break;
                case "O_REFUS":
                    j.setOfferStatus(OfferStatusEnum.C_REFUSE);
                    break;
            }
        }

        addJobStatus(j);
    }

    @Override
    public void updateJob(Job j) {
        loadCurrentUserAndIsAdmin(j);

        Job existingJob = jobRepository.findByIdFilteredForCurrentUser(j.getId())
                .orElseThrow(() -> new SecurityException("Job introuvable ou accès refusé"));


        StringBuilder failedValidation = new StringBuilder();
        if (!doCheckJobRegistering(j, failedValidation)) {
            throw new MissingFieldsException(failedValidation.toString());
        }

        if (getOldOfferStatus(j.getId()) != j.getOfferStatus()) 
            addJobStatus(j);
    }

    @Override
    public Boolean doCheckJobRegistering(Job j, StringBuilder failedValidation) {
        StringBuilder fieldName = new StringBuilder();
        Boolean areFieldsFilled = checkNonNullFields(j, fieldName);

        if (!areFieldsFilled) {
            failedValidation.setLength(0);
            failedValidation.append(fieldName);

            return false;
        }

        return true;
    }

    @Override
    public Boolean checkNonNullFields(Job j, StringBuilder fieldName) {
        if (j.getContract() == null) {
            fieldName.setLength(0);
            fieldName.append("contract");

            return false;
        }

        if (j.getOfferStatus() == null) {
            fieldName.setLength(0);
            fieldName.append("offerStatus");

            return false;
        }

        if (j.getWorkMode() == null) {
            fieldName.setLength(0);
            fieldName.append("workMode");

            return false;
        }

        if (j.getWorkTime() == null) {
            fieldName.setLength(0);
            fieldName.append("workTime");

            return false;
        }

        if (j.getCompagny() == null || j.getCompagny().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("compagny");

            return false;
        }

        if (j.getCity() == null || j.getCity().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("city");

            return false;
        }

        if (j.getPosition() == null || j.getPosition().isBlank()) {
            fieldName.setLength(0);
            fieldName.append("position");

            return false;
        }

        return true;
    }

}
