package io.github.maseev.alpaca.api.order;

import io.github.maseev.alpaca.api.order.entity.ImmutableOrderRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.github.maseev.alpaca.api.order.entity.Order.Side.BUY;
import static io.github.maseev.alpaca.api.order.entity.Order.TimeInForce.*;
import static io.github.maseev.alpaca.api.order.entity.Order.Type.*;
import static java.math.BigDecimal.valueOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OrderRequestTest {

  @Test
  public void specifyingExtendedHoursWithoutCorrectTimeInForceMustThrowException() {
    String clientOrderId = UUID.randomUUID().toString();

    assertThrows(IllegalStateException.class, () ->
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(1)
        .side(BUY)
        .type(LIMIT)
        .limitPrice(valueOf(1.2))
        .timeInForce(GTC)
        .clientOrderId(clientOrderId)
        .extendedHours(true)
        .build());
  }

  @Test
  public void specifyingTooLongClientOrderIdMustThrowException() {
    String clientOrderId = UUID.randomUUID().toString();

    assertThrows(IllegalStateException.class, () ->
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(1)
        .side(BUY)
        .type(MARKET)
        .timeInForce(DAY)
        .clientOrderId(clientOrderId + clientOrderId)
        .build());
  }

  @Test
  public void usingMarketOrderAndSpecifyingLimitPriceMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(1)
        .side(BUY)
        .type(MARKET)
        .limitPrice(valueOf(1))
        .timeInForce(DAY)
        .build());
  }

  @Test
  public void usingMarketOrderAndSpecifyingStopPriceMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(-1)
        .side(BUY)
        .type(MARKET)
        .stopPrice(valueOf(1))
        .timeInForce(DAY)
        .build());
  }

  @Test
  public void usingNegativeQtyParameterMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(-1)
        .side(BUY)
        .type(MARKET)
        .timeInForce(DAY)
        .build());
  }

  @Test
  public void usingIncorrectTimeInForceParameterMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
    ImmutableOrderRequest.builder()
      .symbol("AAPL")
      .qty(1)
      .side(BUY)
      .type(MARKET)
      .timeInForce(FOK)
      .build());
  }

  @Test
  public void usingLimitOrderAndNotSpecifyingLimitPriceMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
    ImmutableOrderRequest.builder()
      .symbol("AAPL")
      .qty(1)
      .side(BUY)
      .type(LIMIT)
      .timeInForce(DAY)
      .build());
  }

  @Test
  public void usingStopOrderAndNotSpecifyingStopPriceMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
    ImmutableOrderRequest.builder()
      .symbol("AAPL")
      .qty(1)
      .side(BUY)
      .type(STOP)
      .timeInForce(DAY)
      .build());
  }

  @Test
  public void specifyingNegativeLimitPriceMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
    ImmutableOrderRequest.builder()
      .symbol("AAPL")
      .qty(1)
      .side(BUY)
      .type(LIMIT)
      .timeInForce(DAY)
      .limitPrice(valueOf(-1))
      .build());
  }

  @Test
  public void specifyingNegativeStopPriceMustThrowException() {
    assertThrows(IllegalStateException.class, () ->
    ImmutableOrderRequest.builder()
      .symbol("AAPL")
      .qty(1)
      .side(BUY)
      .type(STOP)
      .timeInForce(DAY)
      .stopPrice(valueOf(-1))
      .build());
  }
}
