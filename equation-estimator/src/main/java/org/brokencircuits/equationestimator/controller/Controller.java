package org.brokencircuits.equationestimator.controller;


import com.scottlogic.util.SortedList;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.domain.Equation;
import org.brokencircuits.equationestimator.domain.Generation;
import org.brokencircuits.equationestimator.evolve.Evolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Controller implements Runnable {

  final public static int POP_SIZE = 10;
  final public static int INIT_OP_NODE_COUNT = 5;
  final public static double ELITISM = 0.0;    // percent of elites to retain
  final private Evolver evolver = Evolver.getInstance();
  final private Dataset dataset = Dataset.getInstance();

  @Override
  public void run() {
    try {
      dataset.setSolutionName("y");
      dataset.loadCsv(new File("data.csv"));
    } catch (IOException e) {
      log.error("Unable to read csv due to error: ", e);
    }

    Generation initialGen = new Generation();
    initialGen.generateRandomPop();
    SortedList<Equation> eqList = initialGen.equationList();
    for (Equation eq : eqList) {
      log.info("Eq Fitness: {}", eq.getLastFitness());
    }
    log.info("");

    Generation newGen = initialGen.generateNext();
    newGen.equationList()
        .forEach(equation -> log.info("Eq fitness: {}", equation.getLastFitness()));
    log.info("");

  }

}
