package com.sundarm.walletledger.ledger;

import com.sundarm.walletledger.account.Account;
import com.sundarm.walletledger.account.AccountRepository;
import com.sundarm.walletledger.exception.AccountNotFoundException;
import com.sundarm.walletledger.exception.InsufficientFundsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final AccountRepository accounts;
    private final LedgerEntryRepository entries;

    public LedgerService(AccountRepository accounts, LedgerEntryRepository entries) {
        this.accounts = accounts;
        this.entries = entries;
    }

    @Transactional
    public Account createAccount(String ownerEmail) {
        Account a = new Account();
        a.setOwnerEmail(ownerEmail);
        return accounts.save(a); // duplicate email -> unique constraint -> 409
    }

    @Transactional(readOnly = true)
    public Account getAccount(Long id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public LedgerEntry deposit(Long accountId, BigDecimal amount, String key) {
        LedgerEntry replay = replay(key);
        if (replay != null) return replay;

        Account acc = lock(accountId);
        acc.setBalance(acc.getBalance().add(amount));
        return entries.save(new LedgerEntry(acc, EntryType.DEPOSIT, amount,
                acc.getBalance(), UUID.randomUUID().toString(), key));
    }

    @Transactional
    public LedgerEntry withdraw(Long accountId, BigDecimal amount, String key) {
        LedgerEntry replay = replay(key);
        if (replay != null) return replay;

        Account acc = lock(accountId);
        if (acc.getBalance().compareTo(amount) < 0) throw new InsufficientFundsException(accountId);
        acc.setBalance(acc.getBalance().subtract(amount));
        return entries.save(new LedgerEntry(acc, EntryType.WITHDRAWAL, amount,
                acc.getBalance(), UUID.randomUUID().toString(), key));
    }

    /** Returns [debitEntry, creditEntry]. */
    @Transactional
    public List<LedgerEntry> transfer(Long fromId, Long toId, BigDecimal amount, String key) {
        if (fromId.equals(toId)) throw new IllegalArgumentException("Cannot transfer to the same account");

        LedgerEntry replay = replay(key);
        if (replay != null) return entries.findByReferenceOrderByIdAsc(replay.getReference());

        // lock in ascending id order so two opposite transfers can't deadlock
        Account first = lock(Math.min(fromId, toId));
        Account second = lock(Math.max(fromId, toId));
        Account from = first.getId().equals(fromId) ? first : second;
        Account to = from == first ? second : first;

        if (from.getBalance().compareTo(amount) < 0) throw new InsufficientFundsException(fromId);
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        String ref = UUID.randomUUID().toString();
        LedgerEntry debit = entries.save(new LedgerEntry(from, EntryType.TRANSFER_DEBIT, amount,
                from.getBalance(), ref, key));
        LedgerEntry credit = entries.save(new LedgerEntry(to, EntryType.TRANSFER_CREDIT, amount,
                to.getBalance(), ref, null));
        return List.of(debit, credit);
    }

    @Transactional(readOnly = true)
    public Page<LedgerEntry> history(Long accountId, Pageable pageable) {
        if (!accounts.existsById(accountId)) throw new AccountNotFoundException(accountId);
        return entries.findByAccountIdOrderByIdDesc(accountId, pageable);
    }

    private Account lock(Long id) {
        return accounts.findByIdForUpdate(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    private LedgerEntry replay(String key) {
        if (key == null || key.isBlank()) return null;
        return entries.findByIdempotencyKey(key).orElse(null);
    }
}