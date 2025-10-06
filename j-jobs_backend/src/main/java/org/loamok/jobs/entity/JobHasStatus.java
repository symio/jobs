package org.loamok.jobs.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.loamok.jobs.enums.JobStatusEnum;
import org.loamok.jobs.enums.OfferStatusEnum;

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
@Table(name = "JOBS_HAS_STATUS")
public class JobHasStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jhs")
    private Integer id;
    @Column(name = "job_status", nullable = false, length = 50)
    private JobStatusEnum jobStatus;
    @Column(name = "offer_status", nullable = false, length = 50)
    private OfferStatusEnum offerStatus;
    @CreationTimestamp
    private Instant appliedAt;
    // relations
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
}
