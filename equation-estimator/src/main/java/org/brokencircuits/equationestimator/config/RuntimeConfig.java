package org.brokencircuits.equationestimator.config;

import org.brokencircuits.equationestimator.controller.Controller;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class RuntimeConfig {

  @Bean
  public TaskExecutor executor() {
    return new SimpleAsyncTaskExecutor();
  }

  @Bean
  public CommandLineRunner startupController(TaskExecutor executor, Controller controller) {
    return args -> executor.execute(controller);
  }

}
