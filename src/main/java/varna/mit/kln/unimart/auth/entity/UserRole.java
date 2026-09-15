package varna.mit.kln.unimart.auth.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    buyer, seller, admin;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static UserRole fromString(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
