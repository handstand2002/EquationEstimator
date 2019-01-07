package org.brokencircuits.equationestimator.domain;

import static org.junit.Assert.assertEquals;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;
import org.junit.Test;

@Slf4j
public class StatisticTest {

  @Test
  @SneakyThrows
  public void constructorWithTreeNode() {
    double delta = 0.0000001;

    Constant constant = new Constant(3);
    Variable variable = new Variable();
    variable.setValue(4);
    Operator operator = new Operator(Operator.PLUS);
    TreeNode leftChild = new TreeNode(constant);
    TreeNode rightChild = new TreeNode(variable);

    // when
    TreeNode root = new TreeNode(operator, leftChild, rightChild);

    // then
    Statistic stats = root.getStatistics();
    assertEquals(2, stats.getNumDescendant(), delta);
    assertEquals(1, stats.getNumDescendantConstant(), delta);
    assertEquals(1, stats.getNumDescendantVariable(), delta);
    assertEquals(0, stats.getNumDescendantOperator(), delta);
    assertEquals(2, stats.getNumDescendantTerminal(), delta);
  }

  @Test
  @SneakyThrows
  public void constructorWithDeeperTreeNode() {
    double delta = 0.0000001;

    Constant constant = new Constant(3);

    Operator operator = new Operator(Operator.PLUS);
    TreeNode leftChild = new TreeNode(constant);

    Operator operator2 = new Operator(Operator.MINUS);
    Variable variable = new Variable();
    variable.setValue(4);
    Variable variable1 = new Variable();
    variable.setValue(3);
    TreeNode leftSubChild = new TreeNode(variable);
    TreeNode rightSubChild = new TreeNode(variable1);

    TreeNode rightChild = new TreeNode(operator2, leftSubChild, rightSubChild);

    // when
    TreeNode root = new TreeNode(operator, leftChild, rightChild);

    // then
    Statistic stats = root.getStatistics();
    assertEquals(4, stats.getNumDescendant(), delta);
    assertEquals(1, stats.getNumDescendantConstant(), delta);
    assertEquals(2, stats.getNumDescendantVariable(), delta);
    assertEquals(1, stats.getNumDescendantOperator(), delta);
    assertEquals(3, stats.getNumDescendantTerminal(), delta);
  }

}