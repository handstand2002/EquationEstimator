package org.brokencircuits.evolve;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public class EvolutionaryController<T extends AttributeType<T>> {

  private final EvolutionaryParameters<T> args;
  private final Map<Long, Double> fitnessChangeByGeneration = new LinkedHashMap<>();
  private Double lastCollectedFitness = null;
  private Long lastGenerationStatsCaptured = null;

  public Generation<T> run() {
    Generation<T> gen = createGeneration(args.getNumIndividuals());

    Individual<T> bestIndividual = null;
    for (long i = 0; i < args.getNumGenerations(); i++) {
      HistoryMetadata metadata = HistoryMetadata.builder()
          .currentFitness(i == 0 ? null : gen.getIndividuals().get(0).getFitness())
          .currentGen(i)
          .fitnessChangeByGeneration(fitnessChangeByGeneration)
          .genInterval(args.getGenerationStatisticParameters().getGenerationInterval())
          .maxGenerations(args.getNumGenerations())
          .lastCapturedGen(lastGenerationStatsCaptured)
          .build();

      gen = gen
          .newGeneration(args.getNumElites(), args.getNumNewIndividualsPerGeneration(), metadata);
      if (gen.getIndividuals().get(0) != bestIndividual) {
        bestIndividual = gen.getIndividuals().get(0);
        log.info("Best fitness of generation {}: {}", i + 1,
            gen.getIndividuals().get(0).getFitness());
      }
      if (i > 1) {
        if (lastCollectedFitness == null) {
          Objects.requireNonNull(bestIndividual);
          lastCollectedFitness = bestIndividual.getFitness();
        }
        if (i % args.getGenerationStatisticParameters().getGenerationInterval() == 0) {
          lastGenerationStatsCaptured = i;
          Objects.requireNonNull(bestIndividual);
          fitnessChangeByGeneration.put(i, bestIndividual.getFitness() - lastCollectedFitness);
          lastCollectedFitness = bestIndividual.getFitness();
        }
      }
      if (gen.isComplete()) {
        return gen;
      }
    }

    return gen;
  }

  @NotNull
  private Generation<T> createGeneration(int individuals) {

    return new Generation<>(individuals, args);
  }
}
