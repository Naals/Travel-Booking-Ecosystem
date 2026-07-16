package com.travel.identity.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.identity.domain.event.UserDeactivatedEvent;
import com.travel.identity.domain.event.UserRegisteredEvent;
import com.travel.identity.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User aggregate")
class UserTest {

    static final Email         EMAIL    = Email.of("john@example.com");
    static final HashedPassword PASSWORD = HashedPassword.ofHash("$2a$12$hash");
    static final FullName      NAME     = FullName.of("John", "Doe");

    @Nested
    @DisplayName("Registration")
    class Registration {

        @Test
        @DisplayName("new user starts as PENDING_VERIFICATION")
        void newUser_pendingVerification() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        }

        @Test
        @DisplayName("new user has TRAVELER role")
        void newUser_travelerRole() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            assertThat(user.getRoles()).containsExactly(Role.TRAVELER);
        }

        @Test
        @DisplayName("registration raises UserRegisteredEvent")
        void register_raisesEvent() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserRegisteredEvent.class);
            UserRegisteredEvent e = (UserRegisteredEvent) user.getDomainEvents().get(0);
            assertThat(e.getEmail()).isEqualTo("john@example.com");
            assertThat(e.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("each registration gets a unique ID")
        void register_uniqueIds() {
            User a = User.register(EMAIL, PASSWORD, NAME);
            User b = User.register(Email.of("other@example.com"), PASSWORD, NAME);
            assertThat(a.getId()).isNotEqualTo(b.getId());
        }
    }

    @Nested
    @DisplayName("Email verification")
    class EmailVerification {

        @Test
        @DisplayName("verifyEmail transitions to ACTIVE")
        void verifyEmail_activates() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("cannot verify already active user")
        void verifyEmail_alreadyActive_throws() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            assertThatThrownBy(user::verifyEmail)
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Authentication guard")
    class AuthGuard {

        @Test
        @DisplayName("PENDING user cannot authenticate")
        void pending_cannotAuth() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            assertThatThrownBy(user::assertCanAuthenticate)
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not been verified");
        }

        @Test
        @DisplayName("ACTIVE user can authenticate")
        void active_canAuth() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            assertThatCode(user::assertCanAuthenticate).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("account locks after 5 failed attempts")
        void failedAttempts_locksAccount() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            for (int i = 0; i < 5; i++) user.recordFailedLoginAttempt();
            assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
            assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        }

        @Test
        @DisplayName("reset clears failed attempts and LOCKED status")
        void reset_clearsLock() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            user.recordFailedLoginAttempt();
            user.resetFailedLoginAttempts();
            assertThat(user.getFailedLoginAttempts()).isZero();
            assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Deactivation")
    class Deactivation {

        @Test
        @DisplayName("deactivate transitions to DEACTIVATED and raises event")
        void deactivate_raisesEvent() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            user.clearDomainEvents();

            user.deactivate("User requested deletion");

            assertThat(user.getStatus()).isEqualTo(UserStatus.DEACTIVATED);
            assertThat(user.getDomainEvents()).hasSize(1);
            assertThat(user.getDomainEvents().get(0)).isInstanceOf(UserDeactivatedEvent.class);
        }

        @Test
        @DisplayName("cannot deactivate twice")
        void deactivate_twice_throws() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            user.deactivate("first");
            assertThatThrownBy(() -> user.deactivate("second"))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("RBAC")
    class Rbac {

        @Test
        @DisplayName("can assign HOST role")
        void assignRole_host() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            user.assignRole(Role.HOST);
            assertThat(user.getRoles()).contains(Role.TRAVELER, Role.HOST);
        }

        @Test
        @DisplayName("cannot revoke TRAVELER role")
        void revokeRole_traveler_throws() {
            User user = User.register(EMAIL, PASSWORD, NAME);
            user.verifyEmail();
            assertThatThrownBy(() -> user.revokeRole(Role.TRAVELER))
                .isInstanceOf(BusinessRuleViolationException.class);
        }
    }

    @Nested
    @DisplayName("Value objects")
    class ValueObjects {

        @Test @DisplayName("Email rejects invalid format")
        void email_invalid() {
            assertThatThrownBy(() -> Email.of("not-an-email"))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }

        @Test @DisplayName("Email normalizes to lowercase")
        void email_lowercase() {
            assertThat(Email.of("JOHN@EXAMPLE.COM").getValue())
                .isEqualTo("john@example.com");
        }

        @Test @DisplayName("FullName rejects blank first name")
        void fullName_blankFirst() {
            assertThatThrownBy(() -> FullName.of("", "Doe"))
                .isInstanceOf(com.travel.common.exception.DomainException.class);
        }
    }
}
