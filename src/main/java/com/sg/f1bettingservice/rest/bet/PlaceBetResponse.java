package com.sg.f1bettingservice.rest.bet;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceBetResponse {
  private Integer betId;
}
