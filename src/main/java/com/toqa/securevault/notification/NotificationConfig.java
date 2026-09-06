package com.toqa.securevault.notification;

import com.toqa.securevault.notification.decorator.LoggingNotificationSenderDecorator;
import com.toqa.securevault.notification.decorator.RetryingNotificationSenderDecorator;
import com.toqa.securevault.notification.impl.EmailNotificationSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class NotificationConfig {

    @Bean
    public NotificationSender notificationSender() {
        NotificationSender base = new EmailNotificationSender();
        NotificationSender withRetry = new RetryingNotificationSenderDecorator(base, 3);
        return new LoggingNotificationSenderDecorator(withRetry);
        // call flow: log "sending" -> retry(up to 3x) -> actual email send -> log "sent"
    }
}
