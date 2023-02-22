package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpensesDTO {
  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "sender_login")
  private String sender_login;

  @JsonProperty(value = "money_amount")
  private double money_amount;

  @JsonProperty(value = "date_id")
  private int date_id;

  @JsonProperty(value = "is_operation")
  private boolean is_operation;
}
