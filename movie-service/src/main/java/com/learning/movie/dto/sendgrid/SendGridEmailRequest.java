package com.learning.movie.dto.sendgrid;

import java.util.List;

public record SendGridEmailRequest(List<Personalization> personalizations, EmailAddress from, List<Content> content) {
}
