package com.toqa.securevault.notification.impl;

import com.toqa.securevault.notification.NotificationSender;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EmailNotificationSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        log.info("[stub email] to={} message={}", recipient, message);
    }
}
