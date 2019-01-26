package org.brokencircuits.equationestimator.domain;

import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.junit.Test;

@Slf4j
public class GenerationTest {

  private final Dataset dataset = Dataset.getInstance();
  private final int POP_SIZE = 10;

  @Test
  public void select() {

//    try {
//      dataset.setSolutionName("y");
//      dataset.loadCsv(new File("data.csv"));
//    } catch (IOException e) {
//      log.error("Unable to read csv due to error: ", e);
//    }
//
//    Generation initialGen = new Generation(dataset);
//    initialGen.generateRandomPop();
//    SortedList<Equation> eqList = initialGen.equationList();
//    for (Equation eq : eqList) {
//      log.info("Eq Fitness: {}", Generation.equationFitness(eq));
//    }
//
//    Integer[] countArray = new Integer[POP_SIZE];
//    for (int i = 0; i < POP_SIZE; i++) {
//      countArray[i] = 0;
//    }
//
//    for (int i = 0; i < 10000; i++) {
//      int selected = initialGen.select();
//      countArray[selected]++;
//    }
//
//    for (int i = 0; i < POP_SIZE; i++) {
//      log.info("selected index {} - {}x", i, countArray[i]);
//    }
  }
}