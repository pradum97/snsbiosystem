package com.techwhizer.snsbiosystem.model;

import java.util.Set;
public class AuthResponse {
    private Long id;
    private String firstName, lastName, userName;
    private String status;
    private Set<String> roles;
    private String error;

    public AuthResponse() {

    }

    public AuthResponse(String userName, String error) {
        this.userName = userName;
        this.error = error;
    }

    public AuthResponse(Long id, String firstName, String lastName, String userName, String status,
                        Set<String> roles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.status = status;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
