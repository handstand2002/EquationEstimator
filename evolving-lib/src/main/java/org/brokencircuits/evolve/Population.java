package org.brokencircuits.evolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class Population<T extends AttributeType<T>> {

  private final EvolutionaryParameters<T> args;
  private final Map<Long, Double> fitnessChangeByGeneration = new LinkedHashMap<>();
  private Double lastCollectedFitness = null;
  private Long lastGenerationStatsCaptured = null;
  private Individual<T> bestIndividual = null;
  @Getter
  private Generation<T> currentGeneration;

  public Population(EvolutionaryParameters<T> args) {
    this.args = args;
    currentGeneration = createGeneration(args.getNumIndividuals());
  }

  public void advanceGeneration(long genNum, Collection<T> migrated) {

    HistoryMetadata metadata = HistoryMetadata.builder()
        .currentFitness(genNum == 0 ? null : currentGeneration.getIndividuals().get(0).getFitness())
        .currentGen(genNum)
        .fitnessChangeByGeneration(fitnessChangeByGeneration)
        .genInterval(args.getGenerationStatisticParameters().getGenerationInterval())
        .maxGenerations(args.getNumGenerations())
        .lastCapturedGen(lastGenerationStatsCaptured)
        .build();

    Generation<T> lastGeneration = currentGeneration;
    currentGeneration = lastGeneration
        .newGeneration(args.getNumElites(), args.getNumNewIndividualsPerGeneration(), metadata,
            migrated);
    if (currentGeneration.getIndividuals().get(0) != bestIndividual) {
      bestIndividual = currentGeneration.getIndividuals().get(0);
    }
    log.info("Best fitness of generation {}: {}", genNum + 1,
        currentGeneration.getIndividuals().get(0).getFitness());
    if (genNum > 1) {
      if (lastCollectedFitness == null) {
        Objects.requireNonNull(bestIndividual);
        lastCollectedFitness = bestIndividual.getFitness();
      }
      if (genNum % args.getGenerationStatisticParameters().getGenerationInterval() == 0) {
        lastGenerationStatsCaptured = genNum;
        Objects.requireNonNull(bestIndividual);
        fitnessChangeByGeneration.put(genNum, bestIndividual.getFitness() - lastCollectedFitness);
        lastCollectedFitness = bestIndividual.getFitness();
      }
    }
  }

  @NotNull
  private Generation<T> createGeneration(int individuals) {

    return new Generation<>(individuals, args);
  }

  /**
   * select high-fitness individuals to be assigned to a new population. The individuals returned
   * here are clones, and may be assigned to a separate population without cloning again.
   */
  @NotNull
  public List<T> selectForPopulationSwap() {
    int subsetSize = Math.toIntExact(args.getMultiplePopulationParameters().getNumEliteSwap());
    List<T> entries = new ArrayList<>(subsetSize);

    currentGeneration.sort();
    for (int i = 0; i < subsetSize; i++) {
      entries.add(
          args.getSelector().select(currentGeneration.getIndividuals()).getAttributes().clone());
    }

    return entries;
  }
}
