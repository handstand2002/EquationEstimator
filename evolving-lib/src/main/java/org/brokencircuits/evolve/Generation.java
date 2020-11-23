package org.brokencircuits.evolve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.evolve.exception.InvalidFitnessEvaluation;
import org.jetbrains.annotations.NotNull;

@Slf4j
@Getter
public class Generation<T extends AttributeType> {

  private final List<Individual<T>> individuals;
  private final EvolutionaryParameters<T> args;
  private final boolean isComplete;
  private final long generationNumber;

  /**
   * Used by controller to instantiate brand new random individuals
   */
  public Generation(int numIndividuals, EvolutionaryParameters<T> args) {
    this.args = args;
    individuals = new ArrayList<>(numIndividuals);
    for (int i = 0; i < numIndividuals; i++) {
      Individual<T> newIndividual = args.getGenerator().create();
      individuals.add(newIndividual);
    }
    isComplete = false;
    generationNumber = 0;
  }

  /**
   * Used in {@link Generation#newGeneration(int)} to instantiate a new generation with a given
   * population
   */
  private Generation(List<Individual<T>> withIndividuals, EvolutionaryParameters<T> args,
      boolean isComplete, long generationNumber) {
    individuals = withIndividuals;
    this.args = args;
    this.isComplete = isComplete;
    this.generationNumber = generationNumber;
  }

  @NotNull
  public Generation<T> newGeneration(int elites) {

    List<Individual<T>> newGenIndividuals = new ArrayList<>(individuals.size());
    log.trace("{} Elite individuals chosen to pass into the next generation", elites);
    for (int i = 0; i < elites; i++) {
      Individual<T> elite = individuals.get(i);
      newGenIndividuals.add(elite);
      log.trace("Elite individual: {}", elite);
    }

    log.trace("Performing selection, gene exchange, and mutation");
    while (newGenIndividuals.size() < individuals.size()) {

      // Pair up individuals of a generation, preferring the higher-fitness individuals
      Individual<T> selectedOne = args.getSelector().select(individuals);
      Individual<T> selectedTwo = args.getSelector().select(individuals);

      log.trace("Selected individuals {}; {}", selectedOne, selectedTwo);

      // create "genetically identical" children from the individuals
      Individual<T> child1 = selectedOne.clone();
      Individual<T> child2 = selectedTwo.clone();

      // do some gene exchange
      args.getSwapper().apply(child1, child2);
      log.trace("Children after gene swap: {}; {}", child1, child2);

      // apply some mutation
      args.getMutator().apply(child1);
      args.getMutator().apply(child2);
      log.trace("Children after mutation: {}; {}", child1, child2);

      newGenIndividuals.add(child1);
      if (newGenIndividuals.size() < individuals.size()) {
        newGenIndividuals.add(child2);
      } else {
        log.trace("Not adding child2, generation is full: {}", child2);
      }

      log.trace("Generation size after adding children: {}", newGenIndividuals.size());
    }

    List<Individual<T>> scoredIndividuals = newGenIndividuals.stream()
        .peek(i -> {
          double fitness = args.getEvaluator().evaluate(i);
          if (fitness < 0) {
            throw new InvalidFitnessEvaluation(
                "Fitness must be greater than or equal to 0, with 0 being a perfect score");
          }
          i.setFitness(fitness);
        })
        .sorted(Comparator.comparing(Individual::getFitness))
        .collect(Collectors.toList());

    log.debug("Highest scoring individual from generation [{}]: {}", generationNumber,
        scoredIndividuals.get(0));

    boolean isComplete = args.getCompletionPredicate().isComplete(scoredIndividuals);
    return new Generation<>(scoredIndividuals, args, isComplete, generationNumber + 1);
  }
}
