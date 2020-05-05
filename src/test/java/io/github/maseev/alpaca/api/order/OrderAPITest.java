package io.github.maseev.alpaca.api.order;

import com.google.common.net.MediaType;
import io.github.maseev.alpaca.APITest;
import io.github.maseev.alpaca.api.order.entity.ImmutableOrder;
import io.github.maseev.alpaca.api.order.entity.ImmutableOrderRequest;
import io.github.maseev.alpaca.api.order.entity.Order;
import io.github.maseev.alpaca.api.order.entity.OrderRequest;
import io.github.maseev.alpaca.http.HttpClient;
import io.github.maseev.alpaca.http.HttpCode;
import io.github.maseev.alpaca.http.exception.APIException;
import io.github.maseev.alpaca.http.exception.EntityNotFoundException;
import io.github.maseev.alpaca.http.exception.ForbiddenException;
import io.github.maseev.alpaca.http.util.ContentType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.github.maseev.alpaca.api.asset.entity.AssetClass.US_EQUITY;
import static io.github.maseev.alpaca.http.json.util.JsonUtil.toJson;
import static java.time.LocalDateTime.of;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class OrderAPITest extends APITest {

  @Test
  public void gettingFilteredListOfOrdersMustReturnExpectedList() throws Exception {
    OrderAPI.Status status = OrderAPI.Status.OPEN;
    int limit = 10;
    LocalDateTime after = of(2007, Month.DECEMBER, 1, 10, 00, 10);
    LocalDateTime until = of(2009, Month.DECEMBER, 1, 10, 00, 10);
    OrderAPI.Direction direction = OrderAPI.Direction.ASC;

    LocalDateTime orderDate = of(2008, Month.JULY, 9, 12, 30, 00);

    Order expectedOrder =
      ImmutableOrder.builder()
        .id(UUID.randomUUID().toString())
        .clientOrderId(UUID.randomUUID().toString())
        .createdAt(orderDate)
        .updatedAt(orderDate)
        .submittedAt(orderDate)
        .filledAt(orderDate)
        .expiredAt(orderDate)
        .canceledAt(orderDate)
        .failedAt(orderDate)
        .assetId(UUID.randomUUID().toString())
        .symbol("AAPL")
        .assetClass(US_EQUITY)
        .qty(1)
        .filledQty(2)
        .type(Order.Type.MARKET)
        .orderType(Order.Type.MARKET)
        .side(Order.Side.BUY)
        .timeInForce(Order.TimeInForce.DAY)
        .limitPrice(BigDecimal.valueOf(3))
        .stopPrice(BigDecimal.valueOf(4))
        .filledAvgPrice(BigDecimal.valueOf(5))
        .status(Order.Status.FILLED)
        .extendedHours(true)
        .build();

    List<Order> expectedOrders = singletonList(expectedOrder);

    mockServer()
      .when(
        request(OrderAPI.ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
          .withQueryStringParameter("status", status.toString())
          .withQueryStringParameter("limit", Integer.toString(limit))
          .withQueryStringParameter("after", OrderAPI.PATTERN.format(after))
          .withQueryStringParameter("until", OrderAPI.PATTERN.format(until))
          .withQueryStringParameter("direction", direction.toString()))
      .respond(
        response()
          .withStatusCode(HttpCode.OK.getCode())
          .withBody(toJson(expectedOrders), MediaType.JSON_UTF_8));

    List<Order> orders =
      api.orders()
        .get(status, limit, after, until, direction)
        .await();

    assertThat(orders, is(equalTo(expectedOrders)));
  }

  @Test
  public void cancellingNoLongerCancelableOrderMustThrowException() throws APIException {
    String orderId = UUID.randomUUID().toString();

    setUpMockServer(orderId, HttpCode.UNPROCESSABLE,
      "The order status is not cancelable");

    // assertThrows(UnprocessableException.class, () -> api.orders().cancel(orderId).get());
  }

  @Test
  public void cancellingNonexistentOrderMustThrowException() throws APIException {
    String orderId = UUID.randomUUID().toString();

    setUpMockServer(orderId, HttpCode.NOT_FOUND,
      "The order doesn't exist");

    // assertThrows(EntityNotFoundException.class, () -> api.orders().cancel(orderId).get());
  }

  @Test
  public void cancellingValidOrderMustCancelIt() throws APIException, ExecutionException, InterruptedException {
    String orderId = UUID.randomUUID().toString();

    setUpMockServer(orderId, HttpCode.NO_CONTENT,
      "The order has been cancelled");

    api.orders().cancel(orderId).get();
  }

  @Test
  public void gettingNonExistentOrderMustThrowException() throws APIException {
    String orderId = UUID.randomUUID().toString();

    mockServer().when(
      request(OrderAPI.ENDPOINT + '/' + orderId)
        .withMethod(HttpClient.HttpMethod.GET.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
        .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
    ).respond(
      response()
        .withStatusCode(HttpCode.NOT_FOUND.getCode())
        .withReasonPhrase("Order not found")
    );

    assertThrows(EntityNotFoundException.class, () -> api.orders().get(orderId).await());
  }

  @Test
  public void gettingExistentOrderMustReturnExpectedOrder() throws Exception {
    String orderId = UUID.randomUUID().toString();

    LocalDateTime orderDate = of(2008, Month.JULY, 9, 12, 30, 00);

    Order expectedOrder = ImmutableOrder.builder()
      .id(orderId)
      .clientOrderId(UUID.randomUUID().toString())
      .createdAt(orderDate)
      .updatedAt(orderDate)
      .submittedAt(orderDate)
      .filledAt(orderDate)
      .expiredAt(orderDate)
      .canceledAt(orderDate)
      .failedAt(orderDate)
      .assetId(UUID.randomUUID().toString())
      .symbol("AAPL")
      .assetClass(US_EQUITY)
      .qty(1)
      .filledQty(2)
      .type(Order.Type.MARKET)
      .orderType(Order.Type.MARKET)
      .side(Order.Side.BUY)
      .timeInForce(Order.TimeInForce.DAY)
      .limitPrice(BigDecimal.valueOf(3))
      .stopPrice(BigDecimal.valueOf(4))
      .filledAvgPrice(BigDecimal.valueOf(5))
      .status(Order.Status.FILLED)
      .extendedHours(true)
      .build();

    mockServer().when(
      request(OrderAPI.ENDPOINT + '/' + orderId)
        .withMethod(HttpClient.HttpMethod.GET.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
        .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
    ).respond(
      response()
        .withStatusCode(HttpCode.OK.getCode())
        .withBody(toJson(expectedOrder), MediaType.JSON_UTF_8)
    );

    Order order = api.orders().get(orderId).await();

    assertThat(order, is(equalTo(expectedOrder)));
  }

  @Test
  public void gettingNonExistentOrderByClientIdMustThrowException() throws APIException {
    String clientOrderId = UUID.randomUUID().toString();

    mockServer().when(
      request(OrderAPI.GET_BY_CLIENT_ORDER_ID_ENDPOINT)
        .withMethod(HttpClient.HttpMethod.GET.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
        .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
        .withQueryStringParameter("client_order_id", clientOrderId)
    ).respond(
      response()
        .withStatusCode(HttpCode.NOT_FOUND.getCode())
        .withReasonPhrase("Order not found")
    );

    assertThrows(EntityNotFoundException.class,
      () -> api.orders().getByClientOrderId(clientOrderId).await());
  }

  @Test
  public void gettingExistentOrderByClientIdMustReturnExpectedOrder() throws Exception {
    String clientOrderId = UUID.randomUUID().toString();

    LocalDateTime orderDate = of(2008, Month.JULY, 9, 12, 30, 00);

    Order expectedOrder =
      ImmutableOrder.builder()
        .id(UUID.randomUUID().toString())
        .clientOrderId(clientOrderId)
        .createdAt(orderDate)
        .updatedAt(orderDate)
        .submittedAt(orderDate)
        .filledAt(orderDate)
        .expiredAt(orderDate)
        .canceledAt(orderDate)
        .failedAt(orderDate)
        .assetId(UUID.randomUUID().toString())
        .symbol("AAPL")
        .assetClass(US_EQUITY)
        .qty(1)
        .filledQty(2)
        .type(Order.Type.MARKET)
        .orderType(Order.Type.MARKET)
        .side(Order.Side.BUY)
        .timeInForce(Order.TimeInForce.DAY)
        .limitPrice(BigDecimal.valueOf(3))
        .stopPrice(BigDecimal.valueOf(4))
        .filledAvgPrice(BigDecimal.valueOf(5))
        .status(Order.Status.FILLED)
        .extendedHours(true)
        .build();

    mockServer().when(
      request(OrderAPI.GET_BY_CLIENT_ORDER_ID_ENDPOINT)
        .withMethod(HttpClient.HttpMethod.GET.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
        .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
        .withQueryStringParameter("client_order_id", clientOrderId)
    ).respond(
      response()
        .withStatusCode(HttpCode.OK.getCode())
        .withBody(toJson(expectedOrder), MediaType.JSON_UTF_8)
    );

    Order order = api.orders().getByClientOrderId(clientOrderId).await();

    assertThat(order, is(equalTo(expectedOrder)));
  }

  @Test
  public void placingNewOrderRequestMustReturnOrderWithExpectedParameters() throws Exception {
    OrderRequest orderRequest =
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(1)
        .side(Order.Side.BUY)
        .type(Order.Type.STOP_LIMIT)
        .timeInForce(Order.TimeInForce.DAY)
        .limitPrice(BigDecimal.valueOf(10))
        .stopPrice(BigDecimal.valueOf(5))
        .clientOrderId(UUID.randomUUID().toString())
        .build();

    LocalDateTime orderDate = of(2008, Month.JULY, 9, 12, 30, 00);

    Order expectedOrder =
      ImmutableOrder.builder()
        .id(UUID.randomUUID().toString())
        .clientOrderId(orderRequest.clientOrderId())
        .createdAt(orderDate)
        .updatedAt(orderDate)
        .submittedAt(orderDate)
        .filledAt(orderDate)
        .expiredAt(orderDate)
        .canceledAt(orderDate)
        .failedAt(orderDate)
        .assetId(UUID.randomUUID().toString())
        .symbol(orderRequest.symbol())
        .assetClass(US_EQUITY)
        .qty(orderRequest.qty())
        .filledQty(orderRequest.qty())
        .type(orderRequest.type())
        .orderType(orderRequest.type())
        .side(orderRequest.side())
        .timeInForce(orderRequest.timeInForce())
        .limitPrice(orderRequest.limitPrice())
        .stopPrice(orderRequest.stopPrice())
        .filledAvgPrice(BigDecimal.valueOf(5))
        .status(Order.Status.FILLED)
        .extendedHours(true)
        .build();

    mockServer().when(
      request(OrderAPI.ENDPOINT)
        .withMethod(HttpClient.HttpMethod.POST.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
        .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
        .withBody(toJson(orderRequest))
    ).respond(
      response()
        .withStatusCode(HttpCode.OK.getCode())
        .withBody(toJson(expectedOrder), MediaType.JSON_UTF_8)
    );

    Order order = api.orders().place(orderRequest).await();

    assertThat(order, is(equalTo(expectedOrder)));
  }

  @Test
  public void placingNewOrderWithInsufficientByingPowerMustThrowException() throws Exception {
    OrderRequest request =
      ImmutableOrderRequest.builder()
        .symbol("AAPL")
        .qty(1)
        .side(Order.Side.BUY)
        .type(Order.Type.STOP_LIMIT)
        .timeInForce(Order.TimeInForce.DAY)
        .limitPrice(BigDecimal.valueOf(10))
        .stopPrice(BigDecimal.valueOf(5))
        .clientOrderId(UUID.randomUUID().toString())
        .extendedHours(false)
        .build();

    mockServer().when(
      request(OrderAPI.ENDPOINT)
        .withMethod(HttpClient.HttpMethod.POST.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
        .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
        .withBody(toJson(request))
    ).respond(
      response()
        .withStatusCode(HttpCode.FORBIDDEN.getCode())
        .withReasonPhrase("Buying power is not sufficient")
    );

    assertThrows(ForbiddenException.class, () -> api.orders().place(request).await());
  }

  private void setUpMockServer(String expectedOrderId,
                               HttpCode expectedStatusCode,
                               String expectedMessage) {
    mockServer().when(
      request(OrderAPI.ENDPOINT + '/' + expectedOrderId)
        .withMethod(HttpClient.HttpMethod.DELETE.toString())
        .withHeader(APCA_API_KEY_ID, keyId)
        .withHeader(APCA_API_SECRET_KEY, secretKey)
    ).respond(
      response()
        .withStatusCode(expectedStatusCode.getCode())
        .withReasonPhrase(expectedMessage)
    );
  }
}
