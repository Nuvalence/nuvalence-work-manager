package io.nuvalence.workmanager.service.domain.dynamicschema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility to support conversion of data unmarshalled from JSON to their intended types defined by schema.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class DataConversionSupport {
    private static final Map<Class<?>, Map<Class<?>, Function<?, ?>>> converters;

    static {
        converters = new HashMap<>();
        register(Double.class, BigDecimal.class, DataConversionSupport::convertDoubleToBigDecimal);
        register(Integer.class, BigDecimal.class, DataConversionSupport::convertIntegerToBigDecimal);
        register(String.class, BigDecimal.class, DataConversionSupport::convertStringToBigDecimal);

        register(String.class, Boolean.class, DataConversionSupport::convertStringToBoolean);

        register(String.class, Integer.class, DataConversionSupport::convertStringToInteger);

        register(String.class, LocalDate.class, DataConversionSupport::convertStringToLocalDate);

        register(String.class, LocalTime.class, DataConversionSupport::convertStringToLocalTime);
    }

    /**
     * Converts the given value to the requested type, if a converter exists.
     *
     * @param value Value to convert
     * @param type requested type
     * @param <T> requested type
     * @return the value converted ot the requested type
     */
    public static <T> T convert(final Object value, final Class<T> type) {
        final Class<?> inputType = value.getClass();
        if (type.isAssignableFrom(inputType)) {
            return type.cast(value);
        }

        return findConverter(inputType, type)
                .orElseThrow(() -> new UnsupportedOperationException(String.format(
                        "No converter found to convert %s to %s",
                        value.getClass().getName(),
                        type.getName()
                )))
                .apply(inputType.cast(value));
    }

    private static <T, R> void register(final Class<T> from, final Class<R> to, final Function<T, R> converter) {
        converters.computeIfAbsent(from, (key) -> new HashMap<>()).put(to, converter);
    }

    private static <T, R> Optional<Function<Object, R>> findConverter(final Class<T> from, final Class<R> to) {
        for (Class<?> inputType : converters.keySet()) {
            if (inputType.isAssignableFrom(from)) {
                final Map<Class<?>, Function<?, ?>> candidates = converters.get(inputType);
                for (Class<?> outputType : candidates.keySet()) {
                    if (to.isAssignableFrom(outputType)) {
                        @SuppressWarnings("unchecked")
                        final Function<Object, R> converter = (Function<Object, R>) candidates.get(outputType);
                        return Optional.of(converter);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static LocalDate convertStringToLocalDate(final String value) {
        if (value.length() < 1) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private static LocalTime convertStringToLocalTime(final String value) {
        if (value.length() < 1) {
            return null;
        }
        return LocalTime.parse(value);
    }

    private static Integer convertStringToInteger(final String value) {
        if (value.length() < 1) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private static BigDecimal convertStringToBigDecimal(final String value) {
        if (value.length() < 1) {
            return null;
        }
        return new BigDecimal(value);
    }

    private static BigDecimal convertIntegerToBigDecimal(final Integer value) {
        return new BigDecimal(value);
    }

    private static BigDecimal convertDoubleToBigDecimal(final Double value) {
        return BigDecimal.valueOf(value);
    }

    private static Boolean convertStringToBoolean(final String value) {
        if (value.length() < 1) {
            return null;
        }

        switch (value.toLowerCase()) {
            case "true":
            case "yes":
                return true;
            case "false":
            case "no":
                return false;
            default:
                log.warn("Could not convert value \"{}\" to boolean value. Returning null.", value);
                return null;
        }
    }
}
