package com.example.security.jwt.security;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Primary
@Component
@RequiredArgsConstructor
public class CustomAuthenticationManager implements AuthenticationManager {

    private final List<AuthenticationProvider> providers;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthenticationException lastException = null;

        for (AuthenticationProvider provider : providers) {
            if (!provider.supports(authentication.getClass())) {
                continue;
            }

            try {
                Authentication result = provider.authenticate(authentication);
                if (result != null) {
                    return result;
                }
            } catch (AuthenticationException ex) {
                lastException = ex;
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        throw new ProviderNotFoundException(
                "No AuthenticationProvider for " + authentication.getClass().getSimpleName()
        );
    }
}
