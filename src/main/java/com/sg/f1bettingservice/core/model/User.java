package com.sg.f1bettingservice.core.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
  private Integer userId;
  private BigDecimal balance;
}
