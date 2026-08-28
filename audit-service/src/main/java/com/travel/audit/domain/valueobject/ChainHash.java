package com.travel.audit.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import com.travel.common.exception.DomainException;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A SHA-256 digest, always exactly 64 lowercase hex characters.
 * GENESIS is the fixed "no prior entry" sentinel — the same
 * zero-hash convention real blockchain and immutable-ledger systems
 * use for their first block. See HashChainService for how a hash is
 * actually computed, and ADR-015 for why this exists at all.
 */
public final class ChainHash implements ValueObject {

    private static final Pattern HEX_64 = Pattern.compile("^[0-9a-f]{64}$");

    public static final ChainHash GENESIS = new ChainHash("0".repeat(64));

    private final String value;

    private ChainHash(String value) {
        if (value == null || !HEX_64.matcher(value).matches())
            throw new DomainException(
                "ChainHash must be a 64-character lowercase hex string", "INVALID_CHAIN_HASH");
        this.value = value;
    }

    public static ChainHash of(String value) { return new ChainHash(value); }

    public String  getValue()   { return value; }
    public boolean isGenesis()  { return this.equals(GENESIS); }

    @Override public boolean equals(Object o) {
        return o instanceof ChainHash c && value.equals(c.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
