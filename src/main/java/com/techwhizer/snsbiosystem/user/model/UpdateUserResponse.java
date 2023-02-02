package com.techwhizer.snsbiosystem.user.model;

public class UpdateUserResponse {

    UserDTO data;
    String message;

    public UserDTO getData() {
        return data;
    }

    public void setData(UserDTO data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
