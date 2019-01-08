package org.brokencircuits.equationestimator.config;

import org.brokencircuits.equationestimator.dataset.Dataset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetConfig {

  @Bean
  public Dataset dataset() {
    return Dataset.getInstance();
  }
}
