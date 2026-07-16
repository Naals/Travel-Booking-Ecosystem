package com.travel.identity.domain.model;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

public final class MfaConfiguration implements ValueObject {

    public enum MfaType { TOTP, SMS, EMAIL }

    private final boolean enabled;
    private final MfaType type;
    private final String  secret;

    private MfaConfiguration(boolean enabled, MfaType type, String secret) {
        this.enabled = enabled;
        this.type    = type;
        this.secret  = secret;
    }

    public static MfaConfiguration disabled()           { return new MfaConfiguration(false, null, null); }
    public static MfaConfiguration totp(String secret)  { return new MfaConfiguration(true, MfaType.TOTP, secret); }
    public static MfaConfiguration sms()                { return new MfaConfiguration(true, MfaType.SMS, null); }

    public boolean isEnabled() { return enabled; }
    public MfaType getType()   { return type; }
    public String  getSecret() { return secret; }

    @Override
    public boolean equals(Object o) {
        return o instanceof MfaConfiguration m
            && enabled == m.enabled && type == m.type;
    }

    @Override public int hashCode() { return Objects.hash(enabled, type); }
}
