package org.brokencircuits.equationestimator.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Controller implements Runnable {

  @Override
  public void run() {
    log.info("Running something");
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
