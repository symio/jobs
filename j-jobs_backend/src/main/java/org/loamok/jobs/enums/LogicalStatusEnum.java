package org.loamok.jobs.enums;

import java.util.List;

/**
 *
 * @author Huby Franck
 */
public enum LogicalStatusEnum {
    EN_COURS(List.of(OfferStatusEnum.A_EN_COURS, OfferStatusEnum.O_ACCEPT)),
    EN_ATTENTE(List.of(OfferStatusEnum.B_EN_ATTENTE, OfferStatusEnum.B_RELANCE_A, OfferStatusEnum.B_RELANCE_E)),
    ENTRETIEN(List.of(OfferStatusEnum.D_ENTRETIEN)),
    REFUSE(List.of(OfferStatusEnum.O_REFUS, OfferStatusEnum.C_REFUSE));

    private final List<OfferStatusEnum> offerStatuses;

    LogicalStatusEnum(List<OfferStatusEnum> offerStatuses) {
        this.offerStatuses = offerStatuses;
    }

    public List<OfferStatusEnum> getOfferStatuses() {
        return offerStatuses;
    }
}
