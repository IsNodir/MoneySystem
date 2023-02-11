package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class FundDTO {

//  public FundDTO(double balance, double spent, double received, Date day)
//  {
//    this.balance = balance;
//    this.spent = spent;
//    this.received = received;
//    this.day = day;
//  }
  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "balance")
  private double balance;

  @JsonProperty(value = "spent")
  private double spent;

  @JsonProperty(value = "received")
  private double received;

//  @JsonProperty(value = "day")
//  private Date day;

  @Override
  public String toString() {
    return "\nBalance: " + this.balance + "; Spent: " + this.spent + "; Received: " + this.received;
  }
}
