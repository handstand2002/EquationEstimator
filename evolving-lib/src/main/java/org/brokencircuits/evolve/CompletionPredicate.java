package org.brokencircuits.evolve;

import java.util.List;

public interface CompletionPredicate<T extends AttributeType> {

  boolean isComplete(List<Individual<T>> scored);
}
