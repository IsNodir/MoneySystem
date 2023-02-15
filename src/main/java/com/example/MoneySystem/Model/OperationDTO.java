package com.example.MoneySystem.Model;

import com.example.MoneySystem.Entity.Users;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class OperationDTO {

  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "receiver")
  private String receiver;

  @JsonProperty(value = "sender")
  private String sender;

  @JsonProperty(value = "money_amount")
  private double money_amount;

//  @JsonProperty(value = "fund_id")
//  private int fundId;
}
