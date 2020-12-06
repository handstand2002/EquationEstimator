package org.brokencircuits.equationestimator2;

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
import org.brokencircuits.equationestimator2.domain.DefaultTreeNodePrinter;
import org.brokencircuits.equationestimator2.domain.TreeNode;
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
import org.brokencircuits.evolve.GenerationStatisticParameters;
import org.brokencircuits.evolve.HistoryMetadata;
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
        TreeNode<EquationNode, EquationTree> node = tree.randomOperatorNode();
        Objects.requireNonNull(node);
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

      int selection = RandomUtil.RANDOM.nextInt(granularity);

      double percentile = Math.pow(selection, 2) / Math.pow(granularity, 2);

      return sortedIndividuals
          .get(Math.toIntExact(Math.round(Math.floor(percentile * sortedIndividuals.size()))));
    };
  }

  @Bean
  AttributeSwapper<EquationTree> swapper() {
    return (first, second, metadata) -> {

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

      TreeNode<EquationNode, EquationTree> node1 = chooseNodeForSwap(first, staleness);
      TreeNode<EquationNode, EquationTree> node2 = chooseNodeForSwap(second, staleness);
      Objects.requireNonNull(node1);
      Objects.requireNonNull(node2);

      first.swap(node1, node2);
    };
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

  @Bean
  public CommandLineRunner test(
      IndividualGenerator<EquationTree> generator,
      FitnessEvaluator<EquationTree> evaluator,
      AttributeMutator<EquationTree> mutator,
      IndividualSelector<EquationTree> selector,
      AttributeSwapper<EquationTree> swapper,
      EquationData dataSets) {
    return args -> {

      try {
        EvolutionaryParameters<EquationTree> params = EvolutionaryParameters.<EquationTree>builder()
            .generator(generator)
            .evaluator(evaluator)
            .mutator(mutator)
            .selector(selector)
            .swapper(swapper)
            .generationStatisticParameters(GenerationStatisticParameters.builder()
                .generationInterval(30)
                .build())
            .numElites(5)
            .numGenerations(1000)
            .numIndividuals(100)
            .numNewIndividualsPerGeneration(5)
            .build();

        EvolutionaryController<EquationTree> controller = new EvolutionaryController<>(params);
        Generation<EquationTree> newGeneration = controller.run();

        DefaultTreeNodePrinter<EquationNode, EquationTree> defaultPrinter = new DefaultTreeNodePrinter<>();
        EquationTree bestTree = newGeneration.getIndividuals().get(0).getAttributes();
        MethodCallerTracker.log();
        log.info("Best fitness: \n{}", bestTree);
        log.info("Best fitness: \n{}", defaultPrinter.printTree(bestTree));

        // put together the inputs/outputs into a tab-separated table
        StringBuilder sb = new StringBuilder();
        for (EquationVariableReference variable : dataSets.getVariables()) {
          sb.append(variable.getName()).append("\t");
        }
        sb.append("Output\n");

        for (EquationDataSet dataSet : dataSets.getDataSets()) {
          dataSet.getValues().forEach(EquationVariableReference::setCurrentValue);

          for (EquationVariableReference variable : dataSets.getVariables()) {
            sb.append(dataSet.getValues().get(variable)).append("\t");
          }
          sb.append(bestTree.getRootNode().getData().eval()).append("\n");
        }
        log.info("Output: \n{}", sb.toString());

        //EquationData dataSets) {
        //    return tree -> {
        //      double fitness = 0;
        //      for (EquationDataSet dataSet : dataSets.getDataSets()) {
        //
        //        // set the current value for each variable to the set's value
        //        dataSet.getValues().forEach(EquationVariableReference::setCurrentValue);
        //
        //        fitness += Math
        //            .pow(Math.abs(tree.getRootNode().getData().eval() - dataSet.getExpectedOutput()) * 10,
        //                2);
        //      }

      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }

    };
  }
}
