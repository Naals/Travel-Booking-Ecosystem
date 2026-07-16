package com.travel.identity.domain.model;

import com.travel.identity.domain.event.UserDeactivatedEvent;
import com.travel.identity.domain.event.UserRegisteredEvent;
import com.travel.shared.domain.AggregateRoot;
import com.travel.common.exception.BusinessRuleViolationException;
import lombok.Getter;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * User Aggregate Root — identity bounded context.
 *
 * All state changes go through this class to enforce invariants.
 * No setters — mutation only via named domain methods.
 *
 * Domain events registered here are written to the outbox table
 * by the application layer after save() completes.
 */
@Getter
public class User extends AggregateRoot<UserId> {

    private static final int MAX_FAILED_ATTEMPTS  = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private Email             email;
    private HashedPassword    password;
    private FullName          fullName;
    private PhoneNumber       phoneNumber;
    private UserStatus        status;
    private Set<Role>         roles;
    private MfaConfiguration  mfaConfiguration;
    private int               failedLoginAttempts;
    private Instant           lockedUntil;
    private final Instant     createdAt;
    private Instant           updatedAt;

    private User(UserId id, Email email, HashedPassword password, FullName fullName) {
        super(id);
        this.email              = email;
        this.password           = password;
        this.fullName           = fullName;
        this.status             = UserStatus.PENDING_VERIFICATION;
        this.roles              = EnumSet.of(Role.TRAVELER);
        this.mfaConfiguration   = MfaConfiguration.disabled();
        this.failedLoginAttempts = 0;
        this.createdAt          = Instant.now();
        this.updatedAt          = Instant.now();
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    /**
     * Registers a new user. Raises UserRegisteredEvent to trigger
     * welcome email via notification-service (via Kafka).
     */
    public static User register(Email email, HashedPassword password, FullName fullName) {
        UserId id   = UserId.generate();
        User   user = new User(id, email, password, fullName);
        user.registerEvent(new UserRegisteredEvent(
            id.getValue(), email.getValue(), fullName.getFullName()));
        return user;
    }

    /**
     * Reconstitutes a User from persistence.
     * No events raised — this is a rehydration, not a state change.
     */
    public static User reconstitute(
        UserId id, Email email, HashedPassword password,
        FullName fullName, PhoneNumber phoneNumber,
        UserStatus status, Set<Role> roles,
        MfaConfiguration mfaConfiguration,
        int failedLoginAttempts, Instant lockedUntil,
        Instant createdAt, Instant updatedAt) {
        User user = new User(id, email, password, fullName);
        user.phoneNumber         = phoneNumber;
        user.status              = status;
        user.roles               = roles != null ? EnumSet.copyOf(roles) : EnumSet.of(Role.TRAVELER);
        user.mfaConfiguration    = mfaConfiguration;
        user.failedLoginAttempts = failedLoginAttempts;
        user.lockedUntil         = lockedUntil;
        return user;
    }

    // ── Domain methods ────────────────────────────────────────────────────────

    /**
     * Verifies email address. Transitions PENDING_VERIFICATION → ACTIVE.
     */
    public void verifyEmail() {
        if (status != UserStatus.PENDING_VERIFICATION)
            throw new BusinessRuleViolationException(
                "Email can only be verified when status is PENDING_VERIFICATION",
                "INVALID_STATUS_TRANSITION");
        this.status    = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Guards authentication. Throws descriptive exception if user cannot log in.
     * Called before password verification so locked/deactivated users fail fast.
     */
    public void assertCanAuthenticate() {
        switch (status) {
            case LOCKED -> {
                if (lockedUntil != null && Instant.now().isAfter(lockedUntil)) {
                    resetFailedLoginAttempts();
                } else {
                    throw new BusinessRuleViolationException(
                        "Account is locked due to too many failed login attempts",
                        "ACCOUNT_LOCKED");
                }
            }
            case DEACTIVATED -> throw new BusinessRuleViolationException(
                "Account has been deactivated", "ACCOUNT_DEACTIVATED");
            case PENDING_VERIFICATION -> throw new BusinessRuleViolationException(
                "Email address has not been verified", "EMAIL_NOT_VERIFIED");
            case ACTIVE -> { /* ok */ }
        }
    }

    /**
     * Records a failed login attempt.
     * Locks account when MAX_FAILED_ATTEMPTS is reached.
     */
    public void recordFailedLoginAttempt() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= MAX_FAILED_ATTEMPTS) {
            this.lockedUntil = Instant.now().plusSeconds(LOCK_DURATION_MINUTES * 60L);
            this.status      = UserStatus.LOCKED;
        }
        this.updatedAt = Instant.now();
    }

    /**
     * Clears failed login attempts on successful authentication.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.lockedUntil         = null;
        if (this.status == UserStatus.LOCKED)
            this.status = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    /**
     * Deactivates account. Raises UserDeactivatedEvent.
     */
    public void deactivate(String reason) {
        if (status == UserStatus.DEACTIVATED)
            throw new BusinessRuleViolationException(
                "Account is already deactivated", "ALREADY_DEACTIVATED");
        this.status    = UserStatus.DEACTIVATED;
        this.updatedAt = Instant.now();
        registerEvent(new UserDeactivatedEvent(
            getId().getValue(), email.getValue(), reason));
    }

    public void enableMfa(MfaConfiguration config) {
        this.mfaConfiguration = config;
        this.updatedAt        = Instant.now();
    }

    public void assignRole(Role role) {
        this.roles.add(role);
        this.updatedAt = Instant.now();
    }

    public void revokeRole(Role role) {
        if (role == Role.TRAVELER)
            throw new BusinessRuleViolationException(
                "Cannot revoke the base TRAVELER role", "INVALID_ROLE_OPERATION");
        this.roles.remove(role);
        this.updatedAt = Instant.now();
    }

    public void changePassword(HashedPassword newPassword) {
        this.password  = newPassword;
        this.updatedAt = Instant.now();
    }

    public void updatePhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.updatedAt   = Instant.now();
    }


    public Set<Role>        getRoles()               { return Collections.unmodifiableSet(roles); }

    public boolean isActive()     { return status == UserStatus.ACTIVE; }
    public boolean isMfaEnabled() { return mfaConfiguration.isEnabled(); }
}
