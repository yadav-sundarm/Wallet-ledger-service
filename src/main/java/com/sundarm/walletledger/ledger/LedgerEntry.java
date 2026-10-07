package com.sundarm.walletledger.ledger;

import com.sundarm.walletledger.account.Account;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false)
    private String reference;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected LedgerEntry() {}

    public LedgerEntry(Account account, EntryType type, BigDecimal amount,
                       BigDecimal balanceAfter, String reference, String idempotencyKey) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.reference = reference;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getId() { return id; }
    public Account getAccount() { return account; }
    public EntryType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public String getReference() { return reference; }
    public Instant getCreatedAt() { return createdAt; }
}