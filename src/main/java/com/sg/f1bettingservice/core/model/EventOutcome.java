package com.sg.f1bettingservice.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventOutcome {
  private Integer eventId;
  private Integer winnerDriverId;
  private Integer betsWon;
  private Integer betsLost;
}
