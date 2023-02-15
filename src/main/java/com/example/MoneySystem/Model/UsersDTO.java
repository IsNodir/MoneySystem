package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UsersDTO {

  @JsonProperty(value = "login")
  private String login;

  @JsonProperty(value = "password")
  private String password;

//  @JsonProperty(value = "fund_id")
//  private int fundId;
}
