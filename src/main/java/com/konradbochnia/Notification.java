package com.konradbochnia;

import com.fasterxml.jackson.annotation.JsonInclude;

public class Notification {
    private final NotificationBody notification;
    private final String to;

    public Notification(String title, String body, String icon, String to) {
        this.notification = new NotificationBody(title, body, icon);
        this.to = "/topics/" + to;
    }
    
    public Notification(String title, String body, String to) {
        this(title, body, "/icons/logo_512.png", to);
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NotificationBody {
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

    public NotificationBody getNotification() {
        return notification;
    }

    public String getTo() {
        return to;
    }
}
