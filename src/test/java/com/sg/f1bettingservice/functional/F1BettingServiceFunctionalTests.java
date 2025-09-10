package com.sg.f1bettingservice.functional;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"f1data.openf1.base-url=http://localhost:9999/"})
public class F1BettingServiceFunctionalTests {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  static WireMockServer wireMockServer;

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(9999);
    wireMockServer.start();
  }

  @AfterAll
  static void stopWireMock() {
    wireMockServer.stop();
  }

  @Test
  void shouldPlaceABetAndSettleItSuccessfullyForAnExistingEventAndDriver() throws Exception {
    var eventId = 1001;
    var sessionType = "Race";
    var year = 2023;
    var country = "Spain";
    var winnerDriverId = 33;
    var looserDriverId = 44;
    var winnerUserId = 1;
    var looserUserId = 2;

    stubSessions(eventId, sessionType, year, country);
    stubDriversForEventId(eventId);
    stubWinnerDriverForEventId(eventId);

    List<Event> events = fetchEvents();
    assertEventContainsWinnerDriver(events, eventId, winnerDriverId);

    placeBet(winnerUserId, eventId, winnerDriverId, BigDecimal.TEN);
    placeBet(looserUserId, eventId, looserDriverId, BigDecimal.TEN);

    Integer expectedBetsWon = 1, expectedBetsLost = 1;
    publishOutcome(eventId, expectedBetsWon, expectedBetsLost, winnerDriverId);
  }

  private void publishOutcome(
      Integer eventId, Integer expectedBetsWon, Integer expectedBetsLost, Integer winnerDriverId)
      throws Exception {
    mvc.perform(
            post("/api/v1/events/outcome")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                  { "eventId":1001 }
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.betsWon").value(expectedBetsWon))
        .andExpect(jsonPath("$.betsLost").value(expectedBetsLost))
        .andExpect(jsonPath("$.winnerDriverId").value(winnerDriverId));
  }

  private void placeBet(Integer userId, Integer eventId, Integer driverId, BigDecimal amount)
      throws Exception {
    mvc.perform(
            post("/api/v1/bets/place")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                  { "userId":%s, "eventId":%s, "driverId":%s, "amount":%s }
                """
                        .formatted(userId, eventId, driverId, amount)))
        .andExpect(status().isCreated());
  }

  private void assertEventContainsWinnerDriver(
      List<Event> events, int eventId, int winnerDriverId) {
    assertThat(events).extracting(Event::getEventId).contains(eventId);
    assertThat(events)
        .filteredOn(e -> e.getEventId().equals(eventId))
        .singleElement()
        .extracting(Event::getDrivers, list(Driver.class))
        .extracting(Driver::getDriverId)
        .contains(winnerDriverId);
  }

  private List<Event> fetchEvents() throws Exception {
    MvcResult eventsResult =
        mvc.perform(
                get("/api/v1/events")
                    .param("sessionType", "Race")
                    .param("year", "2023")
                    .param("country", "Spain"))
            .andExpect(status().isOk())
            .andReturn();

    String json = eventsResult.getResponse().getContentAsString();
    return objectMapper.readValue(json, new TypeReference<List<Event>>() {});
  }

  private void stubWinnerDriverForEventId(Integer eventId) {
    wireMockServer.stubFor(
        WireMock.get(urlPathEqualTo("/session_result"))
            .withQueryParam("session_key", equalTo(eventId.toString()))
            .withQueryParam("position", equalTo("1"))
            .willReturn(
                okJson(
                    """
              [ { "driver_number": 33 } ]
            """)));
  }

  private void stubDriversForEventId(Integer eventId) {
    wireMockServer.stubFor(
        WireMock.get(urlPathEqualTo("/drivers"))
            .withQueryParam("session_key", equalTo(eventId.toString()))
            .willReturn(
                okJson(
                    """
              [
                { "driver_number": 33, "full_name": "Max Verstappen" },
                { "driver_number": 44, "full_name": "Lewis Hamilton" }
              ]
            """)));
  }

  private void stubSessions(Integer eventId, String sessionType, Integer year, String country) {
    wireMockServer.stubFor(
        WireMock.get(urlPathEqualTo("/sessions"))
            .willReturn(
                okJson(
                    """
              [{
                "session_key": "%s",
                "meeting_key": 200,
                "session_name": "%s",
                "session_type": "%s",
                "country_name": "%s",
                "date_start": "2023-05-01T10:00:00+00:00",
                "year": 2023
              }]
            """
                        .formatted(eventId, sessionType, year, country))));
  }
}
