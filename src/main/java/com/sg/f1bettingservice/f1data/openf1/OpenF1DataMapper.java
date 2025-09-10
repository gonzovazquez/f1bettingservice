package com.sg.f1bettingservice.f1data.openf1;

import static java.time.OffsetDateTime.parse;
import static java.util.Optional.ofNullable;

import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OpenF1DataMapper {
  public List<Event> toEventList(List<OpenF1Session> openF1Session) {
    return ofNullable(openF1Session).map(this::toNotNullableEventList).orElse(List.of());
  }

  private List<Event> toNotNullableEventList(List<OpenF1Session> openF1Sessions) {
    return openF1Sessions.stream()
        .map(
            s ->
                Event.builder()
                    .eventId(s.session_key())
                    .name(s.session_name())
                    .sessionType(s.session_type())
                    .country(s.country_name())
                    .dateTime(parse(s.date_start()))
                    .drivers(Collections.emptyList())
                    .build())
        .toList();
  }

  public List<Driver> toDriverList(List<OpenF1Driver> openF1Drivers) {
    return ofNullable(openF1Drivers).map(this::toNotNullableDriverList).orElse(List.of());
  }

  private List<Driver> toNotNullableDriverList(List<OpenF1Driver> openF1Drivers) {
    return openF1Drivers.stream()
        .map(d -> Driver.builder().driverId(d.driver_number()).fullName(d.full_name()).build())
        .toList();
  }
}
