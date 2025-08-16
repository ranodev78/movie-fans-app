package com.learning.user.authentication.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

public interface AuthSecurityConfigurer {
    SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception;
}
