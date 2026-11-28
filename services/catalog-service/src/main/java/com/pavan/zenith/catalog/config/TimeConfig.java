package com.pavan.zenith.catalog.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// The single place the real wall clock enters the system. Every use case
// receives Clock through its constructor; in production Spring hands out
// this bean, and tests hand in Clock.fixed(...) by hand instead.
@Configuration
public class TimeConfig {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
