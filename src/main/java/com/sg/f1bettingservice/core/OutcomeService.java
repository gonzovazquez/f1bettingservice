package com.sg.f1bettingservice.core;

import static java.math.BigDecimal.valueOf;

import com.sg.f1bettingservice.core.exception.DriverNotFoundException;
import com.sg.f1bettingservice.core.model.Bet;
import com.sg.f1bettingservice.core.model.EventOutcome;
import com.sg.f1bettingservice.persistence.OutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutcomeService {

  private final OutcomeRepository outcomeRepository;
  private final EventService eventService;
  private final BetService betService;
  private final UserService userService;

  public EventOutcome publishOutcome(Integer eventId) {
    var winnerDriver =
        eventService
            .findWinnerDriverByEventId(eventId)
            .orElseThrow(
                () -> new DriverNotFoundException("No winner driver found for event " + eventId));

    var bets = betService.findBetsByEventId(eventId);
    var winnerBets = 0;
    var looserBets = 0;

    for (Bet bet : bets) {
      if (bet.getDriverId().equals(winnerDriver.getDriverId())) {
        betService.markBetAsWinner(bet.getId());
        var earnings = bet.getAmount().multiply(valueOf(winnerDriver.getOdds()));
        userService.addToUserBalance(bet.getUserId(), earnings);
        winnerBets++;
      } else {
        betService.markBetAsLooser(bet.getId());
        looserBets++;
      }
    }

    var eventOutcome =
        EventOutcome.builder()
            .eventId(eventId)
            .winnerDriverId(winnerDriver.getDriverId())
            .betsWon(winnerBets)
            .betsLost(looserBets)
            .build();
    outcomeRepository.save(eventOutcome);
    return eventOutcome;
  }
}
