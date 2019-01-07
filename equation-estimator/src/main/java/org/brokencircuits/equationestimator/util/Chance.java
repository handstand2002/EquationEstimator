package org.brokencircuits.equationestimator.util;

import java.util.Random;

public class Chance {

  final public static Random RAND = new Random(System.currentTimeMillis());
  final private static int CHANCE_PRECISION = 10000;

  public static boolean addOperatorNode(int avgOpNodesDesired, int currentDepth) {
    double numNodes = Math.pow(2, currentDepth);
    double chance = 1 - (numNodes / (float) avgOpNodesDesired / 2);
    return (RAND.nextInt(CHANCE_PRECISION) < (chance * CHANCE_PRECISION));
  }

  public static boolean addVariableNode() {
    return (RAND.nextInt(2) == 0);
  }

  public static int randConstant() {
    return (RAND.nextInt(10) + 1);
  }
}
