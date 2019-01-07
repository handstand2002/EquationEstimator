package org.brokencircuits.equationestimator.domain;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.IDataNode;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;

@Data
@ToString(exclude = {"linkedTreeNode"})
@Slf4j
public class Statistic {

  final private TreeNode linkedTreeNode;

  public Statistic(TreeNode ofNode) {
//    log.info("Setting up statistics of node: {}", ofNode);
    linkedTreeNode = ofNode;

    onChange();
  }

  public void onChange() {
    IDataNode dataNode = linkedTreeNode.getDataNode();
    if (dataNode.getClass() == Operator.class) {

      int directOpChildren = 0;
      int directConstChildren = 0;
      int directVarChildren = 0;

      IDataNode leftChild = linkedTreeNode.getLeftChild().getDataNode();
      IDataNode rightChild = linkedTreeNode.getRightChild().getDataNode();
      if (leftChild.getClass() == Operator.class) {
        directOpChildren++;
      } else if (leftChild.getClass() == Variable.class) {
        directVarChildren++;
      } else if (leftChild.getClass() == Constant.class) {
        directConstChildren++;
      }

      if (rightChild.getClass() == Operator.class) {
        directOpChildren++;
      } else if (rightChild.getClass() == Variable.class) {
        directVarChildren++;
      } else if (rightChild.getClass() == Constant.class) {
        directConstChildren++;
      }

      Statistic leftStats = linkedTreeNode.getLeftChild().getStatistics();
      Statistic rightStats = linkedTreeNode.getRightChild().getStatistics();

      numDescendant = leftStats.getNumDescendant() + rightStats.getNumDescendant() + 2;
      numDescendantOperator =
          leftStats.getNumDescendantOperator() + rightStats.getNumDescendantOperator()
              + directOpChildren;
      numDescendantVariable =
          leftStats.getNumDescendantVariable() + rightStats.getNumDescendantVariable()
              + directVarChildren;
      numDescendantConstant =
          leftStats.getNumDescendantConstant() + rightStats.getNumDescendantConstant()
              + directConstChildren;
      numDescendantTerminal =
          leftStats.getNumDescendantTerminal() + rightStats.getNumDescendantTerminal()
              + directConstChildren + directVarChildren;

    }

    if (linkedTreeNode.getParent() != null) {
      linkedTreeNode.getParent().getStatistics().onChange();
    }
  }

  final public static int NUM_DESCENDANT = 0;
  final public static int NUM_DESCENDANT_OPERATOR = 1;
  final public static int NUM_DESCENDANT_TERMINAL = 2;
  final public static int NUM_DESCENDANT_VARIABLE = 3;
  final public static int NUM_DESCENDANT_CONSTANT = 4;

  private double numDescendant = 0;
  private double numDescendantOperator = 0;
  private double numDescendantTerminal = 0;
  private double numDescendantVariable = 0;
  private double numDescendantConstant = 0;
}
