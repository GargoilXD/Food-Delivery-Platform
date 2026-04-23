package com.auth.service;

import com.auth.dto.AuthRequest;
import com.auth.dto.AuthResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.UserResponse;
import com.auth.exception.DuplicateResourceException;
import com.auth.exception.ResourceNotFoundException;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: username={}, email={}", request.username(), request.email());
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed - username already taken: {}", request.username());
            throw new DuplicateResourceException("Username already taken");
        }
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed - email already registered: {}", request.email());
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : com.auth.model.Role.CUSTOMER);

        userRepository.save(user);
        String token = jwtTokenProvider.generateToken(
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(user.getUsername(), request.password())
                )
        );
        log.info("User registered successfully: id={}, username={}, role={}", user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        log.info("Login attempt: username={}", request.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = (User) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(authentication);
        log.info("Login successful: username={}, role={}", user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(String username) {
        log.debug("Profile lookup: username={}", username);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return UserResponse.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        log.debug("Checking user existence: userId={}", userId);
        return userRepository.existsById(userId);
    }
}
