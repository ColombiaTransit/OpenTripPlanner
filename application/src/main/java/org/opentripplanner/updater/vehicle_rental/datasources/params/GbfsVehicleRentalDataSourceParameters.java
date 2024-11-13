package org.opentripplanner.updater.vehicle_rental.datasources.params;

import java.util.Objects;
import org.opentripplanner.updater.spi.HttpHeaders;
import org.opentripplanner.updater.vehicle_rental.VehicleRentalSourceType;

public record GbfsVehicleRentalDataSourceParameters(
  String url,
  String language,
  boolean allowKeepingRentedVehicleAtDestination,
  HttpHeaders httpHeaders,
  String network,
  boolean geofencingZones,
  boolean overloadingAllowed,
  AllowedRentalType allowedRentalType
)
  implements VehicleRentalDataSourceParameters {
  public GbfsVehicleRentalDataSourceParameters {
    Objects.requireNonNull(allowedRentalType);
  }
  @Override
  public VehicleRentalSourceType sourceType() {
    return VehicleRentalSourceType.GBFS;
  }

  @Override
  public boolean allowRentalType(AllowedRentalType rentalType) {
    return (
      allowedRentalType == null ||
      allowedRentalType == AllowedRentalType.ALL ||
      allowedRentalType == rentalType
    );
  }
}
