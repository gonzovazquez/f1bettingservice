package com.sg.f1bettingservice.rest.bet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sg.f1bettingservice.core.BetService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BetControllerTest {

  BetService betService = mock(BetService.class);
  BetController betController = new BetController(betService);

  @Test
  void shouldReturnPlacedBetTest() {
    var placeBetRequest =
        PlaceBetRequest.builder().userId(1).eventId(1).driverId(1).amount(BigDecimal.TEN).build();
    var expectedBetId = 1;
    var expectedBetResponse = PlaceBetResponse.builder().betId(expectedBetId).build();
    when(betService.placeBet(
            placeBetRequest.getUserId(),
            placeBetRequest.getEventId(),
            placeBetRequest.getDriverId(),
            placeBetRequest.getAmount()))
        .thenReturn(expectedBetId);

    var response = betController.placeBet(placeBetRequest);

    assertThat(response).isEqualTo(expectedBetResponse);
    verify(betService)
        .placeBet(
            placeBetRequest.getUserId(),
            placeBetRequest.getEventId(),
            placeBetRequest.getDriverId(),
            placeBetRequest.getAmount());
  }
}
