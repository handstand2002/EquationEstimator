package org.brokencircuits.evolve;

public interface AttributeMutator<T extends AttributeType<T>> {

  void apply(T individual, HistoryMetadata metadata);
}
