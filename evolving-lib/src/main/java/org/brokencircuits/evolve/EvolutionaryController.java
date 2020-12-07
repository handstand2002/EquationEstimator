package org.brokencircuits.evolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EvolutionaryController<T extends AttributeType<T>> {

  private final EvolutionaryParameters<T> args;

  public List<Population<T>> run() {

    int numPopulations = args.getMultiplePopulationParameters().getNumPopulations();
    List<Population<T>> populations = new ArrayList<>(numPopulations);
    for (int i = 0; i < numPopulations; i++) {
      populations.add(new Population<>(args));
    }

    int numEliteSwap = Math.toIntExact(args.getMultiplePopulationParameters().getNumEliteSwap());

    for (int i = 0; i < args.getNumGenerations(); i++) {
      List<T> toSwap = new ArrayList<>(numPopulations * numEliteSwap);

      if (numPopulations > 1 && args.getMultiplePopulationParameters()
          .getMigrateIndividualsTrigger().getAsBoolean()) {
        log.info("Triggered migration");
        for (Population<T> population : populations) {
          toSwap.addAll(population.selectForPopulationSwap());
        }
        Collections.shuffle(toSwap);
      }
      Queue<T> toSwapQueue = new LinkedBlockingQueue<>(toSwap);

      for (Population<T> population : populations) {
        Collection<T> swapped = new ArrayList<>(numEliteSwap);
        for (int j = 0; j < numEliteSwap && !toSwapQueue.isEmpty(); j++) {
          swapped.add(toSwapQueue.poll());
        }
        population.advanceGeneration(i, swapped);
      }

      if (i % 100 == 0) {
        Optional<Population<T>> bestPop = populations.stream().min(Comparator
            .comparing(p -> p.getCurrentGeneration().getIndividuals().get(0).getFitness()));
        T bestTree = bestPop.get().getCurrentGeneration().getIndividuals().get(0).getAttributes();
        log.info("Best fitness for gen {}: \n{}", i, bestTree);
      }

    }

    return populations;
  }

}
