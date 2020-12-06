package org.brokencircuits.evolve;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
@Builder
public class GenerationStatisticParameters {

  @Default
  private final long generationInterval = Long.MAX_VALUE;

}
