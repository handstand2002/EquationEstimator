package org.brokencircuits.evolve;

public interface IndividualGenerator<T extends AttributeType> {

  Individual<T> create();
}
