package com.travel.identity.infrastructure.persistence.mapper;

import com.travel.identity.domain.model.*;
import com.travel.identity.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User user) {
        return UserJpaEntity.builder()
            .id(user.getId().getValue())
            .email(user.getEmail().getValue())
            .passwordHash(user.getPassword().getHash())
            .firstName(user.getFullName().getFirstName())
            .lastName(user.getFullName().getLastName())
            .phoneNumber(user.getPhoneNumber() != null
                ? user.getPhoneNumber().getValue() : null)
            .status(user.getStatus())
            .roles(user.getRoles())
            .mfaEnabled(user.getMfaConfiguration().isEnabled())
            .mfaType(user.getMfaConfiguration().getType())
            .mfaSecret(user.getMfaConfiguration().getSecret())
            .failedLoginAttempts(user.getFailedLoginAttempts())
            .lockedUntil(user.getLockedUntil())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }

    public User toDomain(UserJpaEntity e) {
        MfaConfiguration mfa = buildMfa(e);
        return User.reconstitute(
            UserId.of(e.getId()),
            Email.of(e.getEmail()),
            HashedPassword.ofHash(e.getPasswordHash()),
            FullName.of(e.getFirstName(), e.getLastName()),
            e.getPhoneNumber() != null ? PhoneNumber.of(e.getPhoneNumber()) : null,
            e.getStatus(),
            e.getRoles(),
            mfa,
            e.getFailedLoginAttempts(),
            e.getLockedUntil(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    private MfaConfiguration buildMfa(UserJpaEntity e) {
        if (!e.isMfaEnabled()) return MfaConfiguration.disabled();
        return switch (e.getMfaType()) {
            case TOTP  -> MfaConfiguration.totp(e.getMfaSecret());
            case SMS   -> MfaConfiguration.sms();
            default    -> MfaConfiguration.disabled();
        };
    }
}
