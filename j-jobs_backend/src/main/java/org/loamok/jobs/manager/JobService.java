package org.loamok.jobs.manager;

import java.util.Map;
import org.loamok.jobs.entity.Job;
import org.loamok.jobs.dto.request.JobSearchRequest;

/**
 *
 * @author Huby Franck
 */
public interface JobService {
    void registerJob(Job j);
    void updateJob(Job j);
    Boolean doCheckJobRegistering(Job j, StringBuilder failedValidation);
    Boolean checkNonNullFields(Job j, StringBuilder fieldName);
    Map<String, Object> searchJobsForCurrentUser(JobSearchRequest searchRequest, int page, int size);
}
