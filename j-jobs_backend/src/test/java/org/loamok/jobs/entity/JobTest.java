package org.loamok.jobs.entity;

import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.loamok.jobs.entity.enums.ContractEnum;
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
public class JobTest {

    @Autowired
    JobRepository jobRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;

    Job myJob;
    User myUser;
    Role myRole;

    public JobTest() {
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
        j.getJobHasStatuses().add(
                JobHasStatus.builder()
                        .jobStatus(j.getOfferStatus().toJobStatus())
                        .offerStatus(j.getOfferStatus())
                        .job(j)
                        .build()
        );
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
     * Test of setOfferStatus method, of class Job.
     */
    @Test
    public void test_01_setOfferStatus_OfferStatusEnum() {
        System.out.println("setOfferStatus Enum");
        OfferStatusEnum status = myJob.getOfferStatus();
        Job instance = buildAJob();
        instance.setOfferStatus(status);

        assertEquals(status, instance.getOfferStatus());
    }

    /**
     * Test of setOfferStatus method, of class Job.
     */
    @Test
    public void test_02_setOfferStatus_String() {
        System.out.println("setOfferStatus String");
        String status = "O_REFUS";
        Job instance = buildAJob();
        instance.setOfferStatus(status);

        assertEquals(myJob.getOfferStatus(), instance.getOfferStatus());
    }

    /**
     * Test of setWorkMode method, of class Job.
     */
    @Test
    public void test_03_1_setWorkMode() {
        System.out.println("setWorkMode Enum");
        WorkModeEnum mode = myJob.getWorkMode();
        Job instance = buildAJob();
        instance.setWorkMode(mode);

        assertEquals(myJob.getWorkMode(), instance.getWorkMode());
    }

    /**
     * Test of setWorkMode method, of class Job.
     */
    @Test
    public void test_03_2_setWorkMode_string() {
        System.out.println("setWorkMode String");
        String mode = "DISTANCIEL";
        Job instance = buildAJob();
        instance.setWorkMode(mode);

        assertEquals(myJob.getWorkMode(), instance.getWorkMode());
    }

    /**
     * Test of setWorkTime method, of class Job.
     */
    @Test
    public void test_04_1_setWorkTime() {
        System.out.println("setWorkTimeEnum");
        WorkTimeEnum time = myJob.getWorkTime();
        Job instance = buildAJob();
        instance.setWorkTime(time);

        assertEquals(myJob.getWorkTime(), instance.getWorkTime());
    }

    /**
     * Test of setWorkTime method, of class Job.
     */
    @Test
    public void test_04_2_setWorkTime() {
        System.out.println("setWorkTime");
        String time = "PLEIN_TEMPS";
        Job instance = buildAJob();
        instance.setWorkTime(time);

        assertEquals(myJob.getWorkTime(), instance.getWorkTime());
    }

    /**
     * Test of setContract method, of class Job.
     */
    @Test
    public void test_05_1_setContract() {
        System.out.println("setContract Enum");
        ContractEnum contract = myJob.getContract();
        Job instance = buildAJob();
        instance.setContract(contract);

        assertEquals(myJob.getContract(), instance.getContract());
    }

    /**
     * Test of setContract method, of class Job.
     */
    @Test
    public void test_05_2_setContract() {
        System.out.println("setContract String");
        String contract = "INTERIM";
        Job instance = buildAJob();
        instance.setContract(contract);

        assertEquals(myJob.getContract(), instance.getContract());
    }

    /**
     * Test of builder method, of class Job.
     */
    @Test
    public void test_06_builder() {
        System.out.println("builder");
        Job expResult = buildAJob();

        Assertions.assertThat(expResult).isEqualTo(myJob);
    }

    /**
     * Test of getId method, of class Job.
     */
    @Test
    public void test_07_getId() {
        System.out.println("getId");
        Job instance = buildAJob();
        Integer expResult = myJob.getId();
        Integer result = instance.getId();

        assertEquals(expResult, result);

        jobRepository.save(myJob);
        jobRepository.save(instance);
        Integer expResult2 = myJob.getId();
        Integer result2 = instance.getId();

        assertNotEquals(expResult2, result2);
    }

    /**
     * Test of getPosition method, of class Job.
     */
    @Test
    public void test_08_getPosition() {
        System.out.println("getPosition");
        Job instance = buildAJob();
        String expResult = myJob.getPosition();
        String result = instance.getPosition();

        assertEquals(expResult, result);
    }

    /**
     * Test of getDescription method, of class Job.
     */
    @Test
    public void test_09_getDescription() {
        System.out.println("getDescription");
        Job instance = buildAJob();
        String expResult = myJob.getDescription();
        String result = instance.getDescription();

        assertEquals(expResult, result);
    }

    /**
     * Test of getCompagny method, of class Job.
     */
    @Test
    public void test_10_getCompagny() {
        System.out.println("getCompagny");
        Job instance = buildAJob();
        String expResult = myJob.getCompagny();
        String result = instance.getCompagny();

        assertEquals(expResult, result);
    }

    /**
     * Test of getCity method, of class Job.
     */
    @Test
    public void test_11_getCity() {
        System.out.println("getCity");
        Job instance = buildAJob();
        String expResult = myJob.getCity();
        String result = instance.getCity();

        assertEquals(expResult, result);
    }

    /**
     * Test of getContract method, of class Job.
     */
    @Test
    public void test_12_getContract() {
        System.out.println("getContract Enum");
        Job instance = buildAJob();
        ContractEnum expResult = myJob.getContract();
        ContractEnum result = instance.getContract();

        assertEquals(expResult, result);
    }

    /**
     * Test of getWorkTime method, of class Job.
     */
    @Test
    public void test_13_getWorkTime() {
        System.out.println("getWorkTime");
        Job instance = buildAJob();
        WorkTimeEnum expResult = myJob.getWorkTime();
        WorkTimeEnum result = instance.getWorkTime();

        assertEquals(expResult, result);
    }

    /**
     * Test of getWorkMode method, of class Job.
     */
    @Test
    public void test_14_getWorkMode() {
        System.out.println("getWorkMode");
        Job instance = buildAJob();
        WorkModeEnum expResult = myJob.getWorkMode();
        WorkModeEnum result = instance.getWorkMode();

        assertEquals(expResult, result);
    }

    /**
     * Test of getOfferStatus method, of class Job.
     */
    @Test
    public void test_15_getOfferStatus() {
        System.out.println("getOfferStatus");
        Job instance = buildAJob();
        OfferStatusEnum expResult = myJob.getOfferStatus();
        OfferStatusEnum result = instance.getOfferStatus();

        assertEquals(expResult, result);
    }

    /**
     * Test of isFromOfficialDom method, of class Job.
     */
    @Test
    public void test_16_isFromOfficialDom() {
        System.out.println("isFromOfficialDom");
        Job instance = buildAJob();
        boolean expResult = myJob.isFromOfficialDom();
        boolean result = instance.isFromOfficialDom();

        assertEquals(expResult, result);
    }

    /**
     * Test of getCreatedAt method, of class Job.
     */
    @Test
    public void test_17_getCreatedAt() {
        System.out.println("getCreatedAt");
        Job instance = buildAJob();
        Instant expResult = myJob.getCreatedAt();
        Instant result = instance.getCreatedAt();

        assertEquals(expResult, result);
    }

    /**
     * Test of getUpdatedAt method, of class Job.
     */
    @Test
    public void test_18_getUpdatedAt() {
        System.out.println("getUpdatedAt");
        Job instance = buildAJob();
        Instant expResult = myJob.getUpdatedAt();
        Instant result = instance.getUpdatedAt();

        assertEquals(expResult, result);
    }

    /**
     * Test of getJobHasStatuses method, of class Job.
     */
    @Test
    public void test_19_getJobHasStatuses() {
        System.out.println("getJobHasStatuses");
        Job instance = buildAJob();

        List<JobHasStatus> expResult = myJob.getJobHasStatuses();
        List<JobHasStatus> result = instance.getJobHasStatuses();

        assertEquals(expResult, result);
    }

    /**
     * Test of getUser method, of class Job.
     */
    @Test
    public void test_20_getUser() {
        System.out.println("getUser");
        Job instance = buildAJob();
        User expResult = myJob.getUser();
        User result = instance.getUser();

        assertEquals(expResult, result);
    }

    /**
     * Test of setId method, of class Job.
     */
    @Test
    public void test_21_setId() {
        System.out.println("setId");
        Integer id = myJob.getId();
        Job instance = buildAJob();
        instance.setId(id);

        assertEquals(myJob.getId(), instance.getId());
    }

    /**
     * Test of setPosition method, of class Job.
     */
    @Test
    public void test_22_setPosition() {
        System.out.println("setPosition");
        String position = myJob.getPosition();
        Job instance = buildAJob();
        instance.setPosition(position);

        assertEquals(myJob.getPosition(), instance.getPosition());
    }

    /**
     * Test of setDescription method, of class Job.
     */
    @Test
    public void test_23_setDescription() {
        System.out.println("setDescription");
        String description = myJob.getDescription();
        Job instance = buildAJob();
        instance.setDescription(description);

        assertEquals(myJob.getDescription(), instance.getDescription());
    }

    /**
     * Test of setCompagny method, of class Job.
     */
    @Test
    public void test_24_setCompagny() {
        System.out.println("setCompagny");
        String compagny = myJob.getCompagny();
        Job instance = buildAJob();
        instance.setCompagny(compagny);

        assertEquals(myJob.getCompagny(), instance.getCompagny());
    }

    /**
     * Test of setCity method, of class Job.
     */
    @Test
    public void test_25_setCity() {
        System.out.println("setCity");
        String city = myJob.getCity();
        Job instance = buildAJob();
        instance.setCity(city);

        assertEquals(myJob.getCity(), instance.getCity());
    }

    /**
     * Test of setFromOfficialDom method, of class Job.
     */
    @Test
    public void test_26_setFromOfficialDom() {
        System.out.println("setFromOfficialDom");
        boolean fromOfficialDom = myJob.isFromOfficialDom();
        Job instance = buildAJob();
        instance.setFromOfficialDom(fromOfficialDom);

        assertEquals(myJob.isFromOfficialDom(), instance.isFromOfficialDom());
    }

    /**
     * Test of setCreatedAt method, of class Job.
     */
    @Test
    public void test_27_setCreatedAt() {
        System.out.println("setCreatedAt");
        Instant createdAt = myJob.getCreatedAt();
        Job instance = buildAJob();
        instance.setCreatedAt(createdAt);

        assertEquals(myJob.getCreatedAt(), instance.getCreatedAt());
    }

    /**
     * Test of setUpdatedAt method, of class Job.
     */
    @Test
    public void test_28_setUpdatedAt() {
        System.out.println("setUpdatedAt");
        Instant updatedAt = myJob.getUpdatedAt();
        Job instance = buildAJob();
        instance.setUpdatedAt(updatedAt);

        assertEquals(myJob.getUpdatedAt(), instance.getUpdatedAt());
    }

    /**
     * Test of setJobHasStatuses method, of class Job.
     */
    @Test
    public void test_29_setJobHasStatuses() {
        System.out.println("setJobHasStatuses");
        List<JobHasStatus> jobHasStatuses = myJob.getJobHasStatuses();
        Job instance = buildAJob(false);
        instance.setJobHasStatuses(jobHasStatuses);

        assertEquals(myJob.getJobHasStatuses(), instance.getJobHasStatuses());
    }

    /**
     * Test of setUser method, of class Job.
     */
    @Test
    public void test_30_setUser() {
        System.out.println("setUser");
        User user = myJob.getUser();
        Job instance = buildAJob(true, false);
        instance.setUser(user);

        assertEquals(myJob.getUser(), instance.getUser());
    }

    /**
     * Test of toString method, of class Job.
     */
    @Test
    public void test_31_toString() {
        System.out.println("toString");
        Job instance = buildAJob(false, false);
        String expResult = myJob.toString();
        instance.setId(myJob.getId());
        String result = instance.toString();

        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class Job.
     */
    @Test
    public void test_32_equals() {
        System.out.println("equals");
        Object o = null;
        Job instance = buildAJob();
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of canEqual method, of class Job.
     */
    @Test
    public void test_33_canEqual() {
        System.out.println("canEqual");
        Object other = null;
        Job instance = buildAJob();
        boolean expResult = false;
        boolean result = instance.canEqual(other);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class Job.
     */
    @Test
    public void test_34_hashCode() {
        System.out.println("hashCode");
        Job instance = buildAJob();
        int expResult = myJob.hashCode();
        int result = instance.hashCode();

        Assertions.assertThat(result).isEqualTo(expResult);
    }

}
