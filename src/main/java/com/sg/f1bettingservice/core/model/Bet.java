package com.sg.f1bettingservice.core.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Bet {
  private Integer id;
  private Integer userId;
  private BetStatus status;
  private Integer eventId;
  private Integer driverId;
  private BigDecimal amount;
  private Integer remainingBalance;
}
