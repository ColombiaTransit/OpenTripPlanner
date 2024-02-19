package org.opentripplanner.apis.gtfs;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.relay.Relay;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.Nonnull;
import org.locationtech.jts.geom.Geometry;
import org.opentripplanner.framework.geometry.GeometryUtils;
import org.opentripplanner.framework.graphql.scalar.DurationScalarFactory;
import org.opentripplanner.framework.model.Grams;
import org.opentripplanner.framework.time.OffsetDateTimeParser;

public class GraphQLScalars {

  private static final ObjectMapper geoJsonMapper = new ObjectMapper()
    .registerModule(new JtsModule(GeometryUtils.getGeometryFactory()));
  public static GraphQLScalarType durationScalar = DurationScalarFactory.createDurationScalar();

  public static GraphQLScalarType polylineScalar = GraphQLScalarType
    .newScalar()
    .name("Polyline")
    .description(
      "List of coordinates in an encoded polyline format (see https://developers.google.com/maps/documentation/utilities/polylinealgorithm). The value appears in JSON as a string."
    )
    .coercing(
      new Coercing<String, String>() {
        @Override
        public String serialize(Object input) {
          return input == null ? null : input.toString();
        }

        @Override
        public String parseValue(Object input) {
          return serialize(input);
        }

        @Override
        public String parseLiteral(Object input) {
          if (!(input instanceof StringValue)) {
            return null;
          }
          return ((StringValue) input).getValue();
        }
      }
    )
    .build();

  public static final GraphQLScalarType offsetDateTimeScalar = GraphQLScalarType
    .newScalar()
    .name("OffsetDateTime")
    .coercing(
      new Coercing<OffsetDateTime, String>() {
        @Override
        public String serialize(@Nonnull Object dataFetcherResult)
          throws CoercingSerializeException {
          if (dataFetcherResult instanceof ZonedDateTime zdt) {
            return zdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
          } else if (dataFetcherResult instanceof OffsetDateTime odt) {
            return odt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
          } else {
            throw new CoercingSerializeException(
              "Cannot serialize object of class %s".formatted(
                  dataFetcherResult.getClass().getSimpleName()
                )
            );
          }
        }

        @Override
        public OffsetDateTime parseValue(Object input) throws CoercingParseValueException {
          if (input instanceof CharSequence cs) {
            return OffsetDateTimeParser.parseLeniently(cs).orElseThrow(() -> valueException(input));
          }
          throw valueException(input);
        }

        @Override
        public OffsetDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
          if (input instanceof StringValue sv) {
            return OffsetDateTimeParser
              .parseLeniently(sv.getValue())
              .orElseThrow(() -> literalException(input));
          }
          throw literalException(input);
        }

        private static CoercingParseValueException valueException(Object input) {
          return new CoercingParseValueException("Cannot parse %s".formatted(input));
        }

        private static CoercingParseLiteralException literalException(Object input) {
          return new CoercingParseLiteralException("Cannot parse %s".formatted(input));
        }
      }
    )
    .build();

  public static final GraphQLScalarType coordinateValueScalar = GraphQLScalarType
    .newScalar()
    .name("CoordinateValue")
    .coercing(
      new Coercing<Double, Double>() {
        @Override
        public Double serialize(@Nonnull Object dataFetcherResult)
          throws CoercingSerializeException {
          if (dataFetcherResult instanceof Double doubleValue) {
            return doubleValue;
          } else if (dataFetcherResult instanceof Float floatValue) {
            return floatValue.doubleValue();
          } else {
            throw new CoercingSerializeException(
              "Cannot serialize object of class %s as a coordinate number".formatted(
                  dataFetcherResult.getClass().getSimpleName()
                )
            );
          }
        }

        @Override
        public Double parseValue(Object input) throws CoercingParseValueException {
          if (input instanceof Double doubleValue) {
            return validateCoordinate(doubleValue);
          }
          throw new CoercingParseValueException(
            "Expected a number, got %s %s".formatted(input.getClass().getSimpleName(), input)
          );
        }

        @Override
        public Double parseLiteral(Object input) throws CoercingParseLiteralException {
          if (input instanceof FloatValue coordinate) {
            return validateCoordinate(coordinate.getValue().doubleValue());
          }
          if (input instanceof IntValue coordinate) {
            return validateCoordinate(coordinate.getValue().doubleValue());
          }
          throw new CoercingParseLiteralException(
            "Expected a number, got: " + input.getClass().getSimpleName()
          );
        }

        private static double validateCoordinate(double coordinate) {
          if (coordinate >= -180.001 && coordinate <= 180.001) {
            return coordinate;
          }
          throw new CoercingParseLiteralException("Not a valid WGS84 coordinate value");
        }
      }
    )
    .build();

  public static GraphQLScalarType geoJsonScalar = GraphQLScalarType
    .newScalar()
    .name("GeoJson")
    .description("Geographic data structures in JSON format. See: https://geojson.org/")
    .coercing(
      new Coercing<Geometry, JsonNode>() {
        @Override
        public JsonNode serialize(Object dataFetcherResult) throws CoercingSerializeException {
          if (dataFetcherResult instanceof Geometry) {
            var geom = (Geometry) dataFetcherResult;
            return geoJsonMapper.valueToTree(geom);
          }
          return null;
        }

        @Override
        public Geometry parseValue(Object input) throws CoercingParseValueException {
          return null;
        }

        @Override
        public Geometry parseLiteral(Object input) throws CoercingParseLiteralException {
          return null;
        }
      }
    )
    .build();

  public static GraphQLScalarType graphQLIDScalar = GraphQLScalarType
    .newScalar()
    .name("ID")
    .coercing(
      new Coercing<Relay.ResolvedGlobalId, String>() {
        @Override
        public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
          if (dataFetcherResult instanceof Relay.ResolvedGlobalId) {
            Relay.ResolvedGlobalId globalId = (Relay.ResolvedGlobalId) dataFetcherResult;
            return new Relay().toGlobalId(globalId.getType(), globalId.getId());
          }
          throw new CoercingSerializeException(
            "Unknown type " + dataFetcherResult.getClass().getSimpleName()
          );
        }

        @Override
        public Relay.ResolvedGlobalId parseValue(Object input) throws CoercingParseValueException {
          if (input instanceof String) {
            return new Relay().fromGlobalId((String) input);
          }
          throw new CoercingParseValueException(
            "Unexpected type " + input.getClass().getSimpleName()
          );
        }

        @Override
        public Relay.ResolvedGlobalId parseLiteral(Object input)
          throws CoercingParseLiteralException {
          if (input instanceof StringValue) {
            return new Relay().fromGlobalId(((StringValue) input).getValue());
          }
          throw new CoercingParseLiteralException(
            "Unexpected type " + input.getClass().getSimpleName()
          );
        }
      }
    )
    .build();

  public static GraphQLScalarType gramsScalar = GraphQLScalarType
    .newScalar()
    .name("Grams")
    .coercing(
      new Coercing<Grams, Double>() {
        @Override
        public Double serialize(Object dataFetcherResult) throws CoercingSerializeException {
          if (dataFetcherResult instanceof Grams) {
            var grams = (Grams) dataFetcherResult;
            return Double.valueOf(grams.asDouble());
          }
          return null;
        }

        @Override
        public Grams parseValue(Object input) throws CoercingParseValueException {
          if (input instanceof Double) {
            var grams = (Double) input;
            return new Grams(grams);
          }
          return null;
        }

        @Override
        public Grams parseLiteral(Object input) throws CoercingParseLiteralException {
          if (input instanceof Double) {
            var grams = (Double) input;
            return new Grams(grams);
          }
          return null;
        }
      }
    )
    .build();

  public static GraphQLScalarType ratioScalar = GraphQLScalarType
    .newScalar()
    .name("Ratio")
    .coercing(
      new Coercing<OffsetDateTime, String>() {
        @Override
        public String serialize(@Nonnull Object dataFetcherResult)
          throws CoercingSerializeException {
          return null;
        }

        @Override
        public OffsetDateTime parseValue(Object input) throws CoercingParseValueException {
          return null;
        }

        @Override
        public OffsetDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
          return null;
        }
      }
    )
    .build();
}
