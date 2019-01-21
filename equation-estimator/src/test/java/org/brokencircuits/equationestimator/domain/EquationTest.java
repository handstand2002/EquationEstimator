package org.brokencircuits.equationestimator.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class EquationTest {

  @Before
  public void setUp() {
    Dataset dataset = Dataset.getInstance();
//    dataset.addVariable(new Variable());
  }

  @Test
  public void generateRandom() {
    int numOperators = 19;

    int totalNumOperators = 0;
    int numTrials = 1000;
    for (int i = 0; i < numTrials; i++) {
      Equation test = Equation.generateRandom(numOperators);
      totalNumOperators += test.statistic().getNumDescendantOperator();
    }

    assertEquals(numOperators, (double) totalNumOperators / numTrials, 1);
  }

  @Test
  public void cloneTest() {

    Equation eq = Equation.generateRandom(10);
    Equation eq2 = eq.clone();

    List<TreeNode> nodeListOne = eq.getNodeList();
    List<TreeNode> nodeListTwo = eq2.getNodeList();

    assertEquals(nodeListOne, nodeListTwo);

    for (TreeNode node : nodeListOne) {
      assertTrue(nodeListTwo.contains(node));
    }
  }

  @Test
  public void removeNodeSubtreeFromList() {
  }

  @Test
  public void eval() {
  }

  @Test
  public void equationReadable() {
  }

  @Test
  public void equationTree() {
  }

  @Test
  public void simplify() {
  }

  @Test
  public void setRoot() {
  }

  @Test
  public void getNodeList() {
  }
}