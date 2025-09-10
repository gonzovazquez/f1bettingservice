package com.sg.f1bettingservice.rest.event;

import com.sg.f1bettingservice.core.EventService;
import com.sg.f1bettingservice.core.OutcomeService;
import com.sg.f1bettingservice.core.model.Event;
import com.sg.f1bettingservice.core.model.EventOutcome;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {

  private final EventService eventService;
  private final OutcomeService outcomeService;

  @GetMapping
  public List<Event> findEvents(String sessionType, Integer year, String country) {
    return eventService.findEvents(sessionType, year, country);
  }

  @PostMapping(value = "/outcome", produces = "application/json")
  public EventOutcome publishOutcome(@Valid @RequestBody PublishOutcomeRequest request) {
    return outcomeService.publishOutcome(request.getEventId());
  }
}
