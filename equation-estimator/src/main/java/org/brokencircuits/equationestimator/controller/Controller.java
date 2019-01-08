package org.brokencircuits.equationestimator.controller;


import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.Equation;
import org.brokencircuits.equationestimator.util.Chance;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Controller implements Runnable {

  @Override
  public void run() {
    log.info("Random Seed: {}", Chance.RAND_SEED);

    Equation eq = Equation.generateRandom(10);
    eq.simplify();

    Equation eq2 = Equation.generateRandom(10);


  }

}
