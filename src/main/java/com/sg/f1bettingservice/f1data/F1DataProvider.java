package com.sg.f1bettingservice.f1data;

import com.sg.f1bettingservice.core.model.Driver;
import com.sg.f1bettingservice.core.model.Event;
import java.util.List;
import java.util.Optional;

public interface F1DataProvider {

  List<Event> findEvents(String sessionType, Integer year, String country)
      throws F1DataProviderException;

  List<Driver> getDriversByEventId(Integer eventId) throws F1DataProviderException;

  Optional<Event> findEventById(int eventId);

  Optional<Integer> getWinnerDriverIdByEventId(Integer eventId);
}
