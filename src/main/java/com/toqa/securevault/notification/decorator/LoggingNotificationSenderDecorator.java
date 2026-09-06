package com.toqa.securevault.notification.decorator;

import com.toqa.securevault.notification.NotificationSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingNotificationSenderDecorator implements NotificationSender {

    private final NotificationSender delegate;

    public LoggingNotificationSenderDecorator(NotificationSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(String recipient, String message) {
        log.info("Sending notification to {}", recipient);
        delegate.send(recipient, message);
        log.info("Notification sent to {}", recipient);
    }
}
