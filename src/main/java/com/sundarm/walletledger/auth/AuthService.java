package com.sundarm.walletledger.auth;

import com.sundarm.walletledger.account.Account;
import com.sundarm.walletledger.auth.AuthDtos.AuthResponse;
import com.sundarm.walletledger.ledger.LedgerService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final LedgerService ledger;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, LedgerService ledger, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.ledger = ledger;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse register(String email, String rawPassword) {
        String normalized = email.trim().toLowerCase();
        User user = users.saveAndFlush(new User(normalized, encoder.encode(rawPassword))); // duplicate -> 409
        Account account = ledger.createAccount(normalized);
        return new AuthResponse(jwt.issue(user), "Bearer", account.getId(), user.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String rawPassword) {
        User user = users.findByEmail(email.trim().toLowerCase())
                .filter(u -> encoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return new AuthResponse(jwt.issue(user), "Bearer", null, user.getRole().name());
    }
}