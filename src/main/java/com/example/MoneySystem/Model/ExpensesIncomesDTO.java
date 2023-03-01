package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ExpensesIncomesDTO {
  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "money_amount")
  private double money_amount;

  @JsonProperty(value = "day")
  private Date day;

  // Created just to differentiate expense from income while showing to user.
  // UNION used while SELECTing expenses and incomes from database
  @JsonProperty(value = "is_expense")
  private boolean is_expense;

  @Override
  public String toString() {
    if (is_expense)
      return "\nExpense ID: " + this.id + "; Money_amount: " + this.money_amount +"; Date: " + day;
    else
      return "\nIncome ID: " + this.id + "; Money_amount: " + this.money_amount +"; Date: " + day;
  }
}
