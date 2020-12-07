package org.brokencircuits.evolve;

import java.util.function.BooleanSupplier;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

@Value
@Builder
public class MultiplePopulationParameters {

  @Default
  int numPopulations = 1;
  @Default
  long numEliteSwap = 1;
  @Default
  BooleanSupplier migrateIndividualsTrigger = () -> false;
}
