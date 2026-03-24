package com.juanoxx.maintenance.common.util;

import com.juanoxx.maintenance.common.exception.ForbiddenOperationException;
import com.juanoxx.maintenance.security.model.AuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new ForbiddenOperationException("Authenticated user not found");
        }
        return principal;
    }
}
