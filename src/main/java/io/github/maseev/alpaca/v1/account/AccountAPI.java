package io.github.maseev.alpaca.v1.account;

import io.github.maseev.alpaca.http.HttpClient;
import io.github.maseev.alpaca.http.exception.APIException;
import io.github.maseev.alpaca.http.transformer.ValueTransformer;
import io.github.maseev.alpaca.v1.account.entity.Account;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static io.github.maseev.alpaca.http.HttpClient.HttpMethod.GET;

/**
 * The accounts API serves important information related to an account, including account status,
 * funds available for trade, funds available for withdrawal, and various flags relevant to an
 * account’s ability to trade. An account may be blocked for just for trades ({@link
 * Account#tradingBlocked()}) or for both trades and transfers ({@link Account#accountBlocked()}) if
 * Alpaca identifies the account to be engaged in any suspicious activity. Also, in accordance with
 * FINRA’s pattern day trading rule, an account may be flagged for pattern day trading ({@link
 * Account#patternDayTrader()}), which would inhibit an account from placing any further
 * day-trades.
 */
public class AccountAPI {

  static final String ENDPOINT = "/account";

  private final HttpClient httpClient;

  public AccountAPI(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Returns the account associated with the API key.
   *
   * @return the {@link Account} associated with the API key
   */
  public CompletableFuture<Account> get() {
    ListenableFuture<Response> future = httpClient.prepare(GET, ENDPOINT).execute();

    return future.toCompletableFuture().thenApply( x-> {
      try {
        return new ValueTransformer<>(Account.class).transform(x.getResponseBody());
      } catch (APIException | IOException e) {
        throw new CompletionException(e);
      }
    });
  }
}
