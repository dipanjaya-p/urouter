package com.utt.urouter.config;

import com.utt.urouter.model.JwtToken;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class AppConfig {

  @Bean("tokenCache")
  public Map<String, JwtToken> sseEmitterCache() {
    return new ConcurrentHashMap<>();
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {

    return builder
        .setConnectTimeout(Duration.ofMillis(10000))
        .setReadTimeout(Duration.ofMillis(10000))
        .build();
  }
}
