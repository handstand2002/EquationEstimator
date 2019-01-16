package org.brokencircuits.equationestimator.controller;


import java.io.File;
import java.io.IOException;
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
    try {
      dataset.loadCsv(new File("data.csv"));
      log.info("\n{}", dataset.allSetContents());
    } catch (IOException e) {
      log.error("Unable to read csv due to error: ", e);
    }

//    log.info("Random Seed: {}", Chance.RAND_SEED);
//
//    Equation eq = Equation.generateRandom(10);
//    eq.simplify();
//
//    Equation eq2 = Equation.generateRandom(10);


  }

}
