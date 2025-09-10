package com.sg.f1bettingservice.rest.bet;

import static org.springframework.http.HttpStatus.CREATED;

import com.sg.f1bettingservice.core.BetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/bets")
public class BetController {

  private final BetService betService;

  @PostMapping("/place")
  @ResponseStatus(CREATED)
  public PlaceBetResponse placeBet(@Valid @RequestBody PlaceBetRequest placeBetRequest) {
    var betId =
        betService.placeBet(
            placeBetRequest.getUserId(),
            placeBetRequest.getEventId(),
            placeBetRequest.getDriverId(),
            placeBetRequest.getAmount());
    return PlaceBetResponse.builder().betId(betId).build();
  }
}
