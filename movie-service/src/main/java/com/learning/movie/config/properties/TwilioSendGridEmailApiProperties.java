package com.learning.movie.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "twilio.sendgrid.email.api")
public class TwilioSendGridEmailApiProperties {
    private final String key;
    private final String url;
    private final String senderEmail;
    private final String senderName;

    public TwilioSendGridEmailApiProperties(final String key, final String url, final String senderEmail,
                                            final String senderName) {
        this.key = key;
        this.url = url;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }
}
