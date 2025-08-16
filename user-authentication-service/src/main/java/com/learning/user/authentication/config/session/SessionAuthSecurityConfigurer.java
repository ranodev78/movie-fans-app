package com.learning.user.authentication.config.session;

import com.learning.user.authentication.config.AuthSecurityConfigurer;
import com.learning.user.authentication.service.AppUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "auth.mode", havingValue = "session")
public class SessionAuthSecurityConfigurer implements AuthSecurityConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAuthSecurityConfigurer.class.getName());

    private static final String SESSION_LOGOUT_ERROR_RESPONSE_JSON = "{\"message\":\"No active session to logout\",\"status\":\"error\"}";

    private final LogoutSuccessHandler logoutHandler;
    private final AppUserDetailsService userDetailsService;
    private final SessionRegistry sessionRegistry;

    @Autowired
    public SessionAuthSecurityConfigurer(final LogoutSuccessHandler logoutHandler,
                                         final AppUserDetailsService userDetailsService,
                                         final SessionRegistry sessionRegistry) {
        this.logoutHandler = logoutHandler;
        this.userDetailsService = userDetailsService;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        }))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/login", "/error")
                                .permitAll()
                                .anyRequest()
                                .authenticated())
                .userDetailsService(this.userDetailsService)
                .formLogin(form -> form
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            this.sessionRegistry.registerNewSession(request.getSession().getId(), authentication.getPrincipal());
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                        .failureHandler((request, response, exception) ->
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)))
                .logout(logoutConfigurer -> logoutConfigurer
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(this.logoutHandler)
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(this.sessionRegistry))
                .build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public LogoutSuccessHandler sessionLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            if (authentication == null) {
                LOGGER.warn("Logout attempt with no active authentication");

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(SESSION_LOGOUT_ERROR_RESPONSE_JSON);
                response.getWriter().flush();
            } else {
                LOGGER.info("Successfully logged out user: {}", authentication.getName());

                final String sessionId = request.getSession(false) == null ? null : request.getSession(false).getId();
                if (sessionId != null) {
                    this.sessionRegistry.removeSessionInformation(sessionId);
                }

                authentication.setAuthenticated(false);
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.sendRedirect("/login");
            }
        };
    }
}
