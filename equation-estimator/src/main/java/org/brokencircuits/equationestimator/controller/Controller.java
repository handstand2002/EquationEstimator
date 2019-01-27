package org.brokencircuits.equationestimator.controller;


import java.io.File;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.domain.Equation;
import org.brokencircuits.equationestimator.domain.Generation;
import org.brokencircuits.equationestimator.domain.TreeNode;
import org.brokencircuits.equationestimator.domain.node.Variable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Controller implements Runnable {

  final public static int POP_SIZE = 100;
  final public static int INIT_OP_NODE_COUNT = 50;
  final public static int ITERATE_GENS = 500;
  final public static double ELITISM = 0.05;    // percent of elites to retain

  final private Dataset dataset = Dataset.getInstance();

  @Override
  public void run() {
    try {
      dataset.setSolutionName("y");
      dataset.loadCsv(new File("data.csv"));
    } catch (IOException e) {
      log.error("Unable to read csv due to error: ", e);
    }

    Generation currentGen = new Generation();
    currentGen.generateRandomPop();
    Equation eq = currentGen.equationList().get(0);

    String json = eq.toJson();

    Equation eq2 = null;
    try {
      eq2 = Equation.fromJson(json);

    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    Variable x = dataset.getVariableByName("x").orElse(null);
    StringBuilder sb = new StringBuilder();
    if (x != null) {
      for (double i = 0; i < 10; i += .01) {
        x.setValue(Optional.of(i));
        double originalSolution = eq.eval();
        double simplifiedSolution = eq2.eval();
        if (originalSolution != simplifiedSolution) {
          log.warn("Original: {}; JSON Copied: {}", originalSolution, simplifiedSolution);
        } else {
          sb.append(i).append("\t").append(simplifiedSolution).append("\n");
        }
      }
      log.info("Solutions:\n{}", sb.toString());
    } else {
      log.error("could not find variable x");
    }

//    int generationNum = 0;
//    Generation currentGen = new Generation();
//    currentGen.generateRandomPop();
//
//    do {
//      currentGen = currentGen.generateNext();
//    } while (++generationNum < ITERATE_GENS);
//
//    // print out best equation in a format excel can read, so we can graph it
//    Equation bestEq = currentGen.equationList().get(0).clone();
//
//    Equation simplifiedBestEq = bestEq.clone();
//    simplifiedBestEq.simplify();
//
//    Variable x = dataset.getVariableByName("x").orElse(null);
//    StringBuilder sb = new StringBuilder();
//    if (x != null) {
//      for (double i = 0; i < 10; i += .01) {
//        x.setValue(Optional.of(i));
//        double originalSolution = bestEq.eval();
//        double simplifiedSolution = simplifiedBestEq.eval();
//        if (originalSolution != simplifiedSolution) {
//          log.warn("Original: {}; Simplified: {}", originalSolution, simplifiedSolution);
//        } else {
//          sb.append(i).append("\t").append(simplifiedSolution).append("\n");
//        }
//      }
//      log.info("Solutions:\n{}", sb.toString());
//    } else {
//      log.error("could not find variable x");
//    }

  }

  public static void printAllEqs(TreeNode node) {
    if (node.getLeftChild() != null) {
      printAllEqs(node.getLeftChild());
    }
    if (node.getRightChild() != null) {
      printAllEqs(node.getRightChild());
    }
    log.info("={}", TreeNode.equationReadable(node, false));
  }

}
