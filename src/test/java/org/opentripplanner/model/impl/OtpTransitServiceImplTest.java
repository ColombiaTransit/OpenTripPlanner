package org.opentripplanner.model.impl;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opentripplanner.gtfs.GtfsContextBuilder.contextBuilder;
import static org.opentripplanner.transit.model._data.TransitModelForTest.FEED_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentripplanner.ConstantsForTests;
import org.opentripplanner.gtfs.GtfsContextBuilder;
import org.opentripplanner.model.FareAttribute;
import org.opentripplanner.model.FareRule;
import org.opentripplanner.model.FeedInfo;
import org.opentripplanner.model.OtpTransitService;
import org.opentripplanner.model.Pathway;
import org.opentripplanner.model.ShapePoint;
import org.opentripplanner.model.Station;
import org.opentripplanner.model.Stop;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.model.StopTime;
import org.opentripplanner.model.Trip;
import org.opentripplanner.transit.model._data.TransitModelForTest;
import org.opentripplanner.transit.model.basic.FeedScopedId;
import org.opentripplanner.transit.model.organization.Agency;

public class OtpTransitServiceImplTest {

  private static final FeedScopedId STATION_ID = TransitModelForTest.id("station");

  // The subject is used as read only; hence static is ok
  private static OtpTransitService subject;

  @BeforeAll
  public static void setup() throws IOException {
    GtfsContextBuilder contextBuilder = contextBuilder(FEED_ID, ConstantsForTests.FAKE_GTFS);
    OtpTransitServiceBuilder builder = contextBuilder.getTransitBuilder();

    // Supplement test data with at least one entity in all collections
    FareRule rule = createFareRule();
    builder.getFareAttributes().add(rule.getFare());
    builder.getFareRules().add(rule);
    builder.getFeedInfos().add(FeedInfo.dummyForTest(FEED_ID));

    subject = builder.build();
  }

  @Test
  public void testGetAllAgencies() {
    Collection<Agency> agencies = subject.getAllAgencies();
    Agency agency = first(agencies);

    assertEquals(1, agencies.size());
    assertEquals("agency", agency.getId().getId());
    assertEquals("Fake Agency", agency.getName());
  }

  @Test
  public void testGetAllFareAttributes() {
    Collection<FareAttribute> fareAttributes = subject.getAllFareAttributes();

    assertEquals(1, fareAttributes.size());
    assertEquals("<FareAttribute F:FA>", first(fareAttributes).toString());
  }

  @Test
  public void testGetAllFareRules() {
    Collection<FareRule> fareRules = subject.getAllFareRules();

    assertEquals(1, fareRules.size());
    assertEquals(
      "<FareRule  origin='Zone A' contains='Zone B' destination='Zone C'>",
      first(fareRules).toString()
    );
  }

  @Test
  public void testGetAllFeedInfos() {
    Collection<FeedInfo> feedInfos = subject.getAllFeedInfos();

    assertEquals(1, feedInfos.size());
    assertEquals("<FeedInfo F>", first(feedInfos).toString());
  }

  @Test
  public void testGetAllPathways() {
    Collection<Pathway> pathways = subject.getAllPathways();

    assertEquals(3, pathways.size());
    assertEquals("<Pathway F:pathways_1_1>", first(pathways).toString());
  }

  @Test
  public void testGetAllTransfers() {
    var result = removeFeedScope(
      subject.getAllTransfers().stream().map(Object::toString).sorted().collect(joining("\n"))
    );

    assertEquals(
      """
        ConstrainedTransfer{from: <Route 2, stop D>, to: <Route 5, stop I>, constraint: {guaranteed}}
        ConstrainedTransfer{from: <Stop F>, to: <Stop E>, constraint: {minTransferTime: 20m}}
        ConstrainedTransfer{from: <Stop K>, to: <Stop L>, constraint: {priority: RECOMMENDED}}
        ConstrainedTransfer{from: <Stop K>, to: <Stop M>, constraint: {priority: NOT_ALLOWED}}
        ConstrainedTransfer{from: <Stop L>, to: <Stop K>, constraint: {priority: RECOMMENDED}}
        ConstrainedTransfer{from: <Stop M>, to: <Stop K>, constraint: {priority: NOT_ALLOWED}}
        ConstrainedTransfer{from: <Trip 1.1, stopPos 1>, to: <Trip 2.2, stopPos 0>, constraint: {guaranteed}}""",
      result
    );
  }

  @Test
  public void testGetAllStations() {
    Collection<Station> stations = subject.getAllStations();

    assertEquals(1, stations.size());
    assertEquals("<Station F:station>", first(stations).toString());
  }

  @Test
  public void testGetAllStops() {
    Collection<Stop> stops = subject.getAllStops();

    assertEquals(22, stops.size());
    assertEquals("<Stop F:A>", first(stops).toString());
  }

  @Test
  public void testGetAllStopTimes() {
    List<StopTime> stopTimes = new ArrayList<>();
    for (Trip trip : subject.getAllTrips()) {
      stopTimes.addAll(subject.getStopTimesForTrip(trip));
    }

    assertEquals(80, stopTimes.size());
    assertEquals(
      "StopTime(seq=1 stop=F:A trip=agency:1.1 times=00:00:00-00:00:00)",
      first(stopTimes).toString()
    );
  }

  @Test
  public void testGetAllTrips() {
    Collection<Trip> trips = subject.getAllTrips();

    assertEquals(33, trips.size());
    assertEquals("Trip{id: 'agency:1.1'}", first(trips).toString());
  }

  @Test
  public void testGetStopForId() {
    Stop stop = subject.getStopForId(TransitModelForTest.id("P"));
    assertEquals("<Stop F:P>", stop.toString());
  }

  @Test
  public void testGetStopsForStation() {
    List<StopLocation> stops = new ArrayList<>(subject.getStationForId(STATION_ID).getChildStops());
    assertEquals("[<Stop F:A>]", stops.toString());
  }

  @Test
  public void testGetShapePointsForShapeId() {
    List<ShapePoint> shapePoints = subject.getShapePointsForShapeId(TransitModelForTest.id("5"));
    assertEquals(
      "[#1 (41,-72), #2 (41,-72), #3 (40,-72), #4 (41,-73), #5 (41,-74)]",
      shapePoints.stream().map(OtpTransitServiceImplTest::toString).toList().toString()
    );
  }

  @Test
  public void testGetStopTimesForTrip() {
    List<StopTime> stopTimes = subject.getStopTimesForTrip(first(subject.getAllTrips()));
    assertEquals(
      "[<Stop F:A>, <Stop F:B>, <Stop F:C>]",
      stopTimes.stream().map(StopTime::getStop).toList().toString()
    );
  }

  @Test
  public void testGetAllServiceIds() {
    Collection<FeedScopedId> serviceIds = subject.getAllServiceIds();

    assertEquals(2, serviceIds.size());
    assertEquals("F:alldays", first(serviceIds).toString());
  }

  @Test
  public void testHasActiveTransit() {
    assertTrue(subject.hasActiveTransit());
  }

  private static FareRule createFareRule() {
    FareAttribute fa = new FareAttribute(TransitModelForTest.id("FA"));
    FareRule rule = new FareRule();
    rule.setOriginId("Zone A");
    rule.setContainsId("Zone B");
    rule.setDestinationId("Zone C");
    rule.setFare(fa);
    return rule;
  }

  private static String removeFeedScope(String text) {
    return text.replace("agency:", "").replace(FEED_ID + ":", "");
  }

  private static <T> List<T> sort(Collection<? extends T> c) {
    return c.stream().sorted(comparing(T::toString)).collect(toList());
  }

  private static <T> T first(Collection<? extends T> c) {
    return c.stream().min(comparing(T::toString)).orElseThrow();
  }

  private static String toString(ShapePoint sp) {
    int lat = (int) sp.getLat();
    int lon = (int) sp.getLon();
    return "#" + sp.getSequence() + " (" + lat + "," + lon + ")";
  }
}
