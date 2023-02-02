package com.techwhizer.snsbiosystem.notice.model;

import java.util.List;

public class NoticePageResponse {

    private int totalPage;

    private long totalRecord;

    private List<NoticeBoardDTO> notices;

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public List<NoticeBoardDTO> getNotices() {
        return notices;
    }

    public void setNotices(List<NoticeBoardDTO> notices) {
        this.notices = notices;
    }
}
