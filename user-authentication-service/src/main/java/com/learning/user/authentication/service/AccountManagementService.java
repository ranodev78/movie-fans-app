package com.learning.user.authentication.service;

import com.learning.user.authentication.controller.AuthController;
import com.learning.user.authentication.dto.UserRegistrationRequest;
import com.learning.user.authentication.dto.sendgrid.SendGridEmailRequest;
import com.learning.user.authentication.mapper.AppUserMapper;
import com.learning.user.authentication.model.AppUser;
import com.learning.user.authentication.model.VerificationToken;
import com.learning.user.authentication.repository.AppUserRepository;
import com.learning.user.authentication.repository.RoleRepository;
import com.learning.user.authentication.repository.VerificationTokenRepository;
import com.learning.user.authentication.service.sendgrid.SendGridEmailClient;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AccountManagementService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManagementService.class);

    private static final long TOKEN_VALIDITY_IN_HOURS = 24L;
    private static final String GENERIC_USER_ROLE_NAME = "ROLE_USER";

    private static final String EMAIL_BODY_TEMPLATE = """
        Hello %s,%n
        Thank you for registering! Please activate your account by clicking on the link below:%n
        %s%n%n
        Best regards,%n
        Movie Discovery
        """;

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenRepository verificationTokenRepository;
    private final SendGridEmailClient sendGridEmailClient;
    private final String authServerUri;
    private final String emailSender;

    @Autowired
    public AccountManagementService(final AppUserRepository userRepository,
                                    final RoleRepository roleRepository,
                                    final PasswordEncoder passwordEncoder,
                                    final VerificationTokenRepository verificationTokenRepository,
                                    final SendGridEmailClient sendGridEmailClient,
                                    @Value("${auth.server.uri}") final String authServerUri,
                                    @Value("${sendgrid.email.sender}") final String emailSender) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
        this.sendGridEmailClient = sendGridEmailClient;
        this.authServerUri = authServerUri;
        this.emailSender = emailSender;
    }

    public AppUser findUserByUsername(String username) {
        return this.userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("No user was found with the given username"));
    }

    @Transactional
    public AppUser registerUser(UserRegistrationRequest request) {
        LOGGER.info("Entering AccountManagementService.registerUser...");

        if (this.userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already in use");
        }

        if (this.userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already in use");
        }

        return this.roleRepository.findByName(GENERIC_USER_ROLE_NAME)
                .map(role -> {
                    final String encodedPassword = this.passwordEncoder.encode(request.password());

                    final AppUser savedUser = this.userRepository.save(
                            AppUserMapper.fromUserRegistrationRequest(request, encodedPassword, role));

                    final SendGridEmailRequest emailRequest = this.buildActivationEmail(savedUser);

                    this.sendGridEmailClient.sendEmail(emailRequest);

                    return savedUser;
                })
                .orElseThrow(() -> new RuntimeException("Role: %s, was not found".formatted(GENERIC_USER_ROLE_NAME)));
    }

    private SendGridEmailRequest buildActivationEmail(AppUser user) {
        final String token = this.createToken(user);
        final String username = user.getUsername();

        final String activationLink = UriComponentsBuilder
                .fromHttpUrl(this.authServerUri)
                .path(AuthController.ACTIVATE_API_PATH)
                .queryParam(AuthController.JWT_TOKEN_REQ_PARAM, token)
                .build()
                .toString();

        final String emailBody = EMAIL_BODY_TEMPLATE.formatted(username, activationLink);

        return AppUserMapper.fromUserRegistrationDetails(user.getEmail(), username, emailBody, this.emailSender);
    }

    private String createToken(AppUser user) {
        final VerificationToken verificationToken = VerificationToken.forUser(user, TOKEN_VALIDITY_IN_HOURS);

        this.verificationTokenRepository.save(verificationToken);

        return verificationToken.getToken();
    }

    @Transactional
    public void activateUser(String token) {
        LOGGER.info("Entering AccountManagementService.activateUser...");

        final VerificationToken verificationToken = this.verificationTokenRepository.findValidToken(token)
                .orElseThrow(() -> new RuntimeException("Requested token was not found"));

        final AppUser user = verificationToken.getUser();

        // Mark token as used
        verificationToken.setUsed(false);
        this.verificationTokenRepository.save(verificationToken);

        // Enable user's account
        user.setEnabled(true);
        this.userRepository.save(user);

        LOGGER.info("User account was successfully activated");
    }
}
