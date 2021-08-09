package me.schnaidt.testcontainer.service;

import lombok.extern.slf4j.Slf4j;
import me.schnaidt.testcontainer.persistence.Account;
import me.schnaidt.testcontainer.persistence.AccountReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

  @Autowired
  private AccountReactiveRepository accountRepository;

  @Override
  public Optional<Account> findByOwner(String owner) {

    Account probe = new Account();
    probe.setOwner(owner);
    Example<Account> example = Example.of(probe);

    Mono<Account> one = accountRepository.findOne(example);
    return one.blockOptional();
  }

  @Override
  public Optional<Account> create(Account newAccount) {
    return accountRepository.save(newAccount).blockOptional();
  }

}
