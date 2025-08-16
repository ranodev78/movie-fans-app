package com.learning.user.authentication.dto.sendgrid;

import java.util.List;

public record SendGridEmailRequest (List<Personalization> personalizations, EmailAddress from, List<Content> content) {}
