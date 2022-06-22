package io.nuvalence.workmanager.service.mapper;

import com.vladmihalcea.hibernate.util.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A mapper for OffsetDateTime.
 */
@Mapper(componentModel = "spring")
public abstract class OffsetDateTimeMapper {
    public static final OffsetDateTimeMapper INSTANCE = Mappers.getMapper(OffsetDateTimeMapper.class);
    private final Clock clock = Clock.systemDefaultZone();
    private DateTimeFormatter yyyyMMddFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Maps a string in ISO_LOCAL_DATE_TIME format to an OffsetDateTime.
     *
     * @param dt The date in ISO_LOCAL_DATE_TIME
     * @return An OffsetDateTime
     */
    public OffsetDateTime toOffsetDateTime(String dt) {
        if (StringUtils.isBlank(dt)) {
            return null;
        }

        LocalDateTime dateTime = LocalDateTime.parse(dt);
        return OffsetDateTime.of(dateTime, clock.getZone().getRules().getOffset(dateTime));
    }

    /**
     * Maps a string in ISO_LOCAL_DATE_TIME format to an OffsetDateTime (at the start of the day).
     *
     * @param dt The date in ISO_LOCAL_DATE_TIME
     * @return An OffsetDateTime
     */
    public OffsetDateTime toOffsetDateTimeStartOfDay(String dt) {
        if (StringUtils.isBlank(dt)) {
            return null;
        }

        LocalDateTime dateTime = LocalDate.parse(dt, yyyyMMddFormat).atStartOfDay();
        return OffsetDateTime.of(dateTime, clock.getZone().getRules().getOffset(dateTime));

    }

    /**
     * Maps a string in ISO_LOCAL_DATE_TIME format to an OffsetDateTime (at the  end of the day).
     *
     * @param dt The date in ISO_LOCAL_DATE_TIME
     * @return An OffsetDateTime
     */
    public OffsetDateTime toOffsetDateTimeEndOfDay(String dt) {
        if (StringUtils.isBlank(dt)) {
            return null;
        }

        LocalDateTime dateTime = LocalDate.parse(dt, yyyyMMddFormat).atStartOfDay().minusDays(-1);
        return OffsetDateTime.of(dateTime, clock.getZone().getRules().getOffset(dateTime));
    }

    /**
     * Converts an OffsetDateTime object to a string with the yyyy-MM-dd format.
     * @param dt The date.
     * @return A string.
     */
    public String toString(OffsetDateTime dt) {
        if (dt == null) {
            return null;
        }

        return dt.format(yyyyMMddFormat);
    }
}
