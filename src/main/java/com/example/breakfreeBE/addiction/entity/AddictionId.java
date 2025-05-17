package com.example.breakfreeBE.addiction.entity;

import java.io.Serializable;
import java.util.Objects;

public class AddictionId implements Serializable {
    private String userId;
    private String addictionId;

    public AddictionId() {}

    public AddictionId(String userId, String addictionId) {
        this.userId = userId;
        this.addictionId = addictionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAddictionId() {
        return addictionId;
    }

    public void setAddictionId(String addictionId) {
        this.addictionId = addictionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddictionId that = (AddictionId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(addictionId, that.addictionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, addictionId);
    }
}