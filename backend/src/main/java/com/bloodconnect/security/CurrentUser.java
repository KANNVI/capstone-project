package com.bloodconnect.security;

import com.bloodconnect.exception.ApiException;
import com.bloodconnect.model.User;
import com.bloodconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Gives controllers the logged-in User entity. */
@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository users;

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return users.findByEmail(auth.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Not logged in"));
    }
}
