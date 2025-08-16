package com.learning.movie.dto.sendgrid;

import java.util.List;

public class SendGridEmailRequest {
    private List<Personalization> personalizations;
    private EmailAddress from;
    private List<Content> content;

    public List<Personalization> getPersonalizations() {
        return personalizations;
    }

    public void setPersonalizations(List<Personalization> personalizations) {
        this.personalizations = personalizations;
    }

    public EmailAddress getFrom() {
        return from;
    }

    public void setFrom(EmailAddress from) {
        this.from = from;
    }

    public List<Content> getContent() {
        return content;
    }

    public void setContent(List<Content> content) {
        this.content = content;
    }
}
