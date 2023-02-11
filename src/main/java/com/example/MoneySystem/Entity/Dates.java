package com.example.MoneySystem.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "dates")
public class Dates {
  @Id
  private int id;

  private Timestamp day;
}
