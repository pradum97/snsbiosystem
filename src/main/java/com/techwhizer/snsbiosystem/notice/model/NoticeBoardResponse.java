package com.techwhizer.snsbiosystem.notice.model;

import java.util.List;

public class NoticeBoardResponse {

    private String message;
    private List<Object> notices;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Object> getNotices() {
        return notices;
    }

    public void setNotices(List<Object> notices) {
        this.notices = notices;
    }

}
