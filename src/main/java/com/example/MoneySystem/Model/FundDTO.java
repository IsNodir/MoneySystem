package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
public class FundDTO {

  public FundDTO() {}

  public FundDTO(double balance, Date date) {
    this.balance = balance;
    this.date = date;
  }

  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "id_user")
  private int idUser;

  @JsonProperty(value = "balance")
  private double balance;

  @JsonProperty(value = "change_in_balance")
  private double changeInBalance;

  @JsonProperty(value = "date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private Date date;

  @Override
  public String toString() {
    return "\nDate: " + this.date + " Balance: " + this.balance;
  }
}
