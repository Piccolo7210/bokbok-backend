package com.bokbok.meow.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtils {

    // Call this from any controller/service to get logged-in user's ID
    public static String getCurrentUserId() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername(); // username = userId in our setup
        }
        return principal.toString();
    }
}
