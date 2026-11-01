package com.devsync.config;

import com.devsync.auth.util.PasswordUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Spring Security PasswordEncoder adapter wrapping DevSync's PBKDF2 PasswordUtil.
 * Guarantees 100% backward compatibility with all existing stored user password hashes.
 */
@Component
public class DevSyncPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return PasswordUtil.hashPassword(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return PasswordUtil.verifyPassword(rawPassword.toString(), encodedPassword);
    }
}

