package com.techwhizer.snsbiosystem.model;

import java.util.Set;

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
