package com.sundarm.walletledger.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Account " + id + " not found");
    }
}