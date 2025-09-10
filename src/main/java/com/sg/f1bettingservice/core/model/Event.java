package com.sg.f1bettingservice.core.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
  private Integer eventId;
  private String name;
  private String sessionType;
  private String country;
  private OffsetDateTime dateTime;
  private List<Driver> drivers;
}
