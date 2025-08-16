package com.learning.user.authentication.mapper;

import com.learning.user.authentication.dto.UserRegistrationRequest;
import com.learning.user.authentication.dto.sendgrid.Content;
import com.learning.user.authentication.dto.sendgrid.EmailAddress;
import com.learning.user.authentication.dto.sendgrid.Personalization;
import com.learning.user.authentication.dto.sendgrid.SendGridEmailRequest;
import com.learning.user.authentication.model.AppUser;
import com.learning.user.authentication.model.UserRole;
import org.springframework.http.MediaType;

import java.util.List;

public final class AppUserMapper {
    private static final String EMAIL_SENDER_NAME = "movie-discovery";
    private static final String EMAIL_SUBJECT = "Movie Discovery account activation";

    private AppUserMapper() {
    }

    public static AppUser fromUserRegistrationRequest(UserRegistrationRequest request, String encodedPassword,
                                                      UserRole role) {
        final AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(encodedPassword);
        user.setEnabled(false);
        user.getRoles().add(role);

        return user;
    }

    public static SendGridEmailRequest fromUserRegistrationDetails(String email, String username, String emailBody,
                                                                   String sender) {
        final Personalization personalization = new Personalization(
                List.of(new EmailAddress(email, username)), EMAIL_SUBJECT);

        final EmailAddress from = new EmailAddress(sender, EMAIL_SENDER_NAME);
        final Content content = new Content(MediaType.TEXT_PLAIN_VALUE, emailBody);

        return new SendGridEmailRequest(List.of(personalization), from, List.of(content));
    }
}
