package com.refnet.Backend.user.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
    CANDIDATE,
    EMPLOYEE,
    HR_ADMIN,
    ADMIN;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) return null;
        String upperValue = value.toUpperCase();
        if ("SUPER_ADMIN".equals(upperValue) || "ADMIN".equals(upperValue)) {
            return ADMIN;
        }
        try {
            return Role.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + value + ". Allowed values are: CANDIDATE, EMPLOYEE, HR_ADMIN, ADMIN");
        }
    }

    @JsonValue
    public String toValue() {
        return name();
    }
}
