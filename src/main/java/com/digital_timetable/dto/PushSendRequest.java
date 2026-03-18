package com.digital_timetable.dto;

public class PushSendRequest {
    private Long userId;
    private String title;
    private String body;
    private String icon;
    private String url;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
