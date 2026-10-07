package com.sundarm.walletledger.ledger;

import com.sundarm.walletledger.account.Account;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record CreateAccountRequest(@NotBlank @Email String ownerEmail) {}

    public record AmountRequest(
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount) {}

    public record TransferRequest(
            @NotNull Long fromAccountId,
            @NotNull Long toAccountId,
            @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount) {}

    public record AccountResponse(Long id, String ownerEmail, BigDecimal balance) {
        public static AccountResponse from(Account a) {
            return new AccountResponse(a.getId(), a.getOwnerEmail(), a.getBalance());
        }
    }

    public record EntryResponse(Long id, Long accountId, EntryType type, BigDecimal amount,
                                BigDecimal balanceAfter, String reference, Instant createdAt) {
        public static EntryResponse from(LedgerEntry e) {
            return new EntryResponse(e.getId(), e.getAccount().getId(), e.getType(), e.getAmount(),
                    e.getBalanceAfter(), e.getReference(), e.getCreatedAt());
        }
    }

    public record TransferResponse(String reference, EntryResponse debit, EntryResponse credit) {}

    public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {}
}