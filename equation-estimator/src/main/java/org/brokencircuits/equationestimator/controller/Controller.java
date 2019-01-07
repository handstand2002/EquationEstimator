package org.brokencircuits.equationestimator.controller;


import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.Equation;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Controller implements Runnable {

  @Override
  public void run() {
    log.info("Running something");

    Equation eq = Equation.generateRandom(10);
    double origEval = eq.eval();
    log.info("Tree:\n{}", eq.equationTree());
    log.info("Eval: {}", origEval);

  }

}
