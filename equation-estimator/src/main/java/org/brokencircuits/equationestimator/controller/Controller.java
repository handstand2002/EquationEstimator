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

  final public static int POP_SIZE = 100;
  final public static int INIT_OP_NODE_COUNT = 100;
  final Evolver evolver;
  final Dataset dataset;

  @Override
  public void run() {
    try {
      dataset.setSolutionName("y");
      dataset.loadCsv(new File("data.csv"));
      log.info("\n{}", dataset.allSetContents());
    } catch (IOException e) {
      log.error("Unable to read csv due to error: ", e);
    }

//    int setId = dataset.getCurrentSetId();
//    log.info("Set ID: {}", setId);
//    Optional<Variable> solution = dataset.getSolution();
//    log.info("solution: {}", solution);
//    Optional<Variable> var = dataset.getById(1);

//    Equation eq1 = Equation.generateRandom(10);
//    Equation eq2 = Equation.generateRandom(10);
//
//    evolver.nodeExchange(eq1, eq2);

  }

}
