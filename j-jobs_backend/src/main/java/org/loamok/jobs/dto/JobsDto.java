package org.loamok.jobs.dto;

import org.loamok.jobs.entity.enums.OfferStatusEnum;

/**
 *
 * @author Huby Franck
 */
public class JobsDto {
    public record StatusCountProjection(OfferStatusEnum status, long count) {}
}
