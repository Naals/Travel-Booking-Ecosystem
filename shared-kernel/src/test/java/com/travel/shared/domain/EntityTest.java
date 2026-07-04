package com.travel.shared.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Entity")
class EntityTest {

    static class OrderId {
        private final String value;
        OrderId(String value) { this.value = value; }
        @Override public boolean equals(Object o) {
            return o instanceof OrderId id && value.equals(id.value);
        }
        @Override public int hashCode() { return value.hashCode(); }
    }

    static class TestEntity extends Entity<OrderId> {
        TestEntity(OrderId id) { super(id); }
    }

    @Nested
    @DisplayName("Identity equality")
    class IdentityEquality {

        @Test
        @DisplayName("same ID → equal regardless of reference")
        void equals_sameId() {
            var a = new TestEntity(new OrderId("x"));
            var b = new TestEntity(new OrderId("x"));
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different ID → not equal")
        void equals_differentId() {
            var a = new TestEntity(new OrderId("x"));
            var b = new TestEntity(new OrderId("y"));
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("same reference → equal")
        void equals_sameReference() {
            var a = new TestEntity(new OrderId("x"));
            assertThat(a).isEqualTo(a);
        }
    }

    @Nested
    @DisplayName("Null guard")
    class NullGuard {

        @Test
        @DisplayName("null ID → NullPointerException")
        void constructor_nullId() {
            assertThatThrownBy(() -> new TestEntity(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTest {

        @Test
        @DisplayName("includes class name and ID")
        void toString_format() {
            var e = new TestEntity(new OrderId("abc"));
            assertThat(e.toString()).contains("TestEntity").contains("abc");
        }
    }
}
