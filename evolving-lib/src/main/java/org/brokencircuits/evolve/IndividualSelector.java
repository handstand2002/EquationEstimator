package org.brokencircuits.evolve;

import java.util.List;

public interface IndividualSelector<T extends AttributeType<T>> {

  Individual<T> select(List<Individual<T>> sortedIndividuals);
}
