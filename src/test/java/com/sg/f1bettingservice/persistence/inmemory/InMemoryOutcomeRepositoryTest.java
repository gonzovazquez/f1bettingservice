package com.sg.f1bettingservice.persistence.inmemory;

import static org.assertj.core.api.Assertions.assertThat;

import com.sg.f1bettingservice.core.model.EventOutcome;
import org.junit.jupiter.api.Test;

class InMemoryOutcomeRepositoryTest {

  InMemoryOutcomeRepository inMemoryOutcomeRepository = new InMemoryOutcomeRepository();

  @Test
  void shouldSaveEventOutcomeAndReturnsIdPopulated() {
    var eventOutcome =
        EventOutcome.builder().eventId(1).betsWon(1).betsLost(1).winnerDriverId(14).build();
    var savedOutcome = inMemoryOutcomeRepository.save(eventOutcome);
    assertThat(savedOutcome).isEqualTo(eventOutcome);
  }
}
