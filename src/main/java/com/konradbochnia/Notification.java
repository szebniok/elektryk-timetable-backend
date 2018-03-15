package com.konradbochnia;

public class Notification {
    private final NotificationBody notification;
    private final String to;

    public Notification(String title, String body, String icon, String to) {
        this.notification = new NotificationBody(title, body, icon);
        this.to = "/topics/" + to;
    }

    public NotificationBody getNotification() {
        return notification;
    }

    public String getTo() {
        return to;
    }
}
