package org.brokencircuits.evolve;

import lombok.Builder;
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

}
