package com.toqa.securevault.notification.decorator;

import com.toqa.securevault.notification.NotificationSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryingNotificationSenderDecorator implements NotificationSender {

    private final NotificationSender delegate;
    private final int maxAttempts;

    public RetryingNotificationSenderDecorator(NotificationSender delegate, int maxAttempts) {
        this.delegate = delegate;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void send(String recipient, String message) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                delegate.send(recipient, message);
                return;
            } catch (RuntimeException e) {
                lastFailure = e;
                log.warn("Notification send attempt {}/{} failed for {}: {}", attempt, maxAttempts, recipient, e.getMessage());
            }
        }
        throw lastFailure;
    }
}
