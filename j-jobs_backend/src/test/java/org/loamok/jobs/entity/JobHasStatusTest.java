package org.loamok.jobs.entity;

import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.loamok.jobs.entity.enums.ContractEnum;
import org.loamok.jobs.entity.enums.JobStatusEnum;
import org.loamok.jobs.entity.enums.OfferStatusEnum;
import org.loamok.jobs.entity.enums.WorkModeEnum;
import org.loamok.jobs.entity.enums.WorkTimeEnum;
import org.loamok.jobs.repository.JobRepository;
import org.loamok.libs.o2springsecurity.entity.Role;
import org.loamok.libs.o2springsecurity.entity.User;
import org.loamok.libs.o2springsecurity.repository.RoleRepository;
import org.loamok.libs.o2springsecurity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 *
 * @author Huby Franck
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
@ActiveProfiles("test")
@Transactional
public class JobHasStatusTest {

    @Autowired
    JobRepository jobRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    Job myJob;
    User myUser;
    Role myRole;
    JobHasStatus myJhs;

    public JobHasStatusTest() {
    }

    private Job buildAJob() {
        return buildAJob(true, true);
    }

    private Job buildAJob(boolean withStatuses) {
        return buildAJob(withStatuses, true);
    }

    private Job buildAJob(boolean withStatuses, boolean withUser) {
        Job newJob = Job.builder()
                .contract(ContractEnum.INTERIM)
                .compagny("ACME")
                .city("New Mexico")
                .description("As a cartoon character, your main goal will be to avoid contact at all costs with ACME's local technical director, Wile E. Coyote, in the New Mexico desert.")
                .offerStatus(OfferStatusEnum.O_REFUS)
                .position("Geococcyx californianus")
                .workMode(WorkModeEnum.DISTANCIEL)
                .workTime(WorkTimeEnum.PLEIN_TEMPS)
                .build();

        if (withUser) {
            newJob.setUser(myUser);
        }

        if (withStatuses) {
            addJobStatus(newJob);
        }

        return newJob;
    }

    private void addJobStatus(Job j) {
        myJhs = JobHasStatus.builder()
                .jobStatus(j.getOfferStatus().toJobStatus())
                .offerStatus(j.getOfferStatus())
                .job(j)
                .build();

        j.getJobHasStatuses().add(myJhs);
    }

    @BeforeEach
    public void setUp() {
        myRole = Role.builder()
                .role("ROLE_USER")
                .isAdmin(Boolean.FALSE)
                .build();
        roleRepository.save(myRole);

        myUser = User.builder()
                .email("bip.bip@acme.com")
                .name("Runner")
                .firstname("road")
                .password("bip-bipMotherF!")
                .enabled(true)
                .gdproptin(true)
                .role(myRole)
                .build();
        userRepository.save(myUser);

        myJob = buildAJob();
    }

    @AfterEach
    public void tearDown() {
        myJob = null;
        myUser = null;
        myRole = null;

        jobRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    /**
     * Test of builder method, of class JobHasStatus.
     */
    @Test
    public void test_01_Builder() {
        System.out.println("builder");
        JobHasStatus expResult = myJhs;
        Job newJob = buildAJob(true, false);

        JobHasStatus result = newJob.getJobHasStatuses().getFirst();

        assertThat(expResult).isEqualTo(result);
    }

    /**
     * Test of getId method, of class JobHasStatus.
     */
    @Test
    public void test_02_GetId() {
        System.out.println("getId");
        Job newJob = buildAJob();
        jobRepository.save(newJob);

        Integer expResult = myJob.getJobHasStatuses().getFirst().getId();
        Integer result = newJob.getJobHasStatuses().getFirst().getId();

        assertThat(result).isPositive();
        assertThat(result).isInstanceOf(Integer.class);
        assertNotEquals(expResult, result);

        jobRepository.delete(newJob);
    }

    /**
     * Test of getJobStatus method, of class JobHasStatus.
     */
    @Test
    public void test_03_GetJobStatus() {
        System.out.println("getJobStatus");

        Job instance = buildAJob(true, false);
        JobStatusEnum expResult = myJhs.getJobStatus();
        JobStatusEnum result = instance.getJobHasStatuses().getFirst().getJobStatus();

        assertThat(expResult).isEqualTo(result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getOfferStatus method, of class JobHasStatus.
     */
    @Test
    public void test_04_GetOfferStatus() {
        System.out.println("getOfferStatus");
        Job instance = buildAJob(true, false);
        OfferStatusEnum expResult = myJhs.getOfferStatus();
        OfferStatusEnum result = instance.getJobHasStatuses().getFirst().getOfferStatus();

        assertThat(expResult).isEqualTo(result);
    }

    /**
     * Test of getAppliedAt method, of class JobHasStatus.
     */
    @Test
    public void test_05_GetAppliedAt() {
        System.out.println("getAppliedAt");
        Job instance = buildAJob();
        jobRepository.save(instance);

        Instant expResult = myJhs.getAppliedAt();
        Instant result = instance.getJobHasStatuses().getFirst().getAppliedAt();

        assertEquals(expResult, result);

        jobRepository.delete(instance);
    }

    /**
     * Test of getJob method, of class JobHasStatus.
     */
    @Test
    public void test_06_GetJob() {
        System.out.println("getJob");
        Job instance = buildAJob();
        instance.setId(myJob.getId());

        Job expResult = myJob.getJobHasStatuses().getFirst().getJob();
        Job result = instance.getJobHasStatuses().getFirst().getJob();

        assertThat(expResult).isEqualTo(result);
    }

    /**
     * Test of setId method, of class JobHasStatus.
     */
    @Test
    public void test_07_SetId() {
        System.out.println("setId");
        Job newJob = buildAJob();
        jobRepository.save(newJob);
        Integer id = newJob.getJobHasStatuses().getFirst().getId();

        JobHasStatus instance = new JobHasStatus();
        instance.setId(id);

        assertThat(instance.getId()).isPositive();
        assertThat(instance.getId()).isInstanceOf(Integer.class);
        assertEquals(newJob.getJobHasStatuses().getFirst().getId(), instance.getId());

        jobRepository.delete(newJob);
    }

    /**
     * Test of setJobStatus method, of class JobHasStatus.
     */
    @Test
    public void test_08_SetJobStatus() {
        System.out.println("setJobStatus");
        JobStatusEnum jobStatus = myJhs.getJobStatus();
        JobHasStatus instance = new JobHasStatus();
        instance.setJobStatus(jobStatus);

        assertThat(instance.getJobStatus()).isEqualTo(jobStatus);
    }

    /**
     * Test of setOfferStatus method, of class JobHasStatus.
     */
    @Test
    public void test_09_SetOfferStatus() {
        System.out.println("setOfferStatus");
        OfferStatusEnum offerStatus = myJhs.getOfferStatus();
        JobHasStatus instance = new JobHasStatus();
        instance.setOfferStatus(offerStatus);

        assertThat(instance.getOfferStatus()).isEqualTo(offerStatus);
    }

    /**
     * Test of setAppliedAt method, of class JobHasStatus.
     */
    @Test
    public void test_10_SetAppliedAt() {
        System.out.println("setAppliedAt");
        Job newJob = buildAJob();
        jobRepository.save(newJob);

        Instant appliedAt = newJob.getJobHasStatuses().getFirst().getAppliedAt();
        JobHasStatus instance = myJhs;
        instance.setAppliedAt(appliedAt);

        assertThat(instance.getAppliedAt()).isEqualTo(appliedAt);

        jobRepository.save(newJob);
    }

    /**
     * Test of setJob method, of class JobHasStatus.
     */
    @Test
    public void test_11_SetJob() {
        System.out.println("setJob");
        Job job = myJob;
        JobHasStatus instance = new JobHasStatus();
        instance.setJob(job);

        assertThat(instance.getJob()).isEqualTo(job);
    }

    /**
     * Test of toString method, of class JobHasStatus.
     */
    @Test
    public void test_12_ToString() {
        System.out.println("toString");
        JobHasStatus instance = JobHasStatus
                .builder()
                .jobStatus(JobStatusEnum.AUTRE)
                .offerStatus(OfferStatusEnum.O_REFUS)
                .appliedAt(null)
                .build();
        String expResult = myJhs.toString();

        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class JobHasStatus.
     */
    @Test
    public void test_13_Equals() {
        System.out.println("equals");
        Object o = myJhs;
        JobHasStatus instance = JobHasStatus
                .builder()
                .jobStatus(JobStatusEnum.AUTRE)
                .offerStatus(OfferStatusEnum.O_REFUS)
                .appliedAt(null)
                .build();

        boolean expResult = true;
        boolean result = instance.equals(o);

        assertEquals(expResult, result);
    }

    /**
     * Test of canEqual method, of class JobHasStatus.
     */
    @Test
    public void test_14_CanEqual() {
        System.out.println("canEqual");
        Object other = null;
        JobHasStatus instance = new JobHasStatus();
        boolean expResult = false;
        boolean result = instance.canEqual(other);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class JobHasStatus.
     */
    @Test
    public void test_15_HashCode() {
        System.out.println("hashCode");
        JobHasStatus instance = buildAJob().getJobHasStatuses().getFirst();
        int result = instance.hashCode();
        
        assertEquals(myJob.getJobHasStatuses().getFirst().hashCode(), result);
    }

}
