package com.sundarm.walletledger.ledger;

import com.sundarm.walletledger.auth.AccessChecker;
import com.sundarm.walletledger.ledger.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LedgerController {

    private final LedgerService service;
    private final AccessChecker access;

    public LedgerController(LedgerService service, AccessChecker access) {
        this.service = service;
        this.access = access;
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse get(@PathVariable Long id, Authentication auth) {
        access.requireOwnerOrAdmin(id, auth);
        return AccountResponse.from(service.getAccount(id));
    }

    @PostMapping("/accounts/{id}/deposit")
    public EntryResponse deposit(@PathVariable Long id, @Valid @RequestBody AmountRequest req,
                                 @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                 Authentication auth) {
        access.requireOwnerOrAdmin(id, auth);
        return EntryResponse.from(service.deposit(id, req.amount(), key));
    }

    @PostMapping("/accounts/{id}/withdraw")
    public EntryResponse withdraw(@PathVariable Long id, @Valid @RequestBody AmountRequest req,
                                  @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                  Authentication auth) {
        access.requireOwnerOrAdmin(id, auth);
        return EntryResponse.from(service.withdraw(id, req.amount(), key));
    }

    @PostMapping("/transfers")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest req,
                                     @RequestHeader(value = "Idempotency-Key", required = false) String key,
                                     Authentication auth) {
        access.requireOwnerOrAdmin(req.fromAccountId(), auth);
        List<LedgerEntry> pair = service.transfer(req.fromAccountId(), req.toAccountId(), req.amount(), key);
        return new TransferResponse(pair.get(0).getReference(),
                EntryResponse.from(pair.get(0)), EntryResponse.from(pair.get(1)));
    }

    @GetMapping("/accounts/{id}/entries")
    public PageResponse<EntryResponse> history(@PathVariable Long id,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               Authentication auth) {
        access.requireOwnerOrAdmin(id, auth);
        Page<LedgerEntry> p = service.history(id,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
        return new PageResponse<>(p.getContent().stream().map(EntryResponse::from).toList(),
                p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages());
    }
}