package org.opentripplanner.model.impl;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opentripplanner.gtfs.GtfsContextBuilder.contextBuilder;

import java.io.IOException;
import java.util.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.model.FareAttribute;
import org.opentripplanner.model.FareRule;
import org.opentripplanner.model.FeedInfo;
import org.opentripplanner.model.Frequency;
import org.opentripplanner.model.ShapePoint;
import org.opentripplanner.model.calendar.ServiceCalendar;
import org.opentripplanner.model.calendar.ServiceCalendarDate;
import org.opentripplanner.model.calendar.ServiceDate;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.FeedScopedId;
import org.opentripplanner.transit.model.network.Route;
import org.opentripplanner.transit.model.organization.Agency;

public class OtpTransitServiceBuilderTest {

  private static final String FEED_ID = TransitModelForTest.FEED_ID;
  private static final FeedScopedId SERVICE_WEEKDAYS_ID = TransitModelForTest.id("weekdays");

  private static OtpTransitServiceBuilder subject;

  @BeforeAll
  public static void setUpClass() throws IOException {
    subject = createBuilder();
  }

  @Test
  public void testGetAllCalendarDates() {
    Collection<ServiceCalendarDate> calendarDates = subject.getCalendarDates();

    assertEquals(1, calendarDates.size());
    assertEquals(
      "<CalendarDate serviceId=F:weekdays date=2017-08-31 exception=2>",
      first(calendarDates).toString()
    );
  }

  @Test
  public void testGetAllCalendars() {
    Collection<ServiceCalendar> calendars = subject.getCalendars();

    assertEquals(2, calendars.size());
    assertEquals("<ServiceCalendar F:alldays [1111111]>", first(calendars).toString());
  }

  @Test
  public void testGetAllFrequencies() {
    Collection<Frequency> frequencies = subject.getFrequencies();

    assertEquals(2, frequencies.size());
    assertEquals(
      "<Frequency trip=agency:15.1 start=06:00:00 end=10:00:01>",
      first(frequencies).toString()
    );
  }

  @Test
  public void testGetRoutes() {
    Collection<Route> routes = subject.getRoutes().values();

    assertEquals(18, routes.size());
    assertEquals("<Route agency:1 1>", first(routes).toString());
  }

  @Test
  public void testGetAllShapePoints() {
    Collection<ShapePoint> shapePoints = subject.getShapePoints().values();

    assertEquals(9, shapePoints.size());
    assertEquals("<ShapePoint F:4 #1 (41.0,-75.0)>", first(shapePoints).toString());
  }

  /* private methods */

  private static OtpTransitServiceBuilder createBuilder() throws IOException {
    OtpTransitServiceBuilder builder = contextBuilder(FEED_ID, ConstantsForTests.FAKE_GTFS)
      .getTransitBuilder();
    Agency agency = agency(builder);

    // Supplement test data with at least one entity in all collections
    builder.getCalendarDates().add(createAServiceCalendarDateExclution(SERVICE_WEEKDAYS_ID));
    builder.getFareAttributes().add(createFareAttribute());
    builder.getFareRules().add(new FareRule());
    builder.getFeedInfos().add(FeedInfo.dummyForTest(FEED_ID));

    return builder;
  }

  private static Agency agency(OtpTransitServiceBuilder builder) {
    return first(builder.getAgenciesById().values());
  }

  private static FareAttribute createFareAttribute() {
    return new FareAttribute(TransitModelForTest.id("FA"));
  }

  private static ServiceCalendarDate createAServiceCalendarDateExclution(FeedScopedId serviceId) {
    return new ServiceCalendarDate(serviceId, new ServiceDate(2017, 8, 31), 2);
  }

  private static <T> T first(Collection<? extends T> c) {
    return c.stream().min(comparing(T::toString)).orElse(null);
  }
}
