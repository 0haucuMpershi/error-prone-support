package tech.picnic.errorprone.refastertemplates.time;

import com.google.errorprone.refaster.Refaster;
import com.google.errorprone.refaster.annotation.AfterTemplate;
import com.google.errorprone.refaster.annotation.AlsoNegation;
import com.google.errorprone.refaster.annotation.BeforeTemplate;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;

final class Time {
  Time() {}

  /** Prefer {@link Instant#EPOCH} over alternative representations. */
  static final class EpochInstant {
    @BeforeTemplate
    Instant before() {
      return Refaster.anyOf(
          Instant.ofEpochMilli(0), Instant.ofEpochSecond(0), Instant.ofEpochSecond(0, 0));
    }

    @AfterTemplate
    Instant after() {
      return Instant.EPOCH;
    }
  }

  /**
   * Prefer {@link Clock#instant()} over {@link Instant#now(Clock)}, as it is more concise and more
   * "OOP-py".
   */
  static final class ClockInstant {
    @BeforeTemplate
    Instant before(Clock clock) {
      return Instant.now(clock);
    }

    @AfterTemplate
    Instant after(Clock clock) {
      return clock.instant();
    }
  }

  /**
   * Prefer {@link Instant#isBefore(Instant)} over explicit comparison, as it yields more readable
   * code.
   */
  static final class InstantIsBefore {
    @BeforeTemplate
    boolean before(Instant a, Instant b) {
      return a.compareTo(b) < 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(Instant a, Instant b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link Instant#isBefore(Instant)} over explicit comparison, as it yields more readable
   * code.
   */
  static final class InstantIsAfter {
    @BeforeTemplate
    boolean before(Instant a, Instant b) {
      return a.compareTo(b) > 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(Instant a, Instant b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDate#isBefore(ChronoLocalDate)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class ChronoLocalDateIsBefore {
    @BeforeTemplate
    boolean before(ChronoLocalDate a, ChronoLocalDate b) {
      return a.compareTo(b) < 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(ChronoLocalDate a, ChronoLocalDate b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDate#isBefore(ChronoLocalDate)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class ChronoLocalDateIsAfter {
    @BeforeTemplate
    boolean before(ChronoLocalDate a, ChronoLocalDate b) {
      return a.compareTo(b) > 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(ChronoLocalDate a, ChronoLocalDate b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDateTime#isBefore(ChronoLocalDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoLocalDateTimeIsBefore {
    @BeforeTemplate
    boolean before(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.compareTo(b) < 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link ChronoLocalDateTime#isBefore(ChronoLocalDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoLocalDateTimeIsAfter {
    @BeforeTemplate
    boolean before(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.compareTo(b) > 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(ChronoLocalDateTime<?> a, ChronoLocalDateTime<?> b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link ChronoZonedDateTime#isBefore(ChronoZonedDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoZonedDateTimeIsBefore {
    @BeforeTemplate
    boolean before(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.compareTo(b) < 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link ChronoZonedDateTime#isBefore(ChronoZonedDateTime)} over explicit comparison, as
   * it yields more readable code.
   */
  static final class ChronoZonedDateTimeIsAfter {
    @BeforeTemplate
    boolean before(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.compareTo(b) > 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(ChronoZonedDateTime<?> a, ChronoZonedDateTime<?> b) {
      return a.isAfter(b);
    }
  }

  /**
   * Prefer {@link OffsetDateTime#isBefore(OffsetDateTime)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class OffsetDateTimeIsBefore {
    @BeforeTemplate
    boolean before(OffsetDateTime a, OffsetDateTime b) {
      return a.compareTo(b) < 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(OffsetDateTime a, OffsetDateTime b) {
      return a.isBefore(b);
    }
  }

  /**
   * Prefer {@link OffsetDateTime#isBefore(OffsetDateTime)} over explicit comparison, as it yields
   * more readable code.
   */
  static final class OffsetDateTimeIsAfter {
    @BeforeTemplate
    boolean before(OffsetDateTime a, OffsetDateTime b) {
      return a.compareTo(b) > 0;
    }

    @AlsoNegation
    @AfterTemplate
    boolean after(OffsetDateTime a, OffsetDateTime b) {
      return a.isAfter(b);
    }
  }

  /**
   * Don't unnecessarily convert two and from milliseconds. (This way nanosecond precision is
   * retained.)
   *
   * <p><strong>Warning:</strong> this rewrite rule increases precision!
   */
  static final class DurationBetweenInstants {
    @BeforeTemplate
    Duration before(Instant a, Instant b) {
      return Duration.ofMillis(b.toEpochMilli() - a.toEpochMilli());
    }

    @AfterTemplate
    Duration after(Instant a, Instant b) {
      return Duration.between(a, b);
    }
  }

  /**
   * Don't unnecessarily convert two and from milliseconds. (This way nanosecond precision is
   * retained.)
   *
   * <p><strong>Warning:</strong> this rewrite rule increases precision!
   */
  static final class DurationBetweenOffsetDateTimes {
    @BeforeTemplate
    Duration before(OffsetDateTime a, OffsetDateTime b) {
      return Refaster.anyOf(
          Duration.between(a.toInstant(), b.toInstant()),
          Duration.ofSeconds(b.toEpochSecond() - a.toEpochSecond()));
    }

    @AfterTemplate
    Duration after(OffsetDateTime a, OffsetDateTime b) {
      return Duration.between(a, b);
    }
  }
}