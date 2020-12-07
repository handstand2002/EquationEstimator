package org.brokencircuits.evolve;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Builder
@Getter
public class EvolutionaryParameters<T extends AttributeType<T>> {

  private final AttributeMutator<T> mutator;
  private final AttributeSwapper<T> swapper;
  private final FitnessEvaluator<T> evaluator;
  private final IndividualSelector<T> selector;
  private final IndividualGenerator<T> generator;
  private final CompletionPredicate<T> completionPredicate;
  @Default
  private final GenerationStatisticParameters generationStatisticParameters = GenerationStatisticParameters
      .builder().build();
  private final int numGenerations;
  private final int numIndividuals;
  @Default
  private final int numElites = 0;
  @Default
  private final int numNewIndividualsPerGeneration = 0;
  @Default
  private final MultiplePopulationParameters multiplePopulationParameters = MultiplePopulationParameters
      .builder().build();
}
