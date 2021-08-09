package me.schnaidt.testcontainer.mq;

import lombok.extern.slf4j.Slf4j;
import me.schnaidt.testcontainer.persistence.Account;
import me.schnaidt.testcontainer.persistence.AccountReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReceiverTest {

  @Container
  static GenericContainer<?> activeMQContainer = new GenericContainer<>("vromero/activemq-artemis:latest-alpine")
      .withExposedPorts(61616)
      .withEnv("DISABLE_SECURITY", "true")
      .waitingFor(Wait.forLogMessage(".*AMQ221007: Server is now live.*\n", 1));

  @Container
  public static GenericContainer<?> mongoDBContainer = new GenericContainer<>("mongo:4.0.10");

  @DynamicPropertySource
  static void setProperties(DynamicPropertyRegistry registry) {
    String mongoUri = "mongodb://" + mongoDBContainer.getContainerIpAddress() + ":" + mongoDBContainer.getMappedPort(27017) + "/reactive";
    log.info(mongoUri);
    Supplier<Object> mongoUriSupplier = new Supplier<Object>() {
      @Override
      public String get() {
        return mongoUri;
      }
    };
    registry.add("spring.data.mongodb.uri", mongoUriSupplier);
    registry.add("spring.artemis.port", activeMQContainer::getFirstMappedPort);
  }

  @Autowired
  private JmsTemplate jmsTemplate;
  @Autowired
  private AccountReactiveRepository accountRepository;

  @AfterEach
  void cleanUp() {
    accountRepository.deleteAll();
  }

  @Test
  void receiveMessage() throws InterruptedException {

    Account newAccount = new Account();
    newAccount.setOwner("Alice");
    newAccount.setValue(100.00);

    jmsTemplate.convertAndSend("accounting", newAccount);
    Thread.sleep(100);

    Account probe = new Account();
    probe.setOwner("Alice");
    Example<Account> example = Example.of(probe);

    Account persisted = accountRepository.findOne(example).block();
    assertThat(persisted.getId()).isNotNull();
    assertThat(persisted.getOwner()).isEqualTo("Alice");
    assertThat(persisted.getValue()).isEqualTo(100.0);
  }

}