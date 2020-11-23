package org.brokencircuits.evolve;

public interface FitnessEvaluator<T extends AttributeType> {

  /**
   * 0 is best, high values are worst. negative values are not allowed
   */
  double evaluate(Individual<T> individual);
}
