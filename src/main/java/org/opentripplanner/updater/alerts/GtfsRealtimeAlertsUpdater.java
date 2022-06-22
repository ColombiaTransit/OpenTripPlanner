package org.opentripplanner.updater.alerts;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import org.opentripplanner.routing.RoutingService;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.impl.TransitAlertServiceImpl;
import org.opentripplanner.routing.services.TransitAlertService;
import org.opentripplanner.transit.service.DefaultTransitService;
import org.opentripplanner.updater.GtfsRealtimeFuzzyTripMatcher;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.WriteToGraphCallback;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GTFS-RT alerts updater
 * <p>
 * Usage example:
 *
 * <pre>
 * myalert.type = real-time-alerts
 * myalert.frequencySec = 60
 * myalert.url = http://host.tld/path
 * myalert.earlyStartSec = 3600
 * myalert.feedId = TA
 * </pre>
 */
public class GtfsRealtimeAlertsUpdater extends PollingGraphUpdater implements TransitAlertProvider {

  private static final Logger LOG = LoggerFactory.getLogger(GtfsRealtimeAlertsUpdater.class);
  private final String url;
  private final String feedId;
  private final long earlyStart;
  private final boolean fuzzyTripMatching;
  private WriteToGraphCallback saveResultOnGraph;
  private Long lastTimestamp = Long.MIN_VALUE;
  private GtfsRealtimeFuzzyTripMatcher fuzzyTripMatcher;
  private AlertsUpdateHandler updateHandler = null;
  private TransitAlertService transitAlertService;

  public GtfsRealtimeAlertsUpdater(GtfsRealtimeAlertsUpdaterParameters config) {
    super(config);
    this.url = config.getUrl();
    this.earlyStart = config.getEarlyStartSec();
    this.feedId = config.getFeedId();
    this.fuzzyTripMatching = config.fuzzyTripMatching();

    LOG.info(
      "Creating real-time alert updater running every {} seconds : {}",
      pollingPeriodSeconds,
      url
    );
  }

  @Override
  public void setGraphUpdaterManager(WriteToGraphCallback saveResultOnGraph) {
    this.saveResultOnGraph = saveResultOnGraph;
  }

  @Override
  public void setup(Graph graph) {
    TransitAlertService transitAlertService = new TransitAlertServiceImpl(graph);
    if (fuzzyTripMatching) {
      this.fuzzyTripMatcher =
        new GtfsRealtimeFuzzyTripMatcher(
          new RoutingService(graph),
          new DefaultTransitService(graph)
        );
    }
    this.transitAlertService = transitAlertService;
    if (updateHandler == null) {
      updateHandler = new AlertsUpdateHandler();
    }
    updateHandler.setEarlyStart(earlyStart);
    updateHandler.setFeedId(feedId);
    updateHandler.setTransitAlertService(transitAlertService);
    updateHandler.setFuzzyTripMatcher(fuzzyTripMatcher);
  }

  @Override
  public void teardown() {}

  public TransitAlertService getTransitAlertService() {
    return transitAlertService;
  }

  public String toString() {
    return "GtfsRealtimeUpdater(" + url + ")";
  }

  @Override
  protected void runPolling() {
    try {
      InputStream data = HttpUtils.getData(
        URI.create(url),
        Map.of(
          "Accept",
          "application/x-google-protobuf, application/x-protobuf, application/protobuf, application/octet-stream, */*"
        )
      );
      if (data == null) {
        throw new RuntimeException("Failed to get data from url " + url);
      }

      final FeedMessage feed = FeedMessage.PARSER.parseFrom(data);

      long feedTimestamp = feed.getHeader().getTimestamp();
      if (feedTimestamp == lastTimestamp) {
        LOG.debug("Ignoring feed with a timestamp that has not been updated from " + url);
        return;
      }
      if (feedTimestamp < lastTimestamp) {
        LOG.info("Ignoring feed with older than previous timestamp from " + url);
        return;
      }

      // Handle update in graph writer runnable
      saveResultOnGraph.execute(graph -> updateHandler.update(feed));

      lastTimestamp = feedTimestamp;
    } catch (Exception e) {
      LOG.error("Error reading gtfs-realtime feed from " + url, e);
    }
  }
}
