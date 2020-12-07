package org.brokencircuits.equationestimator2;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator2.config.EquationEvolutionaryParamConfig;
import org.brokencircuits.equationestimator2.domain.DefaultTreeNodePrinter;
import org.brokencircuits.equationestimator2.eq.EquationData;
import org.brokencircuits.equationestimator2.eq.EquationDataSet;
import org.brokencircuits.equationestimator2.eq.EquationNode;
import org.brokencircuits.equationestimator2.eq.EquationTree;
import org.brokencircuits.equationestimator2.eq.EquationVariableReference;
import org.brokencircuits.equationestimator2.util.MethodCallerTracker;
import org.brokencircuits.equationestimator2.util.RandomUtil;
import org.brokencircuits.evolve.AttributeMutator;
import org.brokencircuits.evolve.AttributeSwapper;
import org.brokencircuits.evolve.EvolutionaryController;
import org.brokencircuits.evolve.EvolutionaryParameters;
import org.brokencircuits.evolve.FitnessEvaluator;
import org.brokencircuits.evolve.GenerationStatisticParameters;
import org.brokencircuits.evolve.IndividualGenerator;
import org.brokencircuits.evolve.IndividualSelector;
import org.brokencircuits.evolve.MultiplePopulationParameters;
import org.brokencircuits.evolve.Population;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  @Bean
  public CommandLineRunner test(
      IndividualGenerator<EquationTree> generator,
      FitnessEvaluator<EquationTree> evaluator,
      AttributeMutator<EquationTree> mutator,
      IndividualSelector<EquationTree> selector,
      AttributeSwapper<EquationTree> swapper,
      EquationData dataSets,
      EquationEvolutionaryParamConfig paramConfig) {
    return args -> {

      try {
        log.info("Using configuration:\n{}", paramConfig);

        EvolutionaryParameters<EquationTree> params = EvolutionaryParameters.<EquationTree>builder()
            .generator(generator)
            .evaluator(evaluator)
            .mutator(mutator)
            .selector(selector)
            .swapper(swapper)
            .generationStatisticParameters(GenerationStatisticParameters.builder()
                .generationInterval(30)
                .build())
            .numElites(paramConfig.getNumElites())
            .numGenerations(paramConfig.getNumGenerations())
            .numIndividuals(paramConfig.getNumIndividuals())
            .numNewIndividualsPerGeneration(paramConfig.getNumNewIndividualsPerGeneration())
            .multiplePopulationParameters(MultiplePopulationParameters.builder()
                .numEliteSwap(paramConfig.getParallel().getNumSwapPerPop())
                .numPopulations(paramConfig.getParallel().getNumPopulations())
                .migrateIndividualsTrigger(
                    () -> RandomUtil.RANDOM.nextFloat() < paramConfig.getParallel()
                        .getChanceForSwap())
                .build())
            .build();

        EvolutionaryController<EquationTree> controller = new EvolutionaryController<>(params);
        List<Population<EquationTree>> pops = controller.run();
        Optional<Population<EquationTree>> bestPop = pops.stream()
            .min(Comparator.comparing(equationTreePopulation -> {
              equationTreePopulation.getCurrentGeneration().sort();
              return equationTreePopulation.getCurrentGeneration().getIndividuals().get(0)
                  .getFitness();
            }));

        DefaultTreeNodePrinter<EquationNode, EquationTree> defaultPrinter = new DefaultTreeNodePrinter<>();
        EquationTree bestTree = bestPop.get().getCurrentGeneration().getIndividuals().get(0)
            .getAttributes();
        MethodCallerTracker.log();
        log.info("Best fitness: \n{}", bestTree);
        log.info("Best fitness: \n{}", defaultPrinter.printTree(bestTree));

        // put together the inputs/outputs into a comma-separated table
        StringBuilder sb = new StringBuilder();
        for (EquationVariableReference variable : dataSets.getVariables()) {
          sb.append(variable.getName()).append(",");
        }
        sb.append("Output\n");

        for (EquationDataSet dataSet : dataSets.getDataSets()) {
          dataSet.getValues().forEach(EquationVariableReference::setCurrentValue);

          for (EquationVariableReference variable : dataSets.getVariables()) {
            sb.append(dataSet.getValues().get(variable)).append(",");
          }
          sb.append(bestTree.getRootNode().getData().eval()).append("\n");
        }
        log.info("Output: \n{}", sb.toString());

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }

    };
  }
}
