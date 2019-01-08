package org.brokencircuits.equationestimator.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.evolve.Evolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Controller implements Runnable {

  final Evolver evolver;
  final Dataset dataset;

  @Override
  public void run() {
//    log.info("Random Seed: {}", Chance.RAND_SEED);
//
//    Equation eq = Equation.generateRandom(10);
//    eq.simplify();
//
//    Equation eq2 = Equation.generateRandom(10);


  }

}
