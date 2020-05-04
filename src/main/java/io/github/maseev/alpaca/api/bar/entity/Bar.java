package io.github.maseev.alpaca.api.bar.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.github.maseev.alpaca.http.json.UnixTimeDeserializer;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value.Immutable
@JsonSerialize(as = ImmutableBar.class)
@JsonDeserialize(as = ImmutableBar.class)
public interface Bar {

  /**
   * @return the beginning time of this bar as a Unix epoch in seconds
   */
  @JsonProperty("t")
  @JsonDeserialize(using = UnixTimeDeserializer.class)
  Instant time();

  @JsonProperty("o")
  BigDecimal openPrice();

  @JsonProperty("h")
  BigDecimal highPrice();

  @JsonProperty("l")
  BigDecimal lowPrice();

  @JsonProperty("c")
  BigDecimal closePrice();

  @JsonProperty("v")
  long volume();
}
