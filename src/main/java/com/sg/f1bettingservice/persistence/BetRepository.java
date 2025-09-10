package com.sg.f1bettingservice.persistence;

import com.sg.f1bettingservice.core.model.Bet;
import com.sg.f1bettingservice.core.model.BetStatus;
import java.math.BigDecimal;
import java.util.List;

public interface BetRepository {
  Bet save(
      Integer userId, Integer eventId, Integer driverId, BigDecimal amount, BetStatus betStatus);

  List<Bet> findByEventId(Integer eventId);

  void updateBetStatus(Integer betId, BetStatus expectedStatus);
}
