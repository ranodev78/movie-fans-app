package com.learning.user.authentication.config;

import com.learning.user.authentication.model.AppUser;
import com.learning.user.authentication.model.UserRole;
import com.learning.user.authentication.repository.AppUserRepository;
import com.learning.user.authentication.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    private final AuthSecurityConfigurer delegate;

    @Autowired
    public SecurityConfig(final AuthSecurityConfigurer delegate) {
        this.delegate = delegate;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${auth.frontend.client.app.origin}") final String appOrigin) {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(appOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        return this.delegate.configure(httpSecurity);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner init(final AppUserRepository userRepository, 
                                  final PasswordEncoder passwordEncoder,
                                  final RoleRepository roleRepository,
                                  @Value("${admin.username}") final String adminUsername,
                                  @Value("${admin.email}") final String adminEmail,
                                  @Value("${admin.password}") final String adminPassword) {
        LOGGER.info("Inserting test admin user");

        return args -> userRepository.findByUsername(adminUsername)
                .ifPresentOrElse(
                    existingAdmin -> LOGGER.info("Admin user '{}' already exists, skipping creation", adminUsername),
                    () -> {
                        final UserRole adminRole = roleRepository.findByName("ROLE_ADMIN")
                                .orElseGet(() -> roleRepository.save(new UserRole("ROLE_ADMIN")));

                        final AppUser adminUser = new AppUser();
                        adminUser.setUsername(adminUsername);
                        adminUser.setEmail(adminEmail);
                        adminUser.setPassword(passwordEncoder.encode(adminPassword));
                        adminUser.setEnabled(true);
                        adminUser.setRoles(Set.of(adminRole));

                        userRepository.save(adminUser);

                        LOGGER.info("Admin user '{}' was created successfully", adminUsername);
                    });
    }
}
