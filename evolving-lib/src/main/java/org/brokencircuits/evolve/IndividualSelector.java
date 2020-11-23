package org.brokencircuits.evolve;

import java.util.List;

public interface IndividualSelector<T extends AttributeType> {

  Individual<T> select(List<Individual<T>> sortedIndividuals);
}
