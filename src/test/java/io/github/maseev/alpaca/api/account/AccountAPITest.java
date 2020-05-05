package io.github.maseev.alpaca.api.account;

import com.google.common.net.MediaType;
import io.github.maseev.alpaca.APITest;
import io.github.maseev.alpaca.api.account.entity.Account;
import io.github.maseev.alpaca.api.account.entity.ImmutableAccount;
import io.github.maseev.alpaca.http.HttpClient;
import io.github.maseev.alpaca.http.HttpCode;
import io.github.maseev.alpaca.http.exception.APIException;
import io.github.maseev.alpaca.http.util.ContentType;
import org.junit.jupiter.api.Test;

import java.time.Month;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.github.maseev.alpaca.api.account.AccountAPI.ENDPOINT;
import static io.github.maseev.alpaca.http.json.util.JsonUtil.toJson;
import static java.math.BigDecimal.valueOf;
import static java.time.LocalDateTime.of;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class AccountAPITest extends APITest {

  @Test
  public void recevingUNonJSONResponseMustThrowException() {
    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(HttpCode.OK.getCode())
          .withBody("}{}{}{}", MediaType.JSON_UTF_8)
      );

    assertThrows(ExecutionException.class, () -> api.account().get().get());
  }

  @Test
  public void unknownHttpStatusCodeMustBeWrappedIntoKnownExceptionType() {
    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(418)
          .withReasonPhrase("I'm a teapot!")
      );

    assertThrows(ExecutionException.class, () -> api.account().get().get());
  }

  @Test
  public void sendingToManyRequestsMustThrowException() {
    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(HttpCode.TOO_MANY_REQUESTS.getCode())
          .withReasonPhrase("Rate limit exceeded")
      );

    assertThrows(ExecutionException.class, () -> api.account().get().get());
  }

  @Test
  public void gettingAccountWithIncorrectCredentialsMustThrowException() throws APIException {
    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(HttpCode.UNAUTHENTICATED.getCode())
          .withReasonPhrase("Authentication has failed")
      );

    assertThrows(ExecutionException.class, () -> api.account().get().get());
  }

  @Test
  public void gettingAccountAsyncWithIncorrectCredentialsMustThrowException() throws Exception {
    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(HttpCode.UNAUTHENTICATED.getCode())
          .withReasonPhrase("Authentication has failed")
      );

//    CountDownLatch latch = new CountDownLatch(1);
//    AtomicReference<Exception> exception = new AtomicReference<>();

//    api.account().get().onComplete(new ResponseHandler<Account>() {
//
//      @Override
//      public void onSuccess(Account result) {
//      }
//
//      @Override
//      public void onError(Exception ex) {
//        exception.set(ex);
//        latch.countDown();
//      }
//    });
//
//    latch.await(5, TimeUnit.SECONDS);

//    assertThat(exception.get().getClass(), is(equalTo(AuthenticationException.class)));
  }

  @Test
  public void gettingAccountDetailsMustReturnCorrectAccountObject() throws Exception {
    Account expectedAccount =
      ImmutableAccount.builder()
        .id(UUID.randomUUID().toString())
        .status(Account.Status.ACTIVE)
        .currency("USD")
        .buyingPower(valueOf(1))
        .cash(valueOf(2))
        .cashWithdrawable(valueOf(3))
        .portfolioValue(valueOf(4))
        .patternDayTrader(true)
        .tradingBlocked(false)
        .tradingBlocked(false)
        .transfersBlocked(false)
        .accountBlocked(false)
        .tradeSuspendedByUser(false)
        .createdAt(of(2007, Month.DECEMBER, 1, 10, 00, 10))
        .shortingEnabled(true)
        .multiplier(1)
        .longMarketValue(valueOf(1.1))
        .shortMarketValue(valueOf(2.2))
        .equity(valueOf(3.3))
        .lastEquity(valueOf(4.4))
        .initialMargin(valueOf(5.5))
        .maintenanceMargin(valueOf(6.6))
        .daytradeCount(valueOf(7.7))
        .sma(valueOf(8.8))
        .accountNumber("AP889800QWE90")
        .regtBuyingPower(valueOf(9.7))
        .dayTradingBuyingPower(valueOf(10.7))
        .build();

    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(HttpCode.OK.getCode())
          .withBody(toJson(expectedAccount), MediaType.JSON_UTF_8)
      );

    Account account = api.account().get().get();

    assertThat(account, is(equalTo(expectedAccount)));
  }

  @Test
  public void gettingAccountDetailsAsyncMustReturnCorrectAccountObject() throws Exception {
    Account expectedAccount =
      ImmutableAccount.builder()
        .id(UUID.randomUUID().toString())
        .status(Account.Status.ACTIVE)
        .currency("USD")
        .buyingPower(valueOf(1))
        .cash(valueOf(2))
        .cashWithdrawable(valueOf(3))
        .portfolioValue(valueOf(4))
        .patternDayTrader(true)
        .tradingBlocked(false)
        .tradingBlocked(false)
        .transfersBlocked(false)
        .accountBlocked(false)
        .tradeSuspendedByUser(false)
        .accountNumber("AP889800QWE90")
        .regtBuyingPower(valueOf(9.7))
        .dayTradingBuyingPower(valueOf(10.7))
        .createdAt(of(2007, Month.DECEMBER, 1, 10, 00, 10))
        .build();

    mockServer()
      .when(
        request(ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON))
      .respond(
        response()
          .withStatusCode(HttpCode.OK.getCode())
          .withBody(toJson(expectedAccount), MediaType.JSON_UTF_8)
      );

//    AtomicReference<Account> account = new AtomicReference<>();

//    api.account().get().onComplete(new ResponseHandler<Account>() {
//
//      @Override
//      public void onSuccess(Account result) {
//        account.set(result);
//        latch.countDown();
//      }
//
//      @Override
//      public void onError(Exception ex) {
//
//      }
//    });
//
//    latch.await(5, TimeUnit.SECONDS);
    assertThat(api.account().get().get(), is(equalTo(expectedAccount)));
  }
}
