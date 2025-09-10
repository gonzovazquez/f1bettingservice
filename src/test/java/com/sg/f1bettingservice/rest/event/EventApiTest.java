package com.sg.f1bettingservice.rest.event;

import static java.time.OffsetDateTime.parse;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.f1bettingservice.core.EventService;
import com.sg.f1bettingservice.core.OutcomeService;
import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.core.model.EventOutcome;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EventController.class)
public class EventApiTest {

  @Autowired MockMvc mvc;

  @Autowired ObjectMapper om;

  @MockitoBean EventService eventService;

  @MockitoBean OutcomeService outcomeService;

  @Nested
  class FindEventsTests {
    @Test
    void findEventsReturns200AndResponseIsPopulated() throws Exception {
      var drivers =
          List.of(
              Driver.builder().driverId(44).fullName("Lewis Hamilton").odds(3).build(),
              Driver.builder().driverId(33).fullName("Max Verstappen").odds(2).build());
      var event =
          Event.builder()
              .eventId(1001)
              .name("Italian Grand Prix")
              .sessionType("Race")
              .country("Italy")
              .dateTime(parse("2021-09-12T14:00:00Z"))
              .drivers(drivers)
              .build();
      when(eventService.findEvents(eq("Race"), eq(2021), eq("Italy"))).thenReturn(List.of(event));

      mvc.perform(
              get("/api/v1/events")
                  .param("sessionType", "Race")
                  .param("year", "2021")
                  .param("country", "Italy")
                  .accept(APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
          .andExpect(jsonPath("$", hasSize(1)))
          .andExpect(jsonPath("$[0].eventId").value(1001))
          .andExpect(jsonPath("$[0].name").value("Italian Grand Prix"))
          .andExpect(jsonPath("$[0].sessionType").value("Race"))
          .andExpect(jsonPath("$[0].country").value("Italy"))
          .andExpect(jsonPath("$[0].drivers", hasSize(2)))
          .andExpect(jsonPath("$[0].drivers[*].odds", everyItem(isIn(List.of(2, 3, 4)))));
    }

    @Test
    void findEventsReturns200AndResponseIsEmpty() throws Exception {
      when(eventService.findEvents(any(), any(), any())).thenReturn(List.of());

      mvc.perform(get("/api/v1/events"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
          .andExpect(jsonPath("$", hasSize(0)));
    }
  }

  @Nested
  class PublishOutcomeTests {
    @Test
    void publishOutcomeReturns200AndResponseIsPopulated() throws Exception {
      var eventId = 1001;
      var request = PublishOutcomeRequest.builder().eventId(eventId).build();
      var eventOutcome =
          EventOutcome.builder()
              .eventId(eventId)
              .winnerDriverId(33)
              .betsWon(10)
              .betsLost(5)
              .build();
      when(outcomeService.publishOutcome(eventId)).thenReturn(eventOutcome);

      mvc.perform(
              post("/api/v1/events/outcome")
                  .contentType(APPLICATION_JSON)
                  .content(om.writeValueAsBytes(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.eventId").value(eventOutcome.getEventId()))
          .andExpect(jsonPath("$.winnerDriverId").value(eventOutcome.getWinnerDriverId()))
          .andExpect(jsonPath("$.betsWon").value(eventOutcome.getBetsWon()))
          .andExpect(jsonPath("$.betsLost").value(eventOutcome.getBetsLost()));
    }

    @Test
    void shouldReturn400WhenPublishingOutcomeWithMissingEventId() throws Exception {
      var request = PublishOutcomeRequest.builder().build();

      mvc.perform(
              post("/api/v1/events/outcome")
                  .contentType(APPLICATION_JSON)
                  .content(om.writeValueAsBytes(request)))
          .andExpect(status().isBadRequest());
    }
  }
}
