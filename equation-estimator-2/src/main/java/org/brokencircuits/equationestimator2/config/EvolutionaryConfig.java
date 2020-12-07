package org.brokencircuits.equationestimator2.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import org.brokencircuits.equationestimator2.eq.EquationData;
import org.brokencircuits.equationestimator2.eq.EquationDataSet;
import org.brokencircuits.equationestimator2.eq.EquationNode;
import org.brokencircuits.equationestimator2.eq.EquationNodeType;
import org.brokencircuits.equationestimator2.eq.EquationTree;
import org.brokencircuits.equationestimator2.eq.EquationTreePrinter;
import org.brokencircuits.equationestimator2.eq.EquationVariableReference;
import org.brokencircuits.equationestimator2.util.RandomEquationUtil;
import org.brokencircuits.equationestimator2.util.RandomUtil;
import org.brokencircuits.evolve.AttributeMutator;
import org.brokencircuits.evolve.AttributeSwapper;
import org.brokencircuits.evolve.FitnessEvaluator;
import org.brokencircuits.evolve.HistoryMetadata;
import org.brokencircuits.evolve.Individual;
import org.brokencircuits.evolve.IndividualGenerator;
import org.brokencircuits.evolve.IndividualSelector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class EvolutionaryConfig {


  @Bean
  IndividualGenerator<EquationTree> generator(
      EquationData dataSets) {
    EquationTreePrinter printer = new EquationTreePrinter();
    return (HistoryMetadata metadata) -> {
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

        fitness += Math
            .pow(Math.abs(tree.getRootNode().getData().eval() - dataSet.getExpectedOutput()) * 4,
                2);
      }

      // any trees without variables should be penalized
      Map<EquationNodeType, Set<TreeNode<EquationNode, EquationTree>>> nodesByType = tree
          .getNodesByType();
      int treeNodeCount = nodesByType.values().stream().mapToInt(Set::size).sum();

      // penalize trees with more than 100 nodes, +10 fitness for each node over limit
      fitness += Math.max(0, treeNodeCount - 250) * 20;
      if (treeNodeCount < 6) {
        fitness *= (6 - treeNodeCount);
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
    return (tree, metadata) -> {

      int i = RandomUtil.RANDOM.nextInt(100);
      if (i < 5) {
        TreeNode<EquationNode, EquationTree> node = chooseNodeForSwap(tree, getStaleness(metadata));
        TreeNode<EquationNode, EquationTree> replacementNode = RandomEquationUtil
            .createSubtree(dataSets.getVariables(), null);
        tree.swap(node, replacementNode);
      }
    };
  }

  @Bean
  IndividualSelector<EquationTree> selector() {
    return sortedIndividuals -> {
      int granularity = 1000;

      int i = 0;
      Individual<EquationTree> individual = null;
      while (individual == null || individual.getFitness().isInfinite() || individual.getFitness()
          .isNaN()) {
        i++;
        int selection = RandomUtil.RANDOM.nextInt(granularity);
        double percentile = Math.pow(selection, 2) / Math.pow(granularity, 2);
        individual = sortedIndividuals
            .get(Math.toIntExact(Math.round(Math.floor(percentile * sortedIndividuals.size()))));
        if (i > 1000 && i % 100 == 0) {
          log.warn("Stuck in probable infinite loop");
        }
      }

      return individual;
    };
  }

  @Bean
  AttributeSwapper<EquationTree> swapper() {
    return (first, second, metadata) -> {

      double staleness = getStaleness(metadata);

      TreeNode<EquationNode, EquationTree> node1 = chooseNodeForSwap(first, staleness);
      TreeNode<EquationNode, EquationTree> node2 = chooseNodeForSwap(second, staleness);
      Objects.requireNonNull(node1);
      Objects.requireNonNull(node2);

      first.swap(node1, node2);
    };
  }

  private double getStaleness(HistoryMetadata metadata) {
    Map<Long, Double> fitnessChangeByGeneration = metadata.getFitnessChangeByGeneration();

    // calculate a staleness factor to determine if the population has become somewhat stale,
    // requiring that the swap nodes are higher up the tree, causing a more drastic exchange
    Long lastStatGen = metadata.getLastCapturedGen();

    // staleness is a scale from 0 to 1, with 1 being completely stale, 0.5 meaning the fitness
    // has halved since the last statistic capture
    double staleness = 0;
    if (lastStatGen != null && metadata.getCurrentFitness() != null) {
      Double lastFitnessChange = Math.abs(fitnessChangeByGeneration.get(lastStatGen));
      Double currentFitness = metadata.getCurrentFitness();
      staleness = currentFitness / (currentFitness + lastFitnessChange);
    }

    return staleness;
  }

  private TreeNode<EquationNode, EquationTree> chooseNodeForSwap(EquationTree tree,
      double staleness) {
    Set<TreeNode<EquationNode, EquationTree>> varNodes = tree.getNodesByType()
        .getOrDefault(EquationNodeType.VARIABLE, Collections.emptySet());
    Set<TreeNode<EquationNode, EquationTree>> constantNodes = tree.getNodesByType()
        .getOrDefault(EquationNodeType.CONSTANT, Collections.emptySet());

    List<TreeNode<EquationNode, EquationTree>> terminalNodes = new ArrayList<>(
        varNodes.size() + constantNodes.size());
    terminalNodes.addAll(varNodes);
    terminalNodes.addAll(constantNodes);
    int i = RandomUtil.RANDOM.nextInt(terminalNodes.size());
    TreeNode<EquationNode, EquationTree> firstTerminal = terminalNodes.get(i);
    List<TreeNode<EquationNode, EquationTree>> hierarchy = new LinkedList<>();

    // push hierarchy onto list, so terminal node is at [0] and root is at the end
    TreeNode<EquationNode, EquationTree> crawler = firstTerminal;
    while (crawler != null) {
      hierarchy.add(crawler);
      crawler = crawler.getParent();
    }
    if (hierarchy.size() == 1) {
      return hierarchy.get(0);
    }

    int numNodes = hierarchy.size();
    // choose an operation node from mid-tree for swapping. The more stale the population is,
    // the more likely the node will be higher in the tree
    int minNode = Math.toIntExact(Math.round(Math.floor(0.1 * numNodes)));
    // if staleness is less than .95, activityFactor will be 0; .96 => 0.2, .97 => 0.4, ..., 1.0 => 1.0
    double activityFactor = Math.max(staleness - 0.95, 0) * 20;
    int maxNode = Math
        .toIntExact(Math.round(Math.floor((0.1 + (0.7 * activityFactor)) * numNodes)));
    if (maxNode == minNode) {
      return hierarchy.get(minNode);
    }
    int chooseHierachyNode = RandomUtil.RANDOM.nextInt(maxNode - minNode) + minNode;

    return hierarchy.get(chooseHierachyNode);
  }

}
