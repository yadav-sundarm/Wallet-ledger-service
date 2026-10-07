package com.sundarm.walletledger.auth;

import com.sundarm.walletledger.account.Account;
import com.sundarm.walletledger.account.AccountRepository;
import com.sundarm.walletledger.exception.AccountNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AccessChecker {

    private final AccountRepository accounts;

    public AccessChecker(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public void requireOwnerOrAdmin(Long accountId, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (admin) return;
        Account acc = accounts.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        if (!acc.getOwnerEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("You do not have access to this account");
        }
    }
}