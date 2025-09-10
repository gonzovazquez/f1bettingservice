package com.sg.f1bettingservice.persistence.inmemory;

import com.sg.f1bettingservice.core.model.EventOutcome;
import com.sg.f1bettingservice.persistence.OutcomeRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryOutcomeRepository implements OutcomeRepository {

  private final Map<Integer, EventOutcome> outcomes = new ConcurrentHashMap<>();

  @Override
  public EventOutcome save(EventOutcome eventOutcome) {
    outcomes.put(eventOutcome.getEventId(), eventOutcome);
    return eventOutcome;
  }
}
