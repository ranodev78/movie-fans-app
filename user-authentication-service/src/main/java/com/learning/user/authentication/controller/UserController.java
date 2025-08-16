package com.learning.user.authentication.controller;

import com.learning.user.authentication.dto.UserDetailsResponse;
import com.learning.user.authentication.model.AppUser;
import com.learning.user.authentication.service.AccountManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1.0/users")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private static final String PLAIN_TEXT_CHARSET_UTF_8 = "text/plain;charset=UTF-8";

    private final AccountManagementService accountManagementService;

    @Autowired
    public UserController(AccountManagementService accountManagementService) {
        this.accountManagementService = accountManagementService;
    }

    @GetMapping(produces = PLAIN_TEXT_CHARSET_UTF_8)
    public ResponseEntity<String> getCurrentLoggedInUser(final Principal principal) {
        LOGGER.info("Retrieving principal...");

        if (principal == null) {
            LOGGER.warn("Failed to retrieve principal");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final String username = principal.getName();

        LOGGER.info("Successfully identified principal with name: {}", username);

        return ResponseEntity.ok(username);
    }

    @GetMapping(value = "/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDetailsResponse> getUserDetails(@PathVariable String username,
                                                              final Principal principal) {
        LOGGER.info("Retrieving user details by username...");

        if (principal == null) {
            LOGGER.warn("User is not logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!principal.getName().equals(username)) {
            LOGGER.warn("User is not allowed to request data");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final AppUser user = this.accountManagementService.findUserByUsername(username);

        return ResponseEntity.ok()
                .body(new UserDetailsResponse(user.getUsername(), user.getEmail()));
    }
}
