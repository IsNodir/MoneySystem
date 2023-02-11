package com.example.MoneySystem.Model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class DateDTO {

  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "dayFrom")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private Date dayFrom;

  @JsonProperty(value = "dayTo")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private Date dayTo;
}
