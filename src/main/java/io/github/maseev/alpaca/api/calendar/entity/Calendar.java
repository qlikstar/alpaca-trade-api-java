package io.github.maseev.alpaca.api.calendar.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;

@Value.Immutable
@JsonSerialize(as = ImmutableCalendar.class)
@JsonDeserialize(as = ImmutableCalendar.class)
public interface Calendar {

  LocalDate date();

  /**
   * @return The time the market opens at on this {@link Calendar#date() date}
   */
  LocalTime open();

  /**
   * @return The time the market closes at on this {@link Calendar#date() date}
   */
  LocalTime close();
}
