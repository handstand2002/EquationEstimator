package org.brokencircuits.evolve;

public interface IndividualGenerator<T extends AttributeType<T>> {

  T create(HistoryMetadata metadata);
}
