package com.sg.f1bettingservice.f1data.openf1;

import static java.time.OffsetDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.MockRestServiceServer.createServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.f1data.F1DataProviderException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class OpenF1DataProviderTest {

  private static final String BASE_URL = "https://test.openF1.com/";

  RestTemplate restTemplate;
  MockRestServiceServer mockServer;
  OpenF1DataMapper openF1DataMapper;
  OpenF1DataProvider openF1DataProvider;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    mockServer = createServer(restTemplate);
    openF1DataMapper = mock(OpenF1DataMapper.class);
    openF1DataProvider = new OpenF1DataProvider(openF1DataMapper, restTemplate, BASE_URL);
  }

  @Nested
  class FindEventsTests {
    @Test
    void shouldReturnListOfMappedEventsWhenSessionTypeAndYearAndCountryAreGiven() {
      var sessionType = "Race";
      var year = 2023;
      var country = "Belgium";

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("sessions")
              .queryParam("session_type", sessionType)
              .queryParam("year", year)
              .queryParam("country_name", country)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(getSessionJsonResponse(), APPLICATION_JSON));

      var events = List.of(Event.builder().build());
      when(openF1DataMapper.toEventList(anyList())).thenReturn(events);

      var result = openF1DataProvider.findEvents(sessionType, year, country);
      assertThat(result).isEqualTo(events);

      ArgumentCaptor<List<OpenF1Session>> sessionsResponseCaptor = forClass(List.class);
      verify(openF1DataMapper).toEventList(sessionsResponseCaptor.capture());

      var sessionsPassed = sessionsResponseCaptor.getValue();
      assertThat(sessionsPassed).hasSize(1);
      var session = sessionsPassed.getFirst();
      assertThat(session.session_key()).isEqualTo(9140);
      assertThat(session.session_type()).isEqualTo(sessionType);
      assertThat(session.country_name()).isEqualTo(country);
      assertThat(session.year()).isEqualTo(year);
      assertThat(session.date_start()).isEqualTo("2023-07-29T15:05:00+00:00");
    }

    @Test
    void shouldIncludeOnlySessionTypeInRequestWhenOnlySessionTypeIsGiven() {
      var sessionType = "Race";

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("sessions")
              .queryParam("session_type", sessionType)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(getSessionJsonResponse(), APPLICATION_JSON));

      var events = List.of(Event.builder().build());
      when(openF1DataMapper.toEventList(anyList())).thenReturn(events);

      var result = openF1DataProvider.findEvents(sessionType, null, null);
      assertThat(result).isEqualTo(events);
    }

    @Test
    void shouldIncludeOnlyYearInRequestWhenOnlyYearIsGiven() {
      var year = 2023;

      var expectedUri =
          fromHttpUrl(BASE_URL).path("sessions").queryParam("year", year).build(true).toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(getSessionJsonResponse(), APPLICATION_JSON));

      var events = List.of(Event.builder().build());
      when(openF1DataMapper.toEventList(anyList())).thenReturn(events);

      var result = openF1DataProvider.findEvents(null, year, null);
      assertThat(result).isEqualTo(events);
    }

    @Test
    void shouldIncludeOnlyCountryNameInRequestWhenOnlyCountryNameIsGiven() {
      var country = "Belgium";

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("sessions")
              .queryParam("country_name", country)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(getSessionJsonResponse(), APPLICATION_JSON));

      var events = List.of(Event.builder().build());
      when(openF1DataMapper.toEventList(anyList())).thenReturn(events);

      var result = openF1DataProvider.findEvents(null, null, country);
      assertThat(result).isEqualTo(events);
    }

    @Test
    void shouldNotIncludeAnyParametersInRequestWhenNoneOfThemAreGiven() {
      var expectedUri = fromHttpUrl(BASE_URL).path("sessions").build(true).toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(getSessionJsonResponse(), APPLICATION_JSON));

      var events = List.of(Event.builder().build());
      when(openF1DataMapper.toEventList(anyList())).thenReturn(events);

      var result = openF1DataProvider.findEvents(null, null, null);
      assertThat(result).isEqualTo(events);
    }

    @Test
    void shouldThrowF1DataProviderExceptionWhenSomethingGoesWrong() {
      var expectedUri = fromHttpUrl(BASE_URL).path("sessions").build(true).toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withServerError());

      assertThatExceptionOfType(F1DataProviderException.class)
          .isThrownBy(() -> openF1DataProvider.findEvents(null, null, null))
          .withMessageContaining("Failed to fetch sessions from OpenF1 API");
    }
  }

  @Nested
  class FindEventByIdTests {
    @Test
    void shouldReturnMappedEventWhenSessionExists() {
      int eventId = 1216;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("sessions")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(getSessionJsonResponse(), APPLICATION_JSON));

      var mappedEvent =
          Event.builder()
              .eventId(eventId)
              .name("Belgian Grand Prix")
              .sessionType("Race")
              .country("Belgium")
              .dateTime(parse("2023-07-29T15:05:00+00:00"))
              .drivers(List.of())
              .build();
      var events = List.of(mappedEvent);
      when(openF1DataMapper.toEventList(anyList())).thenReturn(events);

      var result = openF1DataProvider.findEventById(eventId);
      assertThat(result).isEqualTo(Optional.of(mappedEvent));

      ArgumentCaptor<List<OpenF1Session>> sessionsResponseCaptor = forClass(List.class);
      verify(openF1DataMapper).toEventList(sessionsResponseCaptor.capture());

      var sessionsPassed = sessionsResponseCaptor.getValue();
      assertThat(sessionsPassed).hasSize(1);
      var session = sessionsPassed.getFirst();
      assertThat(session.session_key()).isEqualTo(9140);
      assertThat(session.session_type()).isEqualTo("Race");
      assertThat(session.country_name()).isEqualTo("Belgium");
      assertThat(session.year()).isEqualTo(2023);
      assertThat(session.date_start()).isEqualTo("2023-07-29T15:05:00+00:00");
    }

    @Test
    void shouldReturnEmptyWhenSessionNotFound() {
      int eventId = 9999;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("sessions")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess("[]", APPLICATION_JSON));

      when(openF1DataMapper.toEventList(List.of())).thenReturn(List.of());

      var result = openF1DataProvider.findEventById(eventId);

      assertThat(result).isEmpty();
      mockServer.verify();
    }

    @Test
    void shouldThrowProviderExceptionOnServerError() {
      int eventId = 9140;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("sessions")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withServerError()); // 500

      assertThatThrownBy(() -> openF1DataProvider.findEventById(eventId))
          .isInstanceOf(F1DataProviderException.class)
          .hasMessageContaining("Failed to fetch session by id");

      mockServer.verify();
    }
  }

  @Nested
  class GetDriversByEventIdTests {
    @Test
    void shouldReturnMappedDriversWhenEventIdIsGiven() {
      Integer eventId = 1216;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("drivers")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      String driversJson = getDriversJsonResponse();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(driversJson, APPLICATION_JSON));

      var mapped =
          List.of(
              Driver.builder().driverId(14).fullName("Fernando Alonso").build(),
              Driver.builder().driverId(1).fullName("Max Verstappen").build());

      ArgumentCaptor<List<OpenF1Driver>> captor = forClass(List.class);
      when(openF1DataMapper.toDriverList(captor.capture())).thenReturn(mapped);

      var result = openF1DataProvider.getDriversByEventId(eventId);

      assertThat(result).isEqualTo(mapped);

      var passed = captor.getValue();
      assertThat(passed)
          .filteredOn(d -> d.driver_number().equals(14) || d.driver_number().equals(1))
          .hasSize(2)
          .extracting(OpenF1Driver::full_name)
          .containsExactlyInAnyOrder("Fernando Alonso", "Max Verstappen");

      mockServer.verify();
    }

    @Test
    void shouldReturnEmptyListWhenApiReturnsEmptyArray() {
      Integer eventId = 9999;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("drivers")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess("[]", APPLICATION_JSON));

      when(openF1DataMapper.toDriverList(List.of())).thenReturn(List.of());

      var result = openF1DataProvider.getDriversByEventId(eventId);

      assertThat(result).isEmpty();
      verify(openF1DataMapper).toDriverList(List.of());

      mockServer.verify();
    }

    @Test
    void shouldThrowWhenF1DataProviderExceptionWhenEventIdIsNull() {
      assertThatExceptionOfType(F1DataProviderException.class)
          .isThrownBy(() -> openF1DataProvider.getDriversByEventId(null))
          .withMessage("Event ID must be provided");
    }

    @Test
    void shouldThrowF1DataProviderExceptionWhenSomethingGoesWrong() {
      var eventId = 1;
      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("drivers")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withServerError());

      assertThatExceptionOfType(F1DataProviderException.class)
          .isThrownBy(() -> openF1DataProvider.getDriversByEventId(eventId));
    }
  }

  @Nested
  class GetWinnerDriverIdByEventIdTests {

    @Test
    void shouldReturnWinnerDriverIdFromOpenF1API() {
      var eventId = 1;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("session_result")
              .queryParam("session_key", eventId)
              .queryParam("position", 1)
              .build(true)
              .toUri();

      var winnerDriverJson = getWinnerDriverJsonResponse();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess(winnerDriverJson, APPLICATION_JSON));

      var mapped = List.of(Driver.builder().driverId(14).build());

      ArgumentCaptor<List<OpenF1Driver>> captor = forClass(List.class);
      when(openF1DataMapper.toDriverList(captor.capture())).thenReturn(mapped);

      var result = openF1DataProvider.getWinnerDriverIdByEventId(eventId);

      var expected = Optional.of(mapped.getFirst().getDriverId());
      assertThat(result).isEqualTo(expected);

      var passed = captor.getValue();
      assertThat(passed)
          .hasSize(1)
          .extracting(OpenF1Driver::driver_number)
          .containsExactlyInAnyOrder(14);

      mockServer.verify();
    }

    @Test
    void shouldReturnEmptyOptionalWinnerDriverIdWhenApiReturnsEmptyArray() {
      Integer eventId = 9999;

      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("session_result")
              .queryParam("session_key", eventId)
              .queryParam("position", 1)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withSuccess("[]", APPLICATION_JSON));

      when(openF1DataMapper.toDriverList(List.of())).thenReturn(List.of());

      var result = openF1DataProvider.getWinnerDriverIdByEventId(eventId);

      assertThat(result).isEmpty();
      verify(openF1DataMapper).toDriverList(List.of());

      mockServer.verify();
    }

    @Test
    void shouldThrowF1DataProviderExceptionWhenSomethingGoesWrong() {
      var eventId = 1;
      var expectedUri =
          fromHttpUrl(BASE_URL)
              .path("session_result")
              .queryParam("session_key", eventId)
              .queryParam("position", 1)
              .build(true)
              .toUri();

      mockServer
          .expect(requestTo(expectedUri))
          .andExpect(method(GET))
          .andRespond(withServerError());

      assertThatExceptionOfType(F1DataProviderException.class)
          .isThrownBy(() -> openF1DataProvider.getWinnerDriverIdByEventId(eventId))
          .withMessageContaining("Failed to fetch winner driver by id from OpenF1 API");
    }
  }

  private String getDriversJsonResponse() {
    return """
            [
               {
                           "meeting_key": 1219,
                           "session_key": 9158,
                           "driver_number": 1,
                           "broadcast_name": "M VERSTAPPEN",
                           "full_name": "Max Verstappen",
                           "name_acronym": "VER",
                           "team_name": "Red Bull Racing",
                           "team_colour": "3671C6",
                           "first_name": "Max",
                           "last_name": "Verstappen",
                           "headshot_url": "https://www.formula1.com/content/dam/fom-website/drivers/M/MAXVER01_Max_Verstappen/maxver01.png.transform/1col/image.png",
                           "country_code": "NED"
                         },
               {
                           "meeting_key": 1219,
                           "session_key": 1216,
                           "driver_number": 14,
                           "broadcast_name": "F ALONSO",
                           "full_name": "Fernando Alonso",
                           "name_acronym": "ALO",
                           "team_name": "Aston Martin",
                           "team_colour": "358C75",
                           "first_name": "Fernando",
                           "last_name": "Alonso",
                           "headshot_url": "https://www.formula1.com/content/dam/fom-website/drivers/F/FERALO01_Fernando_Alonso/feralo01.png.transform/1col/image.png",
                           "country_code": "ESP"
                         }
             ]
    """;
  }

  private String getSessionJsonResponse() {
    return "[\n"
        + "  {\n"
        + "    \"circuit_key\": 7,\n"
        + "    \"circuit_short_name\": \"Spa-Francorchamps\",\n"
        + "    \"country_code\": \"BEL\",\n"
        + "    \"country_key\": 16,\n"
        + "    \"country_name\": \"Belgium\",\n"
        + "    \"date_end\": \"2023-07-29T15:35:00+00:00\",\n"
        + "    \"date_start\": \"2023-07-29T15:05:00+00:00\",\n"
        + "    \"gmt_offset\": \"02:00:00\",\n"
        + "    \"location\": \"Spa-Francorchamps\",\n"
        + "    \"session_key\": 1216,\n"
        + "    \"session_key\": 9140,\n"
        + "    \"session_name\": \"Sprint\",\n"
        + "    \"session_type\": \"Race\",\n"
        + "    \"year\": 2023\n"
        + "  }\n"
        + "]";
  }

  private String getWinnerDriverJsonResponse() {
    return ""
        + "[\n"
        + "  {\n"
        + "    \"position\": 1,\n"
        + "    \"driver_number\": 14,\n"
        + "    \"number_of_laps\": 24,\n"
        + "    \"dnf\": false,\n"
        + "    \"dns\": false,\n"
        + "    \"dsq\": false,\n"
        + "    \"duration\": 77.565,\n"
        + "    \"gap_to_leader\": 0,\n"
        + "    \"meeting_key\": 1143,\n"
        + "    \"session_key\": 7782\n"
        + "  }\n"
        + "]";
  }
}
