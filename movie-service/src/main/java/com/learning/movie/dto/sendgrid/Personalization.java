package com.learning.movie.dto.sendgrid;

import java.util.List;

public record Personalization(List<EmailAddress> to, String subject) {
}
