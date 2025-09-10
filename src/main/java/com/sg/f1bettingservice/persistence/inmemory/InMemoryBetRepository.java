package com.sg.f1bettingservice.persistence.inmemory;

import static java.util.Optional.ofNullable;

import com.sg.f1bettingservice.core.model.Bet;
import com.sg.f1bettingservice.core.model.BetStatus;
import com.sg.f1bettingservice.persistence.BetRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryBetRepository implements BetRepository {

  private final AtomicInteger sequence;
  private final Map<Integer, Bet> bets;

  public InMemoryBetRepository() {
    this.sequence = new AtomicInteger(1);
    this.bets = new ConcurrentHashMap<>();
  }

  @Override
  public Bet save(
      Integer userId, Integer eventId, Integer driverId, BigDecimal amount, BetStatus betStatus) {
    Integer id = sequence.getAndIncrement();
    Bet bet =
        Bet.builder()
            .id(id)
            .userId(userId)
            .eventId(eventId)
            .driverId(driverId)
            .amount(amount)
            .status(betStatus)
            .build();

    bets.put(id, bet);
    return bet;
  }

  @Override
  public List<Bet> findByEventId(Integer eventId) {
    return bets.values().stream().filter(b -> b.getEventId().equals(eventId)).toList();
  }

  @Override
  public void updateBetStatus(Integer betId, BetStatus betStatus) {
    ofNullable(bets.get(betId)).ifPresent(bet -> bet.setStatus(betStatus));
  }
}
