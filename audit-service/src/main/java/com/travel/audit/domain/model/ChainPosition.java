package com.travel.audit.domain.model;

import com.travel.audit.domain.valueobject.ChainHash;

/** The chain's current tail — returned by AuditChainRepository.lockHeadForAppend(). */
public record ChainPosition(long sequenceNumber, ChainHash hash) {}
