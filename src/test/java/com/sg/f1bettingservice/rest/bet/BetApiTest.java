package com.sg.f1bettingservice.rest.bet;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sg.f1bettingservice.core.BetService;
import com.sg.f1bettingservice.core.exception.DriverNotFoundException;
import com.sg.f1bettingservice.core.exception.EventNotFoundException;
import com.sg.f1bettingservice.core.exception.InsufficientBalanceException;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = BetController.class)
public class BetApiTest {

  @Autowired MockMvc mvc;

  @Autowired ObjectMapper om;

  @MockitoBean BetService betService;

  @Test
  void shouldReturnHttp201CodeWhenBetHasBeenPlacedSuccessfully() throws Exception {
    var request =
        PlaceBetRequest.builder()
            .userId(1)
            .eventId(1001)
            .driverId(33)
            .amount(BigDecimal.TEN)
            .build();

    var expected = PlaceBetResponse.builder().betId(7).build();

    when(betService.placeBet(
            request.getUserId(), request.getEventId(), request.getDriverId(), request.getAmount()))
        .thenReturn(expected.getBetId());

    mvc.perform(
            post("/api/v1/bets/place")
                .contentType(APPLICATION_JSON)
                .content(om.writeValueAsBytes(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.betId").value(7));
  }

  @Test
  void shouldReturn404WhenPlacingABetForNonExistingEvent() throws Exception {
    var request =
        PlaceBetRequest.builder()
            .userId(1)
            .eventId(9999)
            .driverId(33)
            .amount(BigDecimal.TEN)
            .build();

    when(betService.placeBet(
            request.getUserId(), request.getEventId(), request.getDriverId(), request.getAmount()))
        .thenThrow(new EventNotFoundException(request.getEventId()));

    mvc.perform(
            post("/api/v1/bets/place")
                .contentType(APPLICATION_JSON)
                .content(om.writeValueAsBytes(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404NotFoundWhenPlacingABetForNonExistingDriver() throws Exception {
    var request =
        PlaceBetRequest.builder()
            .userId(1)
            .eventId(1001)
            .driverId(999)
            .amount(BigDecimal.TEN)
            .build();

    when(betService.placeBet(
            request.getUserId(), request.getEventId(), request.getDriverId(), request.getAmount()))
        .thenThrow(new DriverNotFoundException(request.getEventId(), request.getDriverId()));

    mvc.perform(
            post("/api/v1/bets/place")
                .contentType(APPLICATION_JSON)
                .content(om.writeValueAsBytes(request)))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn409ConflictWhenUserHasInsufficientBalance() throws Exception {
    var request =
        PlaceBetRequest.builder()
            .userId(1)
            .eventId(1001)
            .driverId(33)
            .amount(BigDecimal.TEN)
            .build();

    when(betService.placeBet(
            request.getUserId(), request.getEventId(), request.getDriverId(), request.getAmount()))
        .thenThrow(new InsufficientBalanceException(1));

    mvc.perform(
            post("/api/v1/bets/place")
                .contentType(APPLICATION_JSON)
                .content(om.writeValueAsBytes(request)))
        .andExpect(status().isConflict());
  }

  @ParameterizedTest
  @MethodSource("getInvalidRequests")
  void shouldReturn400WhenPlacingInvalidBet(PlaceBetRequest request) throws Exception {
    mvc.perform(
            post("/api/v1/bets/place")
                .contentType(APPLICATION_JSON)
                .content(om.writeValueAsBytes(request)))
        .andExpect(status().isBadRequest());
  }

  private static Stream<Arguments> getInvalidRequests() {
    return Stream.of(
        Arguments.of(PlaceBetRequest.builder().build()),
        Arguments.of(PlaceBetRequest.builder().userId(1).build()),
        Arguments.of(PlaceBetRequest.builder().userId(1).eventId(1).build()),
        Arguments.of(PlaceBetRequest.builder().userId(1).eventId(1).driverId(1).build()),
        Arguments.of(
            PlaceBetRequest.builder()
                .userId(1)
                .eventId(1)
                .driverId(1)
                .amount(BigDecimal.ZERO)
                .build()));
  }
}
