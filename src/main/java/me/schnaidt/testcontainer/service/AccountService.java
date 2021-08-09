package me.schnaidt.testcontainer.service;

import me.schnaidt.testcontainer.persistence.Account;

import java.util.Optional;

public interface AccountService {
  Optional<Account> findByOwner(String owner);
  Optional<Account> create(Account newAccount);
}
