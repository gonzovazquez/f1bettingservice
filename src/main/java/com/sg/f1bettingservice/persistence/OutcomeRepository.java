package com.sg.f1bettingservice.persistence;

import com.sg.f1bettingservice.core.model.EventOutcome;

public interface OutcomeRepository {
  EventOutcome save(EventOutcome eventOutcome);
}
