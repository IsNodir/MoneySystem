package com.example.MoneySystem.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
public class Users {

  @Id
  private String login;

  private String password;

  private int fundId;

  @OneToMany
  List<Funds> funds;
}
