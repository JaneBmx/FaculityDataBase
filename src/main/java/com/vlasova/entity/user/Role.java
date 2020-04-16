package com.vlasova.entity.user;

public enum Role {
    ADMIN(1),
    USER(3),
    GUEST(4);

    private int roleId;

    Role(int id) {
        this.roleId = id;
    }

    public int getId() {
        return roleId;
    }

    public static Role getRole(int id) {
        for (Role r : values()) {
            if (r.getId() == id) {
                return r;
            }
        }
        return GUEST;
    }
}