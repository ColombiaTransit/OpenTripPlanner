package org.opentripplanner.ext.flex.template;

import org.opentripplanner.ext.flex.FlexParameters;
import org.opentripplanner.ext.flex.FlexServiceDate;
import org.opentripplanner.ext.flex.edgetype.FlexTripEdge;
import org.opentripplanner.ext.flex.flexpathcalculator.FlexPathCalculator;
import org.opentripplanner.ext.flex.trip.FlexTrip;
import org.opentripplanner.model.PathTransfer;
import org.opentripplanner.model.Stop;
import org.opentripplanner.model.StopLocation;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.graphfinder.NearbyStop;
import org.opentripplanner.routing.spt.GraphPath;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

public class FlexAccessTemplate extends FlexAccessEgressTemplate {
  public FlexAccessTemplate(
      NearbyStop accessEgress, FlexTrip trip, int fromStopTime, int toStopTime,
      StopLocation transferStop, FlexServiceDate date, FlexPathCalculator calculator,
      FlexParameters flexParams
  ) {
    super(accessEgress, trip, fromStopTime, toStopTime, transferStop, date, calculator, flexParams);
  }

  public GraphPath createDirectGraphPath(
      NearbyStop egress, boolean arriveBy, int departureTime, ZonedDateTime startOfTime
  ) {
    List<Edge> egressEdges = egress.edges;

    Vertex flexToVertex = egress.state.getVertex();

    if (!isRouteable(flexToVertex)) {
      return null;
    }

    FlexTripEdge flexEdge = getFlexEdge(flexToVertex, egress.stop);

    State state = flexEdge.traverse(accessEgress.state);

    for (Edge e : egressEdges) {
      state = e.traverse(state);
    }

    int[] flexTimes = getFlexTimes(flexEdge, state);

    int preFlexTime = flexTimes[0];
    int flexTime = flexTimes[1];
    int postFlexTime = flexTimes[2];

    int timeShift;

    if (arriveBy) {
      int lastStopArrivalTime = departureTime - postFlexTime - secondsFromStartOfTime;
      int latestArrivalTime = trip.latestArrivalTime(
          lastStopArrivalTime,
          fromStopIndex,
          toStopIndex,
          flexTime
      );
      if (latestArrivalTime == -1) {
        return null;
      }

      // Shift from departing at departureTime to arriving at departureTime
      timeShift = secondsFromStartOfTime + latestArrivalTime - flexTime - preFlexTime;
    } else {
      int firstStopDepartureTime = departureTime + preFlexTime - secondsFromStartOfTime;
      int earliestDepartureTime = trip.earliestDepartureTime(
          firstStopDepartureTime,
          fromStopIndex,
          toStopIndex,
          flexTime
      );
      if (earliestDepartureTime == -1) {
        return null;
      }

      timeShift = secondsFromStartOfTime + earliestDepartureTime - preFlexTime;
    }

    State s = state;
    while (s != null) {
      s.timeshiftBySeconds(timeShift);
      s = s.getBackState();
    }

    return new GraphPath(state);
  }

  protected List<Edge> getTransferEdges(PathTransfer transfer) {
    return transfer.getEdges();
  }

  protected Stop getFinalStop(PathTransfer transfer) {
    return transfer.to instanceof Stop ? (Stop) transfer.to : null;
  }

  protected Collection<PathTransfer> getTransfersFromTransferStop(Graph graph) {
    return graph.transfersByStop.get(transferStop);
  }

  protected Vertex getFlexVertex(Edge edge) {
    return edge.getFromVertex();
  }

  protected boolean isRouteable(Vertex flexVertex) {
    if (accessEgress.state.getVertex() == flexVertex) {
      return false;
    } else
      return calculator.calculateFlexPath(accessEgress.state.getVertex(),
          flexVertex,
          fromStopIndex,
          toStopIndex
      ) != null;
  }

  protected int[] getFlexTimes(FlexTripEdge flexEdge, State state) {
    int preFlexTime = (int) accessEgress.state.getElapsedTimeSeconds();
    int edgeTimeInSeconds = flexEdge.getTimeInSeconds();
    int postFlexTime = (int) state.getElapsedTimeSeconds() - preFlexTime - edgeTimeInSeconds;
    return new int[]{ preFlexTime, edgeTimeInSeconds, postFlexTime };
  }

  protected FlexTripEdge getFlexEdge(Vertex flexToVertex, StopLocation transferStop) {
    return new FlexTripEdge(
        accessEgress.state.getVertex(),
        flexToVertex,
        accessEgress.stop,
        transferStop,
        trip,
        this,
        calculator
    );
  }
}
