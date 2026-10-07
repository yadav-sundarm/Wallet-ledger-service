package com.sundarm.walletledger.ledger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    Optional<LedgerEntry> findByIdempotencyKey(String idempotencyKey);
    List<LedgerEntry> findByReferenceOrderByIdAsc(String reference);
    Page<LedgerEntry> findByAccountIdOrderByIdDesc(Long accountId, Pageable pageable);
}