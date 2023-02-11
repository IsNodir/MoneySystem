package com.example.MoneySystem.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DayDTO{

  @JsonProperty(value = "id")
  private int id;

  @JsonProperty(value = "day")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
  private LocalDate day;

}
