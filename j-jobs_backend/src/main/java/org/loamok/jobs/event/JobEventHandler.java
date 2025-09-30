package org.loamok.jobs.event;

import org.loamok.jobs.entity.Job;
import org.loamok.jobs.entity.JobHasStatus;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.enums.JobStatusEnum;
import org.loamok.jobs.repository.JobRepository;
import org.loamok.jobs.repository.UserRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 *
 * @author Huby Franck
 */
@Component
@RepositoryEventHandler(Job.class)
public class JobEventHandler extends IdentifiedHandler {

    private final JobRepository jobRepository;

    public JobEventHandler(UserRepository userRepository, JobRepository jobRepository) {
        super(userRepository);
        this.jobRepository = jobRepository;
    }
    
    @HandleBeforeCreate
    public void handleBeforeCreate(Job job) {
        User currentUser = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();
        
        if (currentUser == null) {
            throw new SecurityException("Utilisateur introuvable");
        }

        if (!adminAccess) {
            job.setUser(currentUser);
        } else {
            if (job.getUser() == null) {
                job.setUser(currentUser);
            }
        }
        
        job.getJobHasStatuses().add(
            JobHasStatus.builder()
                .jobStatus(JobStatusEnum.AUTRE)
                .job(job)
                .build()
        );
    }
    
    @HandleBeforeSave
    public void handleBeforeSave(Job job) {
        User currentUser = getCurrentUser();
        boolean adminAccess = isAdminWithScopeAdmin();
        
        if (currentUser == null) {
            throw new SecurityException("Utilisateur introuvable");
        }

        Job existingJob = jobRepository.findByIdFilteredForCurrentUser(job.getId())
            .orElseThrow(() -> new SecurityException("Job introuvable ou accès refusé"));
        
        if (!adminAccess) {
            job.setUser(existingJob.getUser());
        } else {
            if (job.getUser() == null) {
                job.setUser(existingJob.getUser());
            }
        }
    }
}
