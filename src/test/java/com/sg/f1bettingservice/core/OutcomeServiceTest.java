package com.sg.f1bettingservice.core;

import static com.sg.f1bettingservice.core.model.BetStatus.PLACED;
import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sg.f1bettingservice.core.exception.EventNotFoundException;
import com.sg.f1bettingservice.core.model.Bet;
import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.EventOutcome;
import com.sg.f1bettingservice.core.model.User;
import com.sg.f1bettingservice.persistence.OutcomeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OutcomeServiceTest {

  OutcomeRepository outcomeRepository = mock(OutcomeRepository.class);
  EventService eventService = mock(EventService.class);
  BetService betService = mock(BetService.class);
  UserService userService = mock(UserService.class);
  OutcomeService outcomeService =
      new OutcomeService(outcomeRepository, eventService, betService, userService);

  @Test
  void shouldPropagateEventNotFoundExceptionIfEventDoesNotExistTest() {
    var eventId = 1;
    var expectedException = new EventNotFoundException(eventId);
    when(eventService.findWinnerDriverByEventId(eventId)).thenThrow(expectedException);

    assertThatExceptionOfType(EventNotFoundException.class)
        .isThrownBy(() -> outcomeService.publishOutcome(eventId))
        .withMessage(expectedException.getMessage());

    verifyNoInteractions(outcomeRepository);
  }

  @Test
  void shouldThrowDriverNotFoundExceptionWhenNoWinnerDriverIsFoundForTheGivenEventTest() {
    Integer eventId = 1;
    when(eventService.findWinnerDriverByEventId(eventId)).thenReturn(Optional.empty());

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> outcomeService.publishOutcome(eventId))
        .withMessage("No winner driver found for event " + eventId);
  }

  @Test
  void shouldSaveOutcomeButWithZeroBetsWhenNoBetsHaveBeenPlacedForTheGivenEventTest() {
    Integer eventId = 1, driverId = 14;
    var driver = Driver.builder().driverId(driverId).build();
    var expectedEventOutcome =
        EventOutcome.builder()
            .eventId(eventId)
            .winnerDriverId(driverId)
            .betsWon(0)
            .betsLost(0)
            .build();

    when(eventService.findWinnerDriverByEventId(eventId)).thenReturn(Optional.of(driver));
    when(betService.findBetsByEventId(eventId)).thenReturn(List.of());

    var eventOutcome = outcomeService.publishOutcome(eventId);
    assertThat(eventOutcome).isEqualTo(expectedEventOutcome);

    verify(betService, never()).markBetAsWinner(anyInt());
    verify(betService, never()).markBetAsLooser(anyInt());
    verify(outcomeRepository).save(eventOutcome);
  }

  @Test
  void shouldProcessBetsWhenThereAreBetsPlacedForTheGivenEventTest() {
    Integer eventId = 1, winnerDriverId = 14, nonWinnerDriverId = 33;
    var driver =
        Driver.builder().driverId(winnerDriverId).fullName("Fernando Alonso").odds(2).build();
    var bettingUser1 = User.builder().userId(1).balance(valueOf(100)).build();
    var bettingUser2 = User.builder().userId(2).balance(valueOf(100)).build();
    var bettingUser3 = User.builder().userId(3).balance(valueOf(100)).build();
    var winnerBet1 =
        Bet.builder()
            .id(1)
            .userId(bettingUser1.getUserId())
            .eventId(eventId)
            .amount(valueOf(10))
            .driverId(winnerDriverId)
            .status(PLACED)
            .build();
    var winnerBet2 =
        Bet.builder()
            .id(2)
            .userId(bettingUser2.getUserId())
            .eventId(eventId)
            .amount(valueOf(20))
            .driverId(winnerDriverId)
            .status(PLACED)
            .build();
    var looserBet =
        Bet.builder()
            .id(3)
            .userId(bettingUser3.getUserId())
            .eventId(eventId)
            .driverId(nonWinnerDriverId)
            .amount(valueOf(30))
            .status(PLACED)
            .build();

    when(eventService.findWinnerDriverByEventId(eventId)).thenReturn(Optional.of(driver));
    when(betService.findBetsByEventId(eventId))
        .thenReturn(List.of(winnerBet1, winnerBet2, looserBet));

    var expectedOutcome =
        EventOutcome.builder()
            .eventId(eventId)
            .winnerDriverId(winnerDriverId)
            .betsWon(2)
            .betsLost(1)
            .build();
    var eventOutcome = outcomeService.publishOutcome(eventId);
    assertThat(eventOutcome).isEqualTo(expectedOutcome);

    verify(betService).markBetAsWinner(winnerBet1.getId());
    verify(betService).markBetAsWinner(winnerBet2.getId());
    verify(betService).markBetAsLooser(looserBet.getId());

    var winnerOdds = BigDecimal.valueOf(driver.getOdds());
    var expectedWinningAmountUser1 = winnerBet1.getAmount().multiply(winnerOdds);
    verify(userService).addToUserBalance(bettingUser1.getUserId(), expectedWinningAmountUser1);

    var expectedWinningAmountUser2 = winnerBet2.getAmount().multiply(winnerOdds);
    verify(userService).addToUserBalance(bettingUser2.getUserId(), expectedWinningAmountUser2);

    verify(outcomeRepository).save(eventOutcome);
  }
}
