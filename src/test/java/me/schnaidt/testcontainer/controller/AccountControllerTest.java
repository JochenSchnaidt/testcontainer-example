package me.schnaidt.testcontainer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.schnaidt.testcontainer.persistence.Account;
import me.schnaidt.testcontainer.persistence.AccountReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountControllerTest {

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
  }

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper om;
  @Autowired
  private AccountReactiveRepository accountRepository;

  @AfterEach
  void cleanUp() {
    accountRepository.deleteAll();
  }

  @Test
  void getOwnerTest() throws Exception {

    Account findMe = new Account();
    findMe.setOwner("Bob");
    findMe.setValue(20.00);

    Account persisted = accountRepository.save(findMe).block();
    assertThat(persisted.getId()).isNotNull();
    assertThat(persisted.getOwner()).isEqualTo("Bob");
    assertThat(persisted.getValue()).isEqualTo(20.0);

    MvcResult mvcResult = mockMvc.perform(get("/account/{owner}", "Bob"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$.owner").value("Bob"))
        .andExpect(jsonPath("$.value").value(20.0))
        .andReturn();

    Account result = om.readValue(mvcResult.getResponse().getContentAsString(), Account.class);
    assertThat(result.getId()).isNotNull();
    assertThat(result.getOwner()).isEqualTo("Bob");
    assertThat(result.getValue()).isEqualTo(20.0);
  }

  @Test
  void createTest() throws Exception {

    Account newAccount = new Account();
    newAccount.setOwner("Alice");
    newAccount.setValue(100.00);

    MvcResult mvcResult = mockMvc.perform(post("/account")
        .contentType(MediaType.APPLICATION_JSON)
        .characterEncoding(StandardCharsets.UTF_8.name())
        .content(om.writeValueAsString(newAccount)))
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();

    Account result = om.readValue(mvcResult.getResponse().getContentAsString(), Account.class);
    assertThat(result.getId()).isNotNull();
    assertThat(result.getOwner()).isEqualTo("Alice");
    assertThat(result.getValue()).isEqualTo(100.0);


    Account probe = new Account();
    probe.setOwner("Alice");
    Example<Account> example = Example.of(probe);

    Account persisted = accountRepository.findOne(example).block();
    assertThat(persisted.getId()).isNotNull();
    assertThat(persisted.getOwner()).isEqualTo("Alice");
    assertThat(persisted.getValue()).isEqualTo(100.0);
  }
}