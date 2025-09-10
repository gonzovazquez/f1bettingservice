package com.sg.f1bettingservice.rest.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sg.f1bettingservice.core.EventService;
import com.sg.f1bettingservice.core.OutcomeService;
import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.core.model.EventOutcome;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class EventControllerTest {

  EventService eventService = mock(EventService.class);
  OutcomeService outcomeService = mock(OutcomeService.class);
  EventController controller = new EventController(eventService, outcomeService);

  @Test
  void shouldReturnListOfEventsTest() {
    var sessionType = "Race";
    var year = 2025;
    var country = "Spain";
    var drivers = Driver.builder().driverId(1).fullName("Carlos Sainz").odds(1).build();
    var event =
        Event.builder()
            .eventId(1)
            .sessionType("Race")
            .country("Spain")
            .dateTime(OffsetDateTime.now())
            .drivers(List.of(drivers))
            .build();
    var events = List.of(event);
    when(eventService.findEvents(sessionType, year, country)).thenReturn(events);
    assertThat(controller.findEvents(sessionType, year, country)).isEqualTo(events);

    verify(eventService).findEvents(sessionType, year, country);
  }

  @Test
  void shouldPublishEventOutcomeTest() {
    var eventId = 1;
    var request = PublishOutcomeRequest.builder().eventId(eventId).build();
    var outcome =
        EventOutcome.builder().eventId(eventId).winnerDriverId(1).betsWon(1).betsLost(1).build();

    when(outcomeService.publishOutcome(request.getEventId())).thenReturn(outcome);
    var result = controller.publishOutcome(request);
    assertThat(result).isEqualTo(outcome);

    verify(outcomeService).publishOutcome(eventId);
  }
}
