# Wallet Ledger Service

Spring Boot REST API for a wallet ledger: accounts, deposits, withdrawals, and atomic transfers using double-entry bookkeeping.

## Features
- Double-entry transfers: each transfer writes a debit and a credit entry sharing one reference
- Idempotency: the `Idempotency-Key` header makes retried transfers safe
- Concurrency safety: pessimistic row locking, acquired in account-id order to avoid deadlocks
- JWT authentication, BCrypt password hashing, role-based access (USER/ADMIN)
- Ownership checks: users can only access their own account
- BigDecimal money handling, request validation, structured error responses, paginated history

## Tech
Java 17, Spring Boot, Spring Data JPA/Hibernate, MySQL, Spring Security

## Run locally
1. `CREATE DATABASE wallet_ledger;`
2. Set env vars `DB_PASSWORD` and `JWT_SECRET` (32+ characters)
3. `./mvnw spring-boot:run`

## API
| Method | Path | Description |
|---|---|---|
| POST | /api/auth/register | Create user + wallet account, returns JWT |
| POST | /api/auth/login | Returns JWT |
| GET | /api/accounts/{id} | Balance |
| POST | /api/accounts/{id}/deposit | Deposit |
| POST | /api/accounts/{id}/withdraw | Withdraw (422 if insufficient funds) |
| POST | /api/transfers | Atomic transfer (supports Idempotency-Key) |
| GET | /api/accounts/{id}/entries | Paginated ledger history |