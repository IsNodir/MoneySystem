package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class OperationDTO {

  public OperationDTO() {
  }

  public OperationDTO(int id, double amount, Date date, boolean isExpense) {
    this.id = id;
    this.amount = amount;
    this.date = date;
    this.isExpense = isExpense;
  }

  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "id_user")
  private int idUser;

  @JsonProperty(value = "amount")
  private double amount;

  @JsonProperty(value = "date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private Date date;

  @JsonProperty(value = "is_operation")
  private boolean isOperation;

  @JsonProperty(value = "is_expense")
  private boolean isExpense;

  @Override
  public String toString() {
    if (isExpense)
      return "\nExpense ID: " + this.id + "; Money_amount: -" + this.amount +"; Date: " + date;
    else
      return "\nIncome ID: " + this.id + "; Money_amount: +" + this.amount +"; Date: " + date;
  }
}
