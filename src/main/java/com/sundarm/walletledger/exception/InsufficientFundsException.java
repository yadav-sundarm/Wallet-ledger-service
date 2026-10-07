package com.sundarm.walletledger.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(Long accountId) {
        super("Insufficient funds in account " + accountId);
    }
}