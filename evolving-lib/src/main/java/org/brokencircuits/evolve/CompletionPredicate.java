package org.brokencircuits.evolve;

import java.util.List;

public interface CompletionPredicate<T extends AttributeType<T>> {

  boolean isComplete(List<Individual<T>> scored);
}
