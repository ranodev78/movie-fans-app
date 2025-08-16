package com.learning.movie.mapper;

import com.learning.movie.dto.sendgrid.Content;
import com.learning.movie.dto.sendgrid.EmailAddress;
import com.learning.movie.dto.sendgrid.Personalization;
import com.learning.movie.dto.sendgrid.SendGridEmailRequest;
import com.learning.movie.dto.subscription.MovieReleaseSubscriptionWithPlatforms;
import com.learning.movie.dto.subscription.StreamingPlatform;
import com.learning.movie.model.subscription.MovieReleaseSubscription;
import com.learning.movie.model.subscription.SubscriptionPlatform;
import org.springframework.http.MediaType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class NotificationMapper {
    private NotificationMapper() {}

    public static SendGridEmailRequest fromMovieStreamingReleaseSubDetails(String email, String movie,
                                                                           Set<StreamingPlatform> streamingPlatforms) {
        final Personalization personalization = new Personalization();
        personalization.setTo(List.of(new EmailAddress(email, "user")));

        personalization.setSubject(
                "%s has been released on: %s!".formatted(movie, formatStreamingPlatforms(streamingPlatforms)));

        final EmailAddress from = new EmailAddress("ranochrono29@gmail.com", "movie-discovery");

        final Content content = new Content();
        content.setType(MediaType.TEXT_PLAIN_VALUE);

        content.setValue("""
                Hi user,
                
                %s has been released on the following platform(s):%n
                %s
                """.formatted(movie, formatStreamingPlatforms(streamingPlatforms)));

        final SendGridEmailRequest emailRequest = new SendGridEmailRequest();
        emailRequest.setPersonalizations(List.of(personalization));
        emailRequest.setFrom(from);
        emailRequest.setContent(List.of(content));

        return emailRequest;
    }

    private static String formatStreamingPlatforms(Set<StreamingPlatform> streamingPlatforms) {
        return streamingPlatforms.size() > 1
                ? streamingPlatforms.stream()
                    .map(StreamingPlatform::getDisplayName)
                    .collect(Collectors.joining(", "))
                    .trim()
                : streamingPlatforms.iterator().next().getDisplayName();
    }

    public static MovieReleaseSubscriptionWithPlatforms consolidateSubscriptionAndPlatforms(MovieReleaseSubscription subscription,
                                                                                            Set<SubscriptionPlatform> platforms) {
        return platforms.stream()
                .map(SubscriptionPlatform::getPlatform)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(HashSet::new),
                        streamingPlatforms -> new MovieReleaseSubscriptionWithPlatforms(subscription, streamingPlatforms)
                ));
    }
}
