package org.opentripplanner.ext.vectortiles.layers.stops;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.opentripplanner.apis.gtfs.PatternTestModel;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.network.TripPattern;
import org.opentripplanner.transit.model.site.RegularStop;

class LayerFiltersTest {

  private static final RegularStop STOP = TransitModelForTest.of().stop("1").build();
  private static final LocalDate DATE = LocalDate.of(2024, 9, 5);
  private static final TripPattern PATTERN = PatternTestModel.pattern();

  @Test
  void includeStopWithinServiceWeek() {
    var predicate = LayerFilters.currentServiceWeek(
      s -> List.of(PATTERN),
      trip -> List.of(DATE),
      () -> DATE
    );

    assertTrue(predicate.test(STOP));
  }

  @Test
  void excludeOutsideServiceWeek() {
    var inThreeWeeks = DATE.plusDays(21);
    var predicate = LayerFilters.currentServiceWeek(
      s -> List.of(PATTERN),
      trip -> List.of(inThreeWeeks),
      () -> DATE
    );

    assertFalse(predicate.test(STOP));
  }
}
