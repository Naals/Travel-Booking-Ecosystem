package com.travel.audit.domain;

import com.travel.audit.domain.valueobject.ChainHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ChainHash value object")
class ChainHashTest {

    @Test @DisplayName("GENESIS is 64 zero characters")
    void genesisIsAllZeros() {
        assertThat(ChainHash.GENESIS.getValue()).isEqualTo("0".repeat(64));
        assertThat(ChainHash.GENESIS.isGenesis()).isTrue();
    }

    @Test @DisplayName("rejects a string that isn't exactly 64 hex characters")
    void rejectsWrongLength() {
        assertThatThrownBy(() -> ChainHash.of("abc123"))
            .isInstanceOf(com.travel.common.exception.DomainException.class);
    }

    @Test @DisplayName("rejects uppercase hex")
    void rejectsUppercase() {
        assertThatThrownBy(() -> ChainHash.of("A".repeat(64)))
            .isInstanceOf(com.travel.common.exception.DomainException.class);
    }

    @Test @DisplayName("accepts a valid 64-character lowercase hex string")
    void acceptsValidHash() {
        String valid = "a".repeat(64);
        assertThat(ChainHash.of(valid).getValue()).isEqualTo(valid);
        assertThat(ChainHash.of(valid).isGenesis()).isFalse();
    }

    @Test @DisplayName("equal values produce equal ChainHash instances")
    void equality() {
        String hex = "b".repeat(64);
        assertThat(ChainHash.of(hex)).isEqualTo(ChainHash.of(hex));
    }
}
