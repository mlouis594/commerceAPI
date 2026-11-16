package com.mlouis594.CommerceAPI.user;

public enum Role {
    USER,
    ADMIN;

    /** Spring Security authority name, e.g. ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }
}
