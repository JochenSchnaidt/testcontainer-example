package me.schnaidt.testcontainer.mq;

import lombok.extern.slf4j.Slf4j;
import me.schnaidt.testcontainer.persistence.Account;
import me.schnaidt.testcontainer.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Receiver {

  @Autowired
  private AccountService accountService;

  @JmsListener(destination = "accounting", containerFactory = "myFactory")
  public void receiveMessage(Account account) {
    log.info("received message: {}", account);
    accountService.create(account);
  }

}