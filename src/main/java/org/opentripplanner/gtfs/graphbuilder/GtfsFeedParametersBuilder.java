package org.opentripplanner.gtfs.graphbuilder;

import java.net.URI;
import org.opentripplanner.transit.model.site.StopTransferPriority;

/**
 * Configure a GTFS feed.
 */
public class GtfsFeedParametersBuilder {

  private URI source;
  private String feedId;
  private boolean removeRepeatedStops = GtfsFeedParameters.DEFAULT_REMOVE_REPEATED_STOPS;
  private StopTransferPriority stationTransferPreference =
    GtfsFeedParameters.DEFAULT_STATION_TRANSFER_PREFERENCE;
  private boolean discardMinTransferTimes = GtfsFeedParameters.DEFAULT_DISCARD_MIN_TRANSFER_TIMES;

  public GtfsFeedParametersBuilder() {}

  public GtfsFeedParametersBuilder(GtfsFeedParameters original) {
    this.removeRepeatedStops = original.removeRepeatedStops();
    this.stationTransferPreference = original.stationTransferPreference();
    this.discardMinTransferTimes = original.discardMinTransferTimes();
  }

  public GtfsFeedParametersBuilder withFeedId(String feedId) {
    this.feedId = feedId;
    return this;
  }

  String feedId() {
    return feedId;
  }

  public GtfsFeedParametersBuilder withStationTransferPreference(
    StopTransferPriority stationTransferPreference
  ) {
    this.stationTransferPreference = stationTransferPreference;
    return this;
  }

  StopTransferPriority stationTransferPreference() {
    return stationTransferPreference;
  }

  public GtfsFeedParametersBuilder withSource(URI source) {
    this.source = source;
    return this;
  }

  URI source() {
    return source;
  }

  public GtfsFeedParametersBuilder withRemoveRepeatedStops(boolean value) {
    this.removeRepeatedStops = value;
    return this;
  }

  boolean removeRepeatedStops() {
    return removeRepeatedStops;
  }

  public GtfsFeedParametersBuilder withDiscardMinTransferTimes(boolean value) {
    this.discardMinTransferTimes = value;
    return this;
  }

  boolean discardMinTransferTimes() {
    return discardMinTransferTimes;
  }

  public GtfsFeedParameters build() {
    return new GtfsFeedParameters(this);
  }
}
