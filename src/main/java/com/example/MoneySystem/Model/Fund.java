package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fund {
  @JsonProperty(value = "balance")
  private double balance;

  @JsonProperty(value = "spent")
  private double spent;

  @JsonProperty(value = "received")
  private double received;
}
