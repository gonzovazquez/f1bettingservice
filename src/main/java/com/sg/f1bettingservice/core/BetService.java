package com.sg.f1bettingservice.core;

import static com.sg.f1bettingservice.core.model.BetStatus.LOST;
import static com.sg.f1bettingservice.core.model.BetStatus.PLACED;
import static com.sg.f1bettingservice.core.model.BetStatus.WON;

import com.sg.f1bettingservice.core.exception.DriverNotFoundException;
import com.sg.f1bettingservice.core.exception.EventNotFoundException;
import com.sg.f1bettingservice.core.model.Bet;
import com.sg.f1bettingservice.persistence.BetRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BetService {

  private final EventService eventService;
  private final BetRepository betRepository;
  private final UserService userService;

  public Integer placeBet(Integer userId, Integer eventId, Integer driverId, BigDecimal amount) {
    validateEventAndDriver(eventId, driverId);
    updateUserBalance(userId, amount);
    var bet = betRepository.save(userId, eventId, driverId, amount, PLACED);
    return bet.getId();
  }

  public List<Bet> findBetsByEventId(Integer eventId) {
    return betRepository.findByEventId(eventId);
  }

  public void markBetAsWinner(Integer betId) {
    betRepository.updateBetStatus(betId, WON);
  }

  public void markBetAsLooser(Integer betId) {
    betRepository.updateBetStatus(betId, LOST);
  }

  private void updateUserBalance(Integer userId, BigDecimal amount) {
    var user = userService.getOrCreate(userId);
    userService.subtractFromUserBalance(user.getUserId(), amount);
  }

  private void validateEventAndDriver(Integer eventId, Integer driverId) {
    var event =
        eventService.findByEventId(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

    var driverExists =
        event.getDrivers().stream().anyMatch(d -> Objects.equals(d.getDriverId(), driverId));

    if (!driverExists) {
      throw new DriverNotFoundException(driverId, eventId);
    }
  }
}
