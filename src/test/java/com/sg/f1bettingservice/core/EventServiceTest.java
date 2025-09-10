package com.sg.f1bettingservice.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.f1data.F1DataProvider;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class EventServiceTest {

  F1DataProvider f1DataProvider = mock(F1DataProvider.class);
  EventService eventService = new EventService(f1DataProvider);

  @Nested
  class FindEventsTests {
    @Test
    void shouldFindEventsFromExternalF1DataProviderAndCalculateOdds() {
      var sessionType = "Race";
      var year = 2025;
      var country = "Spain";
      var driverWithoutOdds1 = Driver.builder().driverId(1).fullName("Carlos Sainz").build();
      var driverWithoutOdds2 = Driver.builder().driverId(2).fullName("Fernando Alonso").build();
      var driversWithoutOdds = List.of(driverWithoutOdds1, driverWithoutOdds2);

      var event =
          Event.builder()
              .eventId(1)
              .sessionType("Race")
              .country("Spain")
              .dateTime(OffsetDateTime.now())
              .drivers(Collections.emptyList())
              .build();
      var events = List.of(event);

      when(f1DataProvider.findEvents(sessionType, year, country)).thenReturn(events);
      when(f1DataProvider.getDriversByEventId(event.getEventId())).thenReturn(driversWithoutOdds);

      var result = eventService.findEvents(sessionType, year, country);
      assertThat(result).hasSameSizeAs(events);
      assertThat(events.getFirst()).isEqualTo(event);
      assertThat(result.getFirst().getDrivers()).hasSameSizeAs(driversWithoutOdds);
      assertThat(result.getFirst().getDrivers())
          .extracting(Driver::getFullName)
          .containsExactlyInAnyOrder(
              driverWithoutOdds1.getFullName(), driverWithoutOdds2.getFullName());
      assertThat(result.getFirst().getDrivers())
          .extracting(Driver::getDriverId)
          .containsExactlyInAnyOrder(
              driverWithoutOdds1.getDriverId(), driverWithoutOdds2.getDriverId());
      assertThat(result.getFirst().getDrivers())
          .extracting(Driver::getOdds)
          .allSatisfy(odds -> assertThat(odds).isBetween(2, 4));

      verify(f1DataProvider).findEvents(sessionType, year, country);
      verify(f1DataProvider).getDriversByEventId(event.getEventId());
    }

    @Test
    void shouldNotCallF1DataProviderToGetDriversWhenNoEventsAreFound() {
      var sessionType = "Race";
      var year = 2025;
      var country = "Spain";

      when(f1DataProvider.findEvents(sessionType, year, country))
          .thenReturn(Collections.emptyList());
      var result = eventService.findEvents(sessionType, year, country);
      assertThat(result).isEmpty();

      verify(f1DataProvider).findEvents(sessionType, year, country);
      verify(f1DataProvider, never()).getDriversByEventId(anyInt());
    }
  }

  @Nested
  class FindByEventIdTests {
    @Test
    void returnsEnrichedEventWhenFound() {
      var eventId = 1001;
      var baseEvent =
          Event.builder()
              .eventId(eventId)
              .name("Italian GP")
              .sessionType("Race")
              .country("Italy")
              .drivers(Collections.emptyList())
              .build();

      var drivers =
          List.of(
              Driver.builder().driverId(44).fullName("Lewis Hamilton").build(),
              Driver.builder().driverId(33).fullName("Max Verstappen").build());

      when(f1DataProvider.findEventById(eventId)).thenReturn(Optional.of(baseEvent));
      when(f1DataProvider.getDriversByEventId(eventId)).thenReturn(drivers);

      var result = eventService.findByEventId(eventId);

      assertThat(result).isPresent();
      var enriched = result.orElseThrow();
      assertThat(enriched.getEventId()).isEqualTo(eventId);
      assertThat(enriched.getDrivers()).hasSameSizeAs(drivers);
      assertThat(enriched.getDrivers())
          .extracting(Driver::getOdds)
          .allSatisfy(o -> assertThat(o).isBetween(2, 4));
    }

    @Test
    void returnsEmptyWhenNotFound() {
      when(f1DataProvider.findEventById(1)).thenReturn(Optional.empty());

      var result = eventService.findByEventId(1);
      assertThat(result).isEmpty();

      verify(f1DataProvider, never()).getDriversByEventId(anyInt());
    }
  }

  @Nested
  class FindWinnerDriverByEventIdTests {
    @Test
    void shouldGetWinnerDriverByFetchingItsNumberByEventIdAndEnrichDriverInformation() {
      var eventId = 1;

      when(f1DataProvider.findEventById(eventId))
          .thenReturn(Optional.of(Event.builder().eventId(eventId).build()));
      Driver lewisHamilton =
          Driver.builder().driverId(44).fullName("Lewis Hamilton").odds(2).build();
      Driver maxVerstappen =
          Driver.builder().driverId(33).fullName("Max Verstappen").odds(4).build();
      var expectedWinnerDriverId = maxVerstappen.getDriverId();
      var eventDrivers = List.of(lewisHamilton, maxVerstappen);

      when(f1DataProvider.getDriversByEventId(eventId)).thenReturn(eventDrivers);
      when(f1DataProvider.getWinnerDriverIdByEventId(eventId))
          .thenReturn(Optional.of(expectedWinnerDriverId));

      var result = eventService.findWinnerDriverByEventId(eventId);
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(maxVerstappen);

      verify(f1DataProvider).getDriversByEventId(eventId);
      verify(f1DataProvider).getWinnerDriverIdByEventId(eventId);
    }
  }
}
