package com.sg.f1bettingservice.core.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Driver {
  private Integer driverId;
  private String fullName;
  private Integer odds;
}
