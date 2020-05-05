package io.github.maseev.alpaca.api.order;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.maseev.alpaca.api.order.entity.Order;
import io.github.maseev.alpaca.api.order.entity.OrderRequest;
import io.github.maseev.alpaca.api.streaming.StreamingAPI;
import io.github.maseev.alpaca.http.HttpClient;
import io.github.maseev.alpaca.http.Listenable;
import io.github.maseev.alpaca.http.exception.APIException;
import io.github.maseev.alpaca.http.exception.EntityNotFoundException;
import io.github.maseev.alpaca.http.exception.ForbiddenException;
import io.github.maseev.alpaca.http.exception.UnprocessableException;
import io.github.maseev.alpaca.http.json.util.DateFormatUtil;
import io.github.maseev.alpaca.http.transformer.GenericTransformer;
import io.github.maseev.alpaca.http.transformer.ValueTransformer;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static io.github.maseev.alpaca.http.json.util.JsonUtil.toJson;
import static io.github.maseev.alpaca.http.util.StringUtil.requireNonEmpty;
import static java.lang.String.format;

/**
 * The orders API allows a user to monitor, place and cancel their orders with Alpaca. Each order
 * has a unique identifier provided by the client. This client-side unique order ID will be
 * automatically generated by the system if not provided by the client and will be returned as part
 * of the order object along with the rest of the fields described below. Once an order is placed,
 * it can be queried using the client-side order ID to check the status. Updates on open orders at
 * Alpaca will also be sent over the {@link StreamingAPI
 * streaming interface}, which is the recommended method of maintaining order state.
 */
public class OrderAPI {

  public enum Status {
    OPEN,
    CLOSED,
    ALL;

    @Override
    @JsonValue
    public String toString() {
      return name().toLowerCase();
    }
  }

  public enum Direction {
    ASC,
    DESC;

    @Override
    @JsonValue
    public String toString() {
      return name().toLowerCase();
    }
  }

  static final String ENDPOINT = "/orders";
  static final String GET_BY_CLIENT_ORDER_ID_ENDPOINT =
    ENDPOINT + ":by_client_order_id";

  static final DateTimeFormatter PATTERN =
    DateTimeFormatter.ofPattern(DateFormatUtil.DATE_TIME_NO_NANOSECONDS_FORMAT);

  private final HttpClient httpClient;

  public OrderAPI(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  /**
   * Retrieves a list of orders for the account, filtered by the supplied query parameters.
   *
   * @param status    Order status to be queried
   * @param limit     The maximum number of orders in response. Min is 1, max is 500
   * @param after     The response will include only ones submitted after this timestamp
   *                  (exclusive)
   * @param until     The response will include only ones submitted until this timestamp
   *                  (exclusive)
   * @param direction The chronological order of response based on the submission time
   * @return a list of {@link Order} for the account
   */
  public Listenable<List<Order>> get(Status status, int limit, LocalDateTime after,
                                     LocalDateTime until,
                                     Direction direction) {
    validate(limit, after, until);

    ListenableFuture<Response> future =
      httpClient.prepare(HttpClient.HttpMethod.GET, ENDPOINT)
        .addQueryParam("status", status.toString())
        .addQueryParam("limit", Integer.toString(limit))
        .addQueryParam("after", PATTERN.format(after))
        .addQueryParam("until", PATTERN.format(until))
        .addQueryParam("direction", direction.toString())
        .execute();

    return new Listenable<>(new GenericTransformer<>(new TypeReference<List<Order>>() {}), future);
  }

  /**
   * Places a new order for the given account. An order request may be rejected if the account is
   * not authorized for trading, or if the tradable balance is insufficient to fill the order.
   *
   * @param request a {@link OrderRequest} object which consists of information about order's
   *                details
   * @return a new {@link Order} object
   * @throws JsonProcessingException in case JSON serialization fails
   * @throws ForbiddenException      if the buying power of shares is not sufficient
   * @throws UnprocessableException  if the input parameters are not recognized
   */
  public Listenable<Order> place(OrderRequest request) throws JsonProcessingException {
    ListenableFuture<Response> future =
      httpClient.prepare(HttpClient.HttpMethod.POST, ENDPOINT)
        .setBody(toJson(request))
        .execute();

    return new Listenable<>(new ValueTransformer<>(Order.class), future);
  }

  /**
   * Retrieves a single order for the given order id.
   *
   * @param orderId an {@link Order#id() Order's id}
   * @return The requested {@link Order} object
   * @throws EntityNotFoundException if an Order is not found
   */
  public Listenable<Order> get(String orderId) {
    requireNonEmpty(orderId, "orderId");

    ListenableFuture<Response> future =
      httpClient.prepare(HttpClient.HttpMethod.GET, ENDPOINT, orderId).execute();

    return new Listenable<>(new ValueTransformer<>(Order.class), future);
  }

  /**
   * Retrieves a single order for the given client order id.
   *
   * @param clientOrderId an {@link Order#clientOrderId() Order's clientOrderId}
   * @return The requested {@link Order} object
   * @throws EntityNotFoundException if an Order is not found
   */
  public Listenable<Order> getByClientOrderId(String clientOrderId) {
    requireNonEmpty(clientOrderId, "clientOrderId");

    ListenableFuture<Response> future =
      httpClient.prepare(HttpClient.HttpMethod.GET, GET_BY_CLIENT_ORDER_ID_ENDPOINT)
        .addQueryParam("client_order_id", clientOrderId)
        .execute();

    return new Listenable<>(new ValueTransformer<>(Order.class), future);
  }

  /**
   * Attempts to cancel an open order.
   *
   * @param orderId an {@link Order#id() Order's id}
   * @throws EntityNotFoundException if an Order is not found
   * @throws UnprocessableException  if an Order is no longer cancelable (e.g. {@link
   *                                 Order.Status#FILLED})
   */
  public CompletableFuture<Void> cancel(String orderId) {
    requireNonEmpty(orderId, "orderId");

    ListenableFuture<Response> future =
      httpClient.prepare(HttpClient.HttpMethod.DELETE, ENDPOINT, orderId).execute();

    return future.toCompletableFuture().thenApply( x-> {
      try {
        return new ValueTransformer<>(Void.class).transform(x.getResponseBody());
      } catch (APIException | IOException e) {
        throw new CompletionException(e);
      }
    });

  }

  private static void validate(int limit, LocalDateTime after, LocalDateTime until) {
    final int MAX_NUMBER_OF_ORDERS = 500;

    if (limit <= 0 || limit > MAX_NUMBER_OF_ORDERS) {
      throw new IllegalArgumentException(
        format("'limit' parameter must be greater than 0 and less than %s; limit: %s",
          MAX_NUMBER_OF_ORDERS, limit));
    }

    if (after.isAfter(until)) {
      throw new IllegalArgumentException(
        format("'after' parameter must be before 'until'; after: %s, until: %s", after, until));
    }
  }
}