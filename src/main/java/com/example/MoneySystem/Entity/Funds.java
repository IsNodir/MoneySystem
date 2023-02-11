package com.example.MoneySystem.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "funds")
public class Funds {
  @Id
  @GeneratedValue
  private int id;

  private double balance;

  private double spent;

  private double received;

  @ManyToOne
  Dates dates;
}
