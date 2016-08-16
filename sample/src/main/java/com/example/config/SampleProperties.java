package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@RefreshScope
@Component
public class SampleProperties {

  @Value("${sample.index}")
  String index;

  @Value("${sample.secret}")
  String secret;

  public String getIndex() {
    return index;
  }

  public String getSecret() {
    return secret;
  }

}
