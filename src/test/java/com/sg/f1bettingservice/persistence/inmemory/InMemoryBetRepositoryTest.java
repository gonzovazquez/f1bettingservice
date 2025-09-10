package com.sg.f1bettingservice.persistence.inmemory;

import static com.sg.f1bettingservice.core.model.BetStatus.PLACED;
import static com.sg.f1bettingservice.core.model.BetStatus.WON;
import static org.assertj.core.api.Assertions.assertThat;

import com.sg.f1bettingservice.core.model.Bet;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class InMemoryBetRepositoryTest {

  InMemoryBetRepository betRepository = new InMemoryBetRepository();

  @Test
  void shouldSaveNewBetAndIncrementItsIdTest() {
    var b1 = betRepository.save(1, 1001, 33, BigDecimal.TEN, PLACED);
    var b2 = betRepository.save(1, 1002, 44, BigDecimal.valueOf(5), PLACED);

    assertThat(b1.getId()).isEqualTo(1);
    assertThat(b2.getId()).isEqualTo(2);
  }

  @Test
  void shouldReturnBetsFilteredByEventIdTest() {
    var b1 = betRepository.save(1, 1001, 33, BigDecimal.TEN, PLACED);
    var b2 = betRepository.save(1, 1002, 44, BigDecimal.valueOf(5), PLACED);
    var b3 = betRepository.save(2, 1001, 55, BigDecimal.valueOf(20), PLACED);

    var betsForEvent1001 = betRepository.findByEventId(1001);
    var betsForEvent1002 = betRepository.findByEventId(1002);

    assertThat(betsForEvent1001).containsExactlyInAnyOrder(b1, b3);
    assertThat(betsForEvent1002).containsExactly(b2);
  }

  @Test
  void shouldReturnEmptyListWhenNoBetsForGivenEventIdTest() {
    var betsForEvent9999 = betRepository.findByEventId(9999);
    assertThat(betsForEvent9999).isEmpty();
  }

  @Test
  void shouldUpdateBetStatusByBetIdTest() {
    var b1 = betRepository.save(1, 1001, 33, BigDecimal.TEN, PLACED);
    var b2 = betRepository.save(1, 1001, 33, BigDecimal.ONE, PLACED);

    betRepository.updateBetStatus(b1.getId(), WON);

    var betsForEvent1001 = betRepository.findByEventId(1001);
    assertThat(betsForEvent1001)
        .extracting(Bet::getId)
        .containsExactlyInAnyOrder(b1.getId(), b2.getId());
    assertThat(betsForEvent1001)
        .filteredOn(bet -> bet.getId().equals(b1.getId()))
        .extracting(Bet::getStatus)
        .containsExactly(WON);
    assertThat(betsForEvent1001)
        .filteredOn(bet -> bet.getId().equals(b2.getId()))
        .extracting(Bet::getStatus)
        .containsExactly(PLACED);
  }

  @Test
  void shouldNotUpdateBetStatusIfNoBetFoundById() {
    var b1 = betRepository.save(1, 1001, 33, BigDecimal.TEN, PLACED);
    var b2 = betRepository.save(1, 1001, 33, BigDecimal.ONE, PLACED);

    betRepository.updateBetStatus(9999, WON);

    var betsForEvent1001 = betRepository.findByEventId(1001);
    assertThat(betsForEvent1001)
        .extracting(Bet::getId)
        .containsExactlyInAnyOrder(b1.getId(), b2.getId());
    assertThat(betsForEvent1001)
        .filteredOn(bet -> bet.getId().equals(b1.getId()))
        .extracting(Bet::getStatus)
        .containsExactly(PLACED);
    assertThat(betsForEvent1001)
        .filteredOn(bet -> bet.getId().equals(b2.getId()))
        .extracting(Bet::getStatus)
        .containsExactly(PLACED);
  }
}
