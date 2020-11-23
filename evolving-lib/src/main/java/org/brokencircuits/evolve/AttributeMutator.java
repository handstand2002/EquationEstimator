package org.brokencircuits.evolve;

public interface AttributeMutator<T extends AttributeType> {

  void apply(Individual<T> individual);
}
