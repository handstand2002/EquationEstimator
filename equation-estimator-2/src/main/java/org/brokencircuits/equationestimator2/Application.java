package org.brokencircuits.equationestimator2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeStatistics;
import org.brokencircuits.equationestimator2.eq.EquationData;
import org.brokencircuits.equationestimator2.eq.EquationDataSet;
import org.brokencircuits.equationestimator2.eq.EquationNode;
import org.brokencircuits.equationestimator2.eq.EquationNodeType;
import org.brokencircuits.equationestimator2.eq.EquationTree;
import org.brokencircuits.equationestimator2.eq.EquationTreePrinter;
import org.brokencircuits.equationestimator2.eq.EquationVariableReference;
import org.brokencircuits.equationestimator2.util.MethodCallerTracker;
import org.brokencircuits.equationestimator2.util.RandomEquationUtil;
import org.brokencircuits.equationestimator2.util.RandomUtil;
import org.brokencircuits.evolve.AttributeMutator;
import org.brokencircuits.evolve.AttributeSwapper;
import org.brokencircuits.evolve.EvolutionaryController;
import org.brokencircuits.evolve.EvolutionaryParameters;
import org.brokencircuits.evolve.FitnessEvaluator;
import org.brokencircuits.evolve.Generation;
import org.brokencircuits.evolve.Individual;
import org.brokencircuits.evolve.IndividualGenerator;
import org.brokencircuits.evolve.IndividualSelector;
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
  IndividualGenerator<EquationTree> generator(
      EquationData dataSets) {
    EquationTreePrinter printer = new EquationTreePrinter();
    return () -> {
      EquationTree tree = RandomEquationUtil.createRandomTree(dataSets.getVariables());
      tree.setPrinter(printer);
      return tree;
    };
  }

  @Bean
  EquationData dataSets() throws IOException {

    try (BufferedReader fileReader = new BufferedReader(new FileReader("data.csv"))) {

      // header line
      String s = fileReader.readLine();
      String[] values = s.split(",");
      List<EquationVariableReference> variables = new LinkedList<>();
      for (int i = 0; i < values.length; i++) {
        if (i < values.length - 1) {
          EquationVariableReference newRef = new EquationVariableReference(values[i]);
          variables.add(newRef);
        }
      }

      List<EquationDataSet> dataSets = fileReader.lines()
          .map(line -> line.split(","))
          .map(v -> {
            EquationDataSet set = new EquationDataSet(Double.parseDouble(v[v.length - 1]));
            for (int i = 0; i < variables.size(); i++) {
              set.getValues().put(variables.get(i), Double.parseDouble(v[i]));
            }
            return set;
          }).collect(Collectors.toList());

      return new EquationData(dataSets, variables);
    }
  }

  @Bean
  FitnessEvaluator<EquationTree> evaluator(EquationData dataSets) {
    return tree -> {
      double fitness = 0;
      for (EquationDataSet dataSet : dataSets.getDataSets()) {

        // set the current value for each variable to the set's value
        dataSet.getValues().forEach(EquationVariableReference::setCurrentValue);

        fitness += Math.abs(tree.getRootNode().getData().eval() - dataSet.getExpectedOutput());
      }

      auditTree(tree);
      // any trees without variables should be penalized
      Map<EquationNodeType, Set<TreeNode<EquationNode, EquationTree>>> nodesByType = tree
          .getNodesByType();
      if (nodesByType.getOrDefault(EquationNodeType.VARIABLE, Collections.emptySet()).isEmpty()) {
        fitness = Math.pow(fitness, 3);
      }
      return fitness;
    };
  }

  private void auditTree(EquationTree tree) {
    TreeNode<EquationNode, EquationTree> rootNode = tree.getRootNode();
    Map<EquationNodeType, AtomicLong> nodeCounts = new HashMap<>();
    addToNodeCounts(nodeCounts, rootNode);

    long actualOpCount = nodeCounts.getOrDefault(EquationNodeType.OPERATOR, new AtomicLong(0))
        .get();
    long actualVarCount = nodeCounts.getOrDefault(EquationNodeType.VARIABLE, new AtomicLong(0))
        .get();
    long actualConstantCount = nodeCounts.getOrDefault(EquationNodeType.CONSTANT, new AtomicLong(0))
        .get();

    Map<EquationNodeType, Set<TreeNode<EquationNode, EquationTree>>> trackedNodesByType = tree
        .getNodesByType();

    long trackedOpCount = trackedNodesByType
        .getOrDefault(EquationNodeType.OPERATOR, Collections.emptySet()).size();
    long trackedVarCount = trackedNodesByType
        .getOrDefault(EquationNodeType.VARIABLE, Collections.emptySet()).size();
    long trackedConstantCount = trackedNodesByType
        .getOrDefault(EquationNodeType.CONSTANT, Collections.emptySet()).size();

    if (actualOpCount != trackedOpCount) {
      throw new IllegalStateException();
    }
    if (actualVarCount != trackedVarCount) {
      throw new IllegalStateException();
    }
    if (actualConstantCount != trackedConstantCount) {
      throw new IllegalStateException();
    }
  }

  private void addToNodeCounts(Map<EquationNodeType, AtomicLong> nodeCounts,
      TreeNode<EquationNode, EquationTree> node) {
    nodeCounts.computeIfAbsent(node.getData().getNodeType(), k -> new AtomicLong(0))
        .incrementAndGet();

    if (node.getLeft() != null) {
      addToNodeCounts(nodeCounts, node.getLeft());
    }
    if (node.getRight() != null) {
      addToNodeCounts(nodeCounts, node.getRight());
    }
  }

  @Bean
  AttributeMutator<EquationTree> mutator(EquationData dataSets) {
    return tree -> {
      TreeNode<EquationNode, EquationTree> node = tree.randomOperatorNode();
      Objects.requireNonNull(node);
      TreeNode<EquationNode, EquationTree> replacementNode = RandomEquationUtil
          .createSubtree(dataSets.getVariables(), null);
      tree.swap(node, replacementNode);
    };
  }

  @Bean
  IndividualSelector<EquationTree> selector() {
    return sortedIndividuals -> {
      int granularity = 1000;

      int selection = RandomUtil.RANDOM.nextInt(granularity);

      double percentile = Math.pow(selection, 2) / Math.pow(granularity, 2);

      return sortedIndividuals
          .get(Math.toIntExact(Math.round(Math.floor(percentile * sortedIndividuals.size()))));
    };
  }

  @Bean
  AttributeSwapper<EquationTree> swapper() {
    return (first, second) -> {
      TreeNode<EquationNode, EquationTree> node1 = first.randomOperatorNode();
      TreeNode<EquationNode, EquationTree> node2 = second.randomOperatorNode();
      Objects.requireNonNull(node1);
      Objects.requireNonNull(node2);

      first.swap(node1, node2);
    };
  }

  @Bean
  public CommandLineRunner test(
      IndividualGenerator<EquationTree> generator,
      FitnessEvaluator<EquationTree> evaluator,
      AttributeMutator<EquationTree> mutator,
      IndividualSelector<EquationTree> selector,
      AttributeSwapper<EquationTree> swapper) {
    return args -> {

      try {
        EvolutionaryParameters<EquationTree> params = EvolutionaryParameters.<EquationTree>builder()
            .generator(generator)
            .evaluator(evaluator)
            .mutator(mutator)
            .selector(selector)
            .swapper(swapper)
            .build();

        EvolutionaryController<EquationTree> controller = new EvolutionaryController<>(params);
        Generation<EquationTree> newGeneration = controller.run(100, 100, 5);

        MethodCallerTracker.log();
        log.info("Best fitness: \n{}", newGeneration.getIndividuals().get(0).getAttributes());
        log.info("Num Nodes: {}",
            newGeneration.getIndividuals().stream().map(Individual::getAttributes)
                .map(EquationTree::getStats).mapToLong(TreeStatistics::getNodeCount).sum());

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }

    };
  }
}
