package org.brokencircuits.equationestimator.domain;

import com.scottlogic.util.SortedList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.controller.Controller;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.evolve.Evolver;
import org.brokencircuits.equationestimator.util.Chance;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class Generation {

  private final Double MAX_WORST = Math.sqrt(Double.MAX_VALUE) - 1;
  private final Dataset dataset = Dataset.getInstance();
  private final Evolver evolver = Evolver.getInstance();
  private SortedList<Equation> equationList = new SortedList<>(
      Comparator.comparingDouble(Equation::getLastFitness));

  public static double evaluateEquation(Equation eq) {
    Dataset dataset = Dataset.getInstance();
    List<Integer> datasetIdList = dataset.getIdList();

    double fitness = 0D;
    for (Integer datasetId : datasetIdList) {
      dataset.setCurrentSetId(datasetId);
      double fitnessThisSet = Math.pow(eq.eval() - dataset.getCurrentSetSolution(), 2);
      fitness += fitnessThisSet;
    }

    eq.setLastFitness(fitness);
    return fitness;
  }

  private void addEquation(Equation eq) {
    equationList.add(eq);
  }

  public void generateRandomPop() {
    for (int i = 0; i < Controller.POP_SIZE; i++) {
      addEquation(Equation.generateRandom(Controller.INIT_OP_NODE_COUNT));
    }
  }

  public SortedList<Equation> equationList() {
    return equationList;
  }

  public Equation select() {
    int popSize = equationList.size();
    int randMax = popSize * (popSize + 1) / 2 + 1;
    int randomNumber = Chance.RAND.nextInt(randMax);

    for (int i = 0; i < equationList.size(); i++) {
      int diff = (popSize - i);
      randomNumber -= diff;
      if (randomNumber <= 0) {
        return equationList.get(i);
      }
    }

    return null;
  }

  public Generation generateNext() {
    Generation newGen = new Generation();
    int numElites = (int) Math.floor(Controller.ELITISM * Controller.POP_SIZE);
    Iterator<Equation> eqIterator = this.equationList.iterator();
    for (int i = 0; i < numElites; i++) {
      newGen.addEquation(eqIterator.next());
    }

    while (newGen.equationList.size() < Controller.POP_SIZE) {
      Equation selectOne = this.select();
      Equation selectTwo = this.select();

      List<Equation> childList = evolver.nodeExchange(selectOne, selectTwo);
      // TODO: add mutation
      newGen.addEquation(childList.get(0));
      if (newGen.equationList.size() < Controller.POP_SIZE) {
        newGen.addEquation(childList.get(1));
      }
    }

    return newGen;
  }
}
