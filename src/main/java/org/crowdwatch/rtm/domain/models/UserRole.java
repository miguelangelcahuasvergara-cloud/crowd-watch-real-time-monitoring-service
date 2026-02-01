package org.crowdwatch.rtm.domain.models;

import java.util.Arrays;

public enum UserRole {
    ADMIN("Admin"),
    TRANSPORT_MANAGER("Transport manager"),
    CENTER_CONTROL_OPERATOR("Center control operator"),
    BUS_DRIVER("Bus driver");
    private final String lowerCaseName;
    private UserRole(String lowerCaseName) {
        this.lowerCaseName = lowerCaseName;
    }
    public String getLowerCaseName() {
        return this.lowerCaseName;
    }
    public static boolean isValidUserRole(String role) {
        return Arrays.stream(values())
            .map(UserRole::getLowerCaseName)
            .anyMatch((s) -> s.equals(role));
    }
    public static UserRole createFrom(String role) {
        return Arrays.stream(values())
            .filter(userRole -> userRole.lowerCaseName.equals(role))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Role " + role + " is does not exists"
            ));
    }
    public boolean matchesExistingRole(String role) {
        return lowerCaseName == role;
    }
}
