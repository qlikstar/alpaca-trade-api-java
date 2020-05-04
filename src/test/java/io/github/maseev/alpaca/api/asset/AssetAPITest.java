package io.github.maseev.alpaca.api.asset;

import com.google.common.net.MediaType;
import io.github.maseev.alpaca.APITest;
import io.github.maseev.alpaca.api.asset.entity.Asset;
import io.github.maseev.alpaca.api.asset.entity.AssetClass;
import io.github.maseev.alpaca.api.asset.entity.ImmutableAsset;
import io.github.maseev.alpaca.api.entity.Exchange;
import io.github.maseev.alpaca.http.HttpClient;
import io.github.maseev.alpaca.http.HttpCode;
import io.github.maseev.alpaca.http.exception.APIException;
import io.github.maseev.alpaca.http.exception.EntityNotFoundException;
import io.github.maseev.alpaca.http.util.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.github.maseev.alpaca.http.json.util.JsonUtil.toJson;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class AssetAPITest extends APITest {

  @Test
  public void gettingListOfAssetsMustReturnExpectedAssets() throws Exception {
    AssetClass assetClass = AssetClass.US_EQUITY;
    Asset.Status status = Asset.Status.ACTIVE;
    Asset expectedAsset =
      ImmutableAsset.builder()
        .id(UUID.randomUUID().toString())
        .assetClass(assetClass)
        .exchange(Exchange.NYSE)
        .symbol("AAPL")
        .status(status)
        .tradable(true)
        .marginable(true)
        .shortable(true)
        .easyToBorrow(true)
        .build();

    List<Asset> expectedAssets = Collections.singletonList(expectedAsset);

    mockServer()
      .when(
        request(AssetAPI.ENDPOINT)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
          .withQueryStringParameter("status", status.toString())
          .withQueryStringParameter("asset_class", assetClass.toString())
      )
      .respond(
        response()
          .withStatusCode(HttpCode.OK.getCode())
          .withBody(toJson(expectedAssets), MediaType.JSON_UTF_8)
      );

    List<Asset> assets = api.assets().get(status, assetClass).await();

    assertThat(assets, is(equalTo(expectedAssets)));
  }

  @Test
  public void gettingNonExistentAssetMustThrowException() throws APIException {
    String symbol = "AAPL";

    mockServer()
      .when(
        request(AssetAPI.ENDPOINT + '/' + symbol)
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
      )
      .respond(
        response()
          .withStatusCode(HttpCode.NOT_FOUND.getCode())
          .withReasonPhrase("Asset not found")
      );

    assertThrows(EntityNotFoundException.class, () -> api.assets().get(symbol).await());
  }

  @Test
  public void gettingExistentAssetMustReturnExpectedAsset() throws Exception {
    Asset expectedAsset =
      ImmutableAsset.builder()
        .id(UUID.randomUUID().toString())
        .assetClass(AssetClass.US_EQUITY)
        .exchange(Exchange.NYSE)
        .symbol("AAPL")
        .status(Asset.Status.ACTIVE)
        .tradable(true)
        .build();

    mockServer()
      .when(
        request(AssetAPI.ENDPOINT + '/' + expectedAsset.symbol())
          .withMethod(HttpClient.HttpMethod.GET.toString())
          .withHeader(APCA_API_KEY_ID, keyId)
          .withHeader(APCA_API_SECRET_KEY, secretKey)
          .withHeader(ContentType.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON)
      )
      .respond(
        response()
          .withStatusCode(HttpCode.OK.getCode())
          .withBody(toJson(expectedAsset), MediaType.JSON_UTF_8)
      );

    Asset asset = api.assets().get(expectedAsset.symbol()).await();

    assertThat(asset, is(equalTo(expectedAsset)));
  }
}
