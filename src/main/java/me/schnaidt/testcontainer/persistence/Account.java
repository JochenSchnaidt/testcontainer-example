package me.schnaidt.testcontainer.persistence;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document
public class Account implements Serializable {

  @Id
  private String id;
  private String owner;
  private Double value;

}