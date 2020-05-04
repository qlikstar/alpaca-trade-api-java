package io.github.maseev.alpaca.api.streaming.exception;

import io.github.maseev.alpaca.api.streaming.Stream;

import java.util.Set;

import static java.util.Arrays.asList;

public class SubscriptionException extends Exception {

  public SubscriptionException(Set<Stream> streams) {
    super(String.format("unable to subscribe to %s streams; subscribed streams: %s",
      asList(Stream.TRADE_UPDATES, Stream.ACCOUNT_UPDATES), streams));
  }
}
