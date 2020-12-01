package org.brokencircuits.evolve;

public interface AttributeSwapper<T extends AttributeType<T>> {

  void apply(T first, T second);
}
