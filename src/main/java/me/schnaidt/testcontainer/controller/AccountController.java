package me.schnaidt.testcontainer.controller;

import me.schnaidt.testcontainer.persistence.Account;
import me.schnaidt.testcontainer.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class AccountController {

  @Autowired
  private AccountService accountService;

  @GetMapping(path ="/account/{owner}",  produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Account> getOwner(@PathVariable String owner) {

    Optional<Account> result = accountService.findByOwner(owner);

    if (result.isPresent()) {
      return new ResponseEntity<>(result.get(), HttpStatus.OK);
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find resource");
    }
  }

  @PostMapping(path = "/account", consumes = MediaType.APPLICATION_JSON_VALUE,  produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Account> create(@RequestBody Account newAccount) {

    Optional<Account> result = accountService.create(newAccount);

    if (result.isPresent()) {
      return new ResponseEntity<>(result.get(), HttpStatus.CREATED);
    } else {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create resource");
    }
  }

}
