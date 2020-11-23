package org.brokencircuits.evolve;

public interface AttributeSwapper<T extends AttributeType> {

  void apply(Individual<T> first, Individual<T> second);
}
