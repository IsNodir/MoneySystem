package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpensesIncomesDTO {
  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "money_amount")
  private double money_amount;

  @JsonProperty(value = "date_id")
  private int date_id;

  // Created just to differentiate expense from income while showing to user.
  // UNION used while SELECTing expenses and incomes from database
  @JsonProperty(value = "is_expense")
  private boolean is_expense;

  @Override
  public String toString() {
    if (is_expense)
      return "\nExpense ID: " + this.id + "; Money_amount: " + this.money_amount;
    else
      return "\nIncome ID: " + this.id + "; Money_amount: " + this.money_amount;
  }
}
