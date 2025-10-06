package org.loamok.jobs.event;

import org.loamok.jobs.entity.Job;
import org.loamok.jobs.entity.JobHasStatus;
import org.loamok.jobs.entity.User;
import org.loamok.jobs.enums.JobStatusEnum;
import org.loamok.jobs.manager.JobManager;
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
public class JobEventHandler {

    private final JobManager jobManager;

    public JobEventHandler(JobManager jobManager) {
        this.jobManager = jobManager;
    }
    
    @HandleBeforeCreate
    public void handleBeforeCreate(Job job) {
        jobManager.registerJob(job);
    }
    
    @HandleBeforeSave
    public void handleBeforeSave(Job job) {
        jobManager.updateJob(job);
    }
}
