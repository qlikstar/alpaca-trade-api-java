package io.github.maseev.alpaca.http;

import io.github.maseev.alpaca.http.exception.APIException;
import io.github.maseev.alpaca.http.exception.InternalException;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

public final class Listenable<T> {

  private final Transformer<T> transformer;
  private final ListenableFuture<Response> future;

  public Listenable(Transformer<T> transformer, ListenableFuture<Response> future) {
    this.transformer = transformer;
    this.future = future;
  }

  public void onComplete(ResponseHandler<T> responseHandler) {
    future.addListener(() -> {
      try {
        responseHandler.onSuccess(transform());
      } catch (APIException ex) {
        responseHandler.onError(ex);
      }
    }, null);
  }

  public T await() throws APIException {
    return transform();
  }

  private T transform() throws APIException {
    try {
      Response response = future.get();

      return transformer.transform(response);
    } catch (Exception ex) {
      throw new InternalException(ex);
    }
  }
}
