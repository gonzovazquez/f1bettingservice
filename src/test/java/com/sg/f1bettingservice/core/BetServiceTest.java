package com.sg.f1bettingservice.core;

import static com.sg.f1bettingservice.core.model.BetStatus.WON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sg.f1bettingservice.core.exception.DriverNotFoundException;
import com.sg.f1bettingservice.core.exception.EventNotFoundException;
import com.sg.f1bettingservice.core.exception.InsufficientBalanceException;
import com.sg.f1bettingservice.core.model.Bet;
import com.sg.f1bettingservice.core.model.BetStatus;
import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.core.model.User;
import com.sg.f1bettingservice.persistence.BetRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BetServiceTest {

  EventService eventService = mock(EventService.class);
  UserService userService = mock(UserService.class);
  BetRepository betRepository = mock(BetRepository.class);
  BetService betService = new BetService(eventService, betRepository, userService);

  @Nested
  class PlaceBetTests {
    @Test
    void shouldPlaceABetWhenForUserAndDriverAndEventWhenItExists() {
      Integer userId = 1, eventId = 1001, driverId = 33;
      var amount = BigDecimal.TEN;
      var betStatus = BetStatus.PLACED;
      var existingUser = User.builder().userId(userId).balance(BigDecimal.valueOf(50)).build();
      var existingEvent =
          Event.builder()
              .eventId(eventId)
              .drivers(List.of(Driver.builder().driverId(driverId).build()))
              .build();
      var expectedBet =
          Bet.builder()
              .id(1)
              .userId(userId)
              .eventId(eventId)
              .driverId(driverId)
              .amount(amount)
              .status(betStatus)
              .build();

      when(eventService.findByEventId(eventId)).thenReturn(Optional.of(existingEvent));
      when(betRepository.save(userId, eventId, driverId, amount, betStatus))
          .thenReturn(expectedBet);
      when(userService.getOrCreate(userId)).thenReturn(existingUser);

      var result = betService.placeBet(userId, eventId, driverId, amount);

      assertThat(result).isEqualTo(expectedBet.getId());

      verify(userService).subtractFromUserBalance(userId, amount);
      verify(betRepository).save(userId, eventId, driverId, amount, betStatus);
      verify(userService).getOrCreate(userId);
    }

    @Test
    void shouldThrowEventNotFoundExceptionWhenEventDoesNotExist() {
      Integer userId = 1, eventId = 999, driverId = 33;
      var amount = BigDecimal.TEN;

      when(eventService.findByEventId(eventId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> betService.placeBet(userId, eventId, driverId, amount))
          .isInstanceOf(EventNotFoundException.class)
          .hasMessageContaining("Event with id %s not found".formatted(eventId));

      verifyNoInteractions(userService);
      verifyNoInteractions(betRepository);
    }

    @Test
    void shouldThrowDriverNotFoundExceptionWhenDriverDoesNotExistForAGivenEvent() {
      Integer userId = 1, eventId = 1001, driverId = 999;
      var amount = BigDecimal.TEN;

      when(eventService.findByEventId(eventId))
          .thenReturn(
              Optional.of(
                  Event.builder().eventId(eventId).drivers(Collections.emptyList()).build()));

      assertThatThrownBy(() -> betService.placeBet(userId, eventId, driverId, amount))
          .isInstanceOf(DriverNotFoundException.class)
          .hasMessageContaining(
              "Driver with id %s not found in event with id %s".formatted(driverId, eventId));

      verifyNoInteractions(userService);
      verifyNoInteractions(betRepository);
    }

    @Test
    void shouldPropagateExceptionWhenInsufficientBalance() {
      Integer userId = 1, eventId = 1001, driverId = 33;
      var amount = BigDecimal.valueOf(150);
      var existingUser = User.builder().userId(userId).balance(BigDecimal.valueOf(50)).build();
      var event =
          Event.builder()
              .eventId(eventId)
              .drivers(List.of(Driver.builder().driverId(driverId).build()))
              .build();
      var expectedException = new InsufficientBalanceException(userId);

      when(eventService.findByEventId(eventId)).thenReturn(Optional.of(event));
      when(userService.getOrCreate(userId)).thenReturn(existingUser);
      doThrow(expectedException).when(userService).subtractFromUserBalance(userId, amount);

      assertThatException()
          .isThrownBy(() -> betService.placeBet(userId, eventId, driverId, amount))
          .isEqualTo(expectedException);

      verify(userService).subtractFromUserBalance(userId, amount);
      verifyNoInteractions(betRepository);
    }
  }

  @Nested
  class FindBetsByEventIdTests {
    @Test
    void shouldReturnListOfBetsByEventIdFromRepository() {
      var eventId = 1;
      var expectedBets =
          List.of(
              Bet.builder()
                  .id(1)
                  .eventId(eventId)
                  .userId(1)
                  .driverId(14)
                  .amount(BigDecimal.TEN)
                  .status(BetStatus.PLACED)
                  .build(),
              Bet.builder()
                  .id(2)
                  .eventId(eventId)
                  .userId(2)
                  .driverId(33)
                  .amount(BigDecimal.valueOf(20))
                  .status(BetStatus.PLACED)
                  .build());

      when(betRepository.findByEventId(eventId)).thenReturn(expectedBets);
      var result = betService.findBetsByEventId(eventId);
      assertThat(result).isEqualTo(expectedBets);

      verify(betRepository).findByEventId(eventId);
    }

    @Test
    void returnEmptyBetListWhenNoBetsFoundForEventId() {
      var eventId = 1;
      when(betRepository.findByEventId(eventId)).thenReturn(Collections.emptyList());
      var result = betService.findBetsByEventId(eventId);
      assertThat(result).isEmpty();

      verify(betRepository).findByEventId(eventId);
    }
  }

  @Nested
  class UpdateBetStatusTests {
    @Test
    void shouldMarkBetAsWinnerTest() {
      var betId = 1;
      var expectedStatus = WON;
      betService.markBetAsWinner(betId);
      verify(betRepository).updateBetStatus(betId, expectedStatus);
    }

    @Test
    void shouldMarkBetAsLooserTest() {
      var betId = 1;
      var expectedStatus = BetStatus.LOST;
      betService.markBetAsLooser(betId);
      verify(betRepository).updateBetStatus(betId, expectedStatus);
    }
  }
}
