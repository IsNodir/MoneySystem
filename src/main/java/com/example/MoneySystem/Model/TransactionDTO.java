package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class TransactionDTO {

  @JsonProperty(value = "id_sender")
  private int idSender;

  @JsonProperty(value = "id_receiver")
  private int idReceiver;

  @JsonProperty(value = "amount")
  private double amount;

  @JsonProperty(value = "date")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private Date date;

}
