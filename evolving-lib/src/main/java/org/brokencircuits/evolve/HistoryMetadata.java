package org.brokencircuits.evolve;

import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@Value
@Builder
public class HistoryMetadata {

  long genInterval;
  long maxGenerations;
  long currentGen;
  @Nullable
  Long lastCapturedGen;
  @Nullable
  Double currentFitness;
  Map<Long, Double> fitnessChangeByGeneration;
}
