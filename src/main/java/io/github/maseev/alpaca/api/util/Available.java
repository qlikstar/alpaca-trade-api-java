package io.github.maseev.alpaca.api.util;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Available {
  enum Version {
    V1, V2
  }

  Version in();
}
