package org.loamok.jobs.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.loamok.jobs.enums.ContractEnum;
import org.loamok.jobs.enums.OfferStatusEnum;
import org.loamok.jobs.enums.WorkModeEnum;
import org.loamok.jobs.enums.WorkTimeEnum;

/**
 *
 * @author Huby Franck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Entity
@Table(name = "jobs", indexes = {
    @Index(columnList = "position")
})
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_job")
    private Integer id;
    @Column(name = "position", length = 200, nullable = false)
    private String position;
    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;
    @Column(name = "compagny", length = 150, nullable = false)
    private String compagny;
    @Column(name = "city", length = 200, nullable = false)
    private String city;
    
    // cas particulier Enums ...
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "contract", nullable = false, length = 50)
    private ContractEnum contract;
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "work_time", nullable = false, length = 50)
    private WorkTimeEnum workTime;
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 50)
    private WorkModeEnum workMode;
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "offer_status", nullable = false, length = 50)
    private OfferStatusEnum offerStatus;
    
    @Column(name = "from_officialdom", nullable = true)
    private boolean fromOfficialDom;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    // relations 
    /**
     * Relation avec JobHasStatus *
     */
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobHasStatus> jobHasStatuses = new ArrayList<>();
    /**
     * Relation avec User *
     */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private User user;

    // encore une couche sur les enums
    @JsonProperty("offerStatus") 
    public void setOfferStatus(String status) {
        if (status != null) {
            this.offerStatus = OfferStatusEnum.valueOf(status.trim().toUpperCase());
        }
    }
    @JsonProperty("workMode") 
    public void setWorkMode(String mode) {
        if (mode != null) {
            this.workMode = WorkModeEnum.valueOf(mode.trim().toUpperCase());
        }
    }
    @JsonProperty("workTime") 
    public void setWorkTime(String time) {
        if (time != null) {
            this.workTime = WorkTimeEnum.valueOf(time.trim().toUpperCase());
        }
    }
    @JsonProperty("contract") 
    public void setContract(String contract) {
        if (contract != null) {
            this.contract = ContractEnum.valueOf(contract.trim().toUpperCase());
        }
    }
}
