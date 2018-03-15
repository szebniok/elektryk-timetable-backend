package com.konradbochnia;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationBody {
    private final String title;
    private final String body;
    private final String icon;
    
    public NotificationBody(String title, String body, String icon) {
        this.title = title;
        this.body = body;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getIcon() {
        return icon;
    }
}
