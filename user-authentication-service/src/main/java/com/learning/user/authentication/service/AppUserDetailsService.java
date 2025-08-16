package com.learning.user.authentication.service;

import com.learning.user.authentication.model.UserRole;
import com.learning.user.authentication.repository.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository userRepository;

    public AppUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.userRepository.findByUsername(username)
                .map(appUser -> new User(
                        appUser.getUsername(),
                        appUser.getPassword(),
                        appUser.isEnabled(),
                        true,
                        true,
                        true,
                        appUser.getRoles().stream()
                                .map(UserRole::getName)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet())))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
