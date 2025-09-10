package com.sg.f1bettingservice.f1data.openf1;

import static java.time.OffsetDateTime.parse;
import static org.assertj.core.api.Assertions.assertThat;

import com.sg.f1bettingservice.core.model.Driver;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OpenF1DataMapperTest {

  OpenF1DataMapper mapper = new OpenF1DataMapper();

  @Nested
  class ToEventTests {
    @Test
    void shouldMapOpenF1SessionToEvent() {
      var session =
          new OpenF1Session(
              9140,
              1216,
              "Belgian Grand Prix",
              "Sprint",
              "Race",
              "Belgium",
              "2023-07-29T15:05:00+00:00",
              2023);

      var sessions = List.of(session);
      var result = mapper.toEventList(sessions);

      assertThat(result).hasSameSizeAs(List.of(sessions));
      var event = result.getFirst();
      assertThat(event.getEventId()).isEqualTo(session.session_key());
      assertThat(event.getName()).isEqualTo(session.session_name());
      assertThat(event.getSessionType()).isEqualTo(session.session_type());
      assertThat(event.getCountry()).isEqualTo(session.country_name());
      assertThat(event.getDateTime()).isEqualTo(parse(session.date_start()));
      assertThat(event.getDrivers()).isEmpty();
    }

    @Test
    void shouldMapNullOpenF1SessionToEmptyEventList() {
      assertThat(mapper.toEventList(null)).isEmpty();
    }

    @Test
    void shouldMapEmptyOpenF1SessionToEmptyEventList() {
      assertThat(mapper.toEventList(List.of())).isEmpty();
    }
  }

  @Nested
  class ToDriverTests {
    @Test
    void shouldMapOpenF1DriversToDrivers() {
      var d1 = new OpenF1Driver(44, "Lewis Hamilton");
      var d2 = new OpenF1Driver(33, "Max Verstappen");

      var drivers = List.of(d1, d2);
      var result = mapper.toDriverList(drivers);

      assertThat(result).hasSameSizeAs(drivers);

      assertThat(result)
          .filteredOn(
              e -> e.getDriverId() == d1.driver_number() || e.getDriverId() == d2.driver_number())
          .extracting(Driver::getFullName)
          .containsExactlyInAnyOrder(d1.full_name(), d2.full_name());
    }

    @Test
    void shouldMapNullOpenF1DriverToEmptyDriverList() {
      assertThat(mapper.toDriverList(null)).isEmpty();
    }

    @Test
    void shouldMapEmptyOpenF1DriverToEmptyDriverList() {
      assertThat(mapper.toDriverList(List.of())).isEmpty();
    }
  }
}
