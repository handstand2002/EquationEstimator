package org.brokencircuits.evolve;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public class Controller<T extends AttributeType> {

  private final EvolutionaryParameters<T> args;

  public Generation<T> run(long generations, int individuals, int elites) {
    Generation<T> gen = createGeneration(individuals);
    for (long i = 0; i < generations; i++) {
      gen = gen.newGeneration(elites);
      if (gen.isComplete()) {
        return gen;
      }
    }

    return gen;
  }

  @NotNull
  private Generation<T> createGeneration(int individuals) {

    return new Generation<>(individuals, args);
  }
}
