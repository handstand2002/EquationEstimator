package org.brokencircuits.equationestimator.domain;

import com.scottlogic.util.SortedList;
import java.util.Comparator;
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

  private final Dataset dataset = Dataset.getInstance();
  private final Evolver evolver = Evolver.getInstance();
  private SortedList<Equation> equationList = new SortedList<>(
      Comparator.comparingDouble(Equation::getLastFitness));

  public static double equationFitness(Equation eq) {
    Dataset dataset = Dataset.getInstance();
    List<Integer> datasetIdList = dataset.getIdList();

    double fitness = 0D;
    for (Integer datasetId : datasetIdList) {
      dataset.setCurrentSetId(datasetId);
      double diff = Math.abs(eq.eval() - dataset.getCurrentSetSolution());

      double fitnessThisSet = Math.pow(diff + 1, 2);
      fitness += fitnessThisSet;
    }
    fitness -= datasetIdList.size();    // we added +1 to each solution difference

    // if there are more than 500 nodes, make it a "less fit" solution
    long numNodesInEq = eq.statistic().getNumDescendant();
    if (numNodesInEq > 500) {
      fitness += (numNodesInEq - 500);
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

    log.error("Unable to select an equation");
    return null;
  }

  public Generation generateNext() {
    Generation newGen = new Generation();
    int numElites = (int) Math.floor(Controller.ELITISM * Controller.POP_SIZE);

    for (int i = 0; i < numElites; i++) {
      Equation elite = this.equationList.get(i).clone();
      newGen.addEquation(elite);
    }

    while (newGen.equationList.size() < Controller.POP_SIZE) {
      Equation selectOne = this.select();
      Equation selectTwo = this.select();

      List<Equation> childList = evolver.nodeExchange(selectOne, selectTwo);
      childList.forEach(evolver::equationMutate);   // mutate each child

      newGen.addEquation(childList.get(0));
      if (newGen.equationList.size() < Controller.POP_SIZE) {
        newGen.addEquation(childList.get(1));
      }
    }

    return newGen;
  }
}
