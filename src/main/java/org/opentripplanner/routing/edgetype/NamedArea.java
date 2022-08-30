package org.opentripplanner.routing.edgetype;

import java.io.Serializable;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.transit.model.basic.I18NString;

/**
 * A named area is a subset of an area with a certain set of properties (name, safety, etc). Its
 * originalEdges may include some edges which are crossable (because they separate it from another
 * contiguous and routeable area).
 */
public class NamedArea implements Serializable {

  private Geometry originalEdges;

  private I18NString name;

  private double bicycleSafetyMultiplier;

  private double walkSafetyMultiplier;

  private int streetClass;

  private StreetTraversalPermission permission;

  public I18NString getName() {
    return name;
  }

  public void setName(I18NString name) {
    this.name = name;
  }

  public Geometry getPolygon() {
    return originalEdges;
  }

  public void setOriginalEdges(Geometry originalEdges) {
    this.originalEdges = originalEdges;
  }

  public double getBicycleSafetyMultiplier() {
    return bicycleSafetyMultiplier;
  }

  public void setBicycleSafetyMultiplier(double bicycleSafetyMultiplier) {
    this.bicycleSafetyMultiplier = bicycleSafetyMultiplier;
  }

  public double getWalkSafetyMultiplier() {
    return walkSafetyMultiplier;
  }

  public void setWalkSafetyMultiplier(double walkSafetyMultiplier) {
    this.walkSafetyMultiplier = walkSafetyMultiplier;
  }

  public StreetTraversalPermission getPermission() {
    return permission;
  }

  public void setPermission(StreetTraversalPermission permission) {
    this.permission = permission;
  }

  public int getStreetClass() {
    return streetClass;
  }

  public void setStreetClass(int streetClass) {
    this.streetClass = streetClass;
  }
}
