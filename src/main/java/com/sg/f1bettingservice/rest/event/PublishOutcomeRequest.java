package com.sg.f1bettingservice.rest.event;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PublishOutcomeRequest {
  @NotNull private Integer eventId;
}
