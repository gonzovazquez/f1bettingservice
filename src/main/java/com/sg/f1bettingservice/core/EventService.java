package com.sg.f1bettingservice.core;

import static java.util.Objects.hash;

import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.f1data.F1DataProvider;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventService {

  private final F1DataProvider f1DataProvider;

  public List<Event> findEvents(String sessionType, Integer year, String country) {
    return f1DataProvider.findEvents(sessionType, year, country).stream()
        .map(this::enrichWithDriversMarket)
        .toList();
  }

  public Optional<Event> findByEventId(Integer eventId) {
    return f1DataProvider.findEventById(eventId).map(this::enrichWithDriversMarket);
  }

  public Optional<Driver> findWinnerDriverByEventId(Integer eventId) {
    var driversFromEvent = getDriversByEventWithOdds(eventId);
    return f1DataProvider
        .getWinnerDriverIdByEventId(eventId)
        .map(winnerDriverId -> getWinnerDriverFromList(driversFromEvent, winnerDriverId));
  }

  private Driver getWinnerDriverFromList(List<Driver> driversFromEvent, Integer winnerDriverId) {
    return driversFromEvent.stream()
        .filter(d -> d.getDriverId().equals(winnerDriverId))
        .findFirst()
        .orElseThrow();
  }

  private Event enrichWithDriversMarket(Event event) {
    var driversWithOdds = getDriversByEventWithOdds(event.getEventId());
    event.setDrivers(driversWithOdds);
    return event;
  }

  private List<Driver> getDriversByEventWithOdds(Integer eventId) {
    return f1DataProvider.getDriversByEventId(eventId).stream()
        .map(
            d ->
                Driver.builder()
                    .driverId(d.getDriverId())
                    .fullName(d.getFullName())
                    .odds(computeOdds(eventId, d.getDriverId()))
                    .build())
        .toList();
  }

  private int computeOdds(Integer eventId, Integer driverId) {
    int[] values = {2, 3, 4};
    int h = Math.abs(hash(eventId, driverId));
    return values[h % values.length];
  }
}
