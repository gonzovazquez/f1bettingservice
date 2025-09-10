package com.sg.f1bettingservice.rest.bet;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceBetRequest {
  @NotNull private Integer userId;

  @NotNull private Integer eventId;

  @NotNull private Integer driverId;

  @NotNull
  @Min(1)
  private BigDecimal amount;
}
