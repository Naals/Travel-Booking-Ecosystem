package com.travel.fraud.domain.event;

import com.travel.shared.event.DomainEvent;

/** Raised by staff-initiated ClearRiskFlagUseCase. No current consumer. */
public class RiskFlagClearedEvent extends DomainEvent {

    private final String userId;
    private final String clearedByStaffId;

    public RiskFlagClearedEvent(String userId, String clearedByStaffId) {
        super("RiskFlagCleared");
        this.userId           = userId;
        this.clearedByStaffId = clearedByStaffId;
    }

    @Override public String getAggregateId()  { return userId; }
    public String getUserId()           { return userId; }
    public String getClearedByStaffId() { return clearedByStaffId; }
}
