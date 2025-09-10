package com.sg.f1bettingservice.f1data.openf1;

import static java.util.Optional.ofNullable;
import static org.springframework.http.RequestEntity.get;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.f1data.F1DataProvider;
import com.sg.f1bettingservice.f1data.F1DataProviderException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class OpenF1DataProvider implements F1DataProvider {

  private final OpenF1DataMapper openF1DataMapper;
  private final RestTemplate restTemplate;
  private final String baseUrl;

  public OpenF1DataProvider(
      OpenF1DataMapper openF1DataMapper,
      RestTemplate restTemplate,
      @Value("${f1data.openf1.base-url}") String baseUrl) {
    this.openF1DataMapper = openF1DataMapper;
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public List<Event> findEvents(String sessionType, Integer year, String country)
      throws F1DataProviderException {
    try {
      var uri =
          fromHttpUrl(baseUrl)
              .path("sessions")
              .queryParamIfPresent("session_type", ofNullable(sessionType))
              .queryParamIfPresent("year", ofNullable(year))
              .queryParamIfPresent("country_name", ofNullable(country))
              .build(true)
              .toUri();

      log.info("Making OpenF1 request to fetch sessions: {}", uri);
      var type = new ParameterizedTypeReference<List<OpenF1Session>>() {};
      var sessions =
          ofNullable(restTemplate.exchange(get(uri).build(), type).getBody()).orElseGet(List::of);

      return openF1DataMapper.toEventList(sessions);

    } catch (Exception e) {
      log.error("Error fetching events from OpenF1 API", e);
      throw new F1DataProviderException("Failed to fetch sessions from OpenF1 API", e);
    }
  }

  @Override
  public List<Driver> getDriversByEventId(Integer eventId) {

    var notNullableEventId =
        ofNullable(eventId)
            .orElseThrow(() -> new F1DataProviderException("Event ID must be provided"));

    try {
      var uri =
          fromHttpUrl(baseUrl)
              .path("drivers")
              .queryParamIfPresent("session_key", ofNullable(notNullableEventId))
              .build(true)
              .toUri();

      log.info("Making OpenF1 request to fetch drivers: {}", uri);
      var type = new ParameterizedTypeReference<List<OpenF1Driver>>() {};
      var drivers =
          ofNullable(restTemplate.exchange(get(uri).build(), type).getBody()).orElseGet(List::of);

      return openF1DataMapper.toDriverList(drivers);

    } catch (Exception e) {
      log.error("Error fetching drivers from OpenF1 API", e);
      throw new F1DataProviderException("Failed to fetch drivers from OpenF1", e);
    }
  }

  @Override
  public Optional<Event> findEventById(int eventId) {
    try {
      var uri =
          fromHttpUrl(baseUrl)
              .path("sessions")
              .queryParam("session_key", eventId)
              .build(true)
              .toUri();

      log.info("Making OpenF1 request to fetch sessions by its ID: {}", uri);
      var type = new ParameterizedTypeReference<List<OpenF1Session>>() {};
      var sessions =
          ofNullable(restTemplate.exchange(get(uri).build(), type).getBody()).orElseGet(List::of);

      var mapped = openF1DataMapper.toEventList(sessions);
      return mapped.stream().findFirst();

    } catch (Exception e) {
      log.error("Error fetching events by ID from OpenF1 API", e);
      throw new F1DataProviderException("Failed to fetch session by id from OpenF1 API", e);
    }
  }

  @Override
  public Optional<Integer> getWinnerDriverIdByEventId(Integer eventId) {
    try {
      var uri =
          fromHttpUrl(baseUrl)
              .path("session_result")
              .queryParam("session_key", eventId)
              .queryParam("position", 1)
              .build(true)
              .toUri();

      log.info("Making OpenF1 request to fetch session_results: {}", uri);
      var type = new ParameterizedTypeReference<List<OpenF1Driver>>() {};
      var openF1Drivers =
          ofNullable(restTemplate.exchange(get(uri).build(), type).getBody()).orElseGet(List::of);

      var mapped = openF1DataMapper.toDriverList(openF1Drivers);
      return mapped.stream().map(Driver::getDriverId).findFirst();

    } catch (Exception e) {
      log.error("Error fetching session results from OpenF1 API", e);
      throw new F1DataProviderException("Failed to fetch winner driver by id from OpenF1 API", e);
    }
  }
}
