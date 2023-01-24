package com.techwhizer.snsbiosystem.model;

import java.util.Set;

public class CreateUsersResponse {

    private int totalUserDataGiven;
    private int totalUserAdded;
    private Set<UserDTO> failedUsers;
    private String message;

    public int getTotalUserDataGiven() {
        return totalUserDataGiven;
    }

    public void setTotalUserDataGiven(int totalUserDataGiven) {
        this.totalUserDataGiven = totalUserDataGiven;
    }

    public int getTotalUserAdded() {
        return totalUserAdded;
    }

    public void setTotalUserAdded(int totalUserAdded) {
        this.totalUserAdded = totalUserAdded;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Set<UserDTO> getFailedUsers() {
        return failedUsers;
    }

    public void setFailedUsers(Set<UserDTO> failedUsers) {
        this.failedUsers = failedUsers;
    }

}

