package com.toqa.securevault.notification;

public interface NotificationSender {
    void send(String recipient, String message);
}
