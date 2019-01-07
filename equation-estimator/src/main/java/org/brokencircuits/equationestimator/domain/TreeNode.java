package org.brokencircuits.equationestimator.domain;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.IDataNode;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;

@Slf4j
@EqualsAndHashCode(exclude = {"leftChild", "rightChild", "parent"})
@ToString(exclude = {"leftChild", "rightChild", "parent"})
public class TreeNode {

  @Getter
  @NonNull
  private IDataNode dataNode;
  @Getter
  private TreeNode leftChild = null;
  @Getter
  private TreeNode rightChild = null;
  @Getter
  @Setter
  private TreeNode parent = null;
  @Getter
  private Statistic statistics;

  public TreeNode(IDataNode dataNode, TreeNode leftChild, TreeNode rightChild) {
    this.dataNode = dataNode;
    this.leftChild = leftChild;
    this.rightChild = rightChild;
    this.leftChild.setParent(this);
    this.rightChild.setParent(this);
    statistics = new Statistic(this);
  }

  @java.beans.ConstructorProperties({"dataNode"})
  public TreeNode(IDataNode dataNode) {
    this.dataNode = dataNode;
    statistics = new Statistic(this);
  }

  public double eval() {
    if (dataNode.getClass() == Operator.class) {
      return ((Operator) dataNode).operation(leftChild.eval(), rightChild.eval());
    } else if (dataNode.getClass() == Variable.class) {
      return ((Variable) dataNode).eval();
    } else if (dataNode.getClass() == Constant.class) {
      return ((Constant) dataNode).eval();
    } else {
      log.error("DataNode is not of known class: {}", this);
      return 0;
    }
  }

  public String equationReadable(boolean variableShowValue) {
    return TreeNode.equationReadable(this, variableShowValue);
  }

  public void simplify() {
    log.info("Will do simplification here");
    // TODO: Set up simplification
  }

  /* ***************************** STATIC FUNCTIONS ***************************** */
  public static String equationReadableOperatorNode(TreeNode fromNode, boolean variableShowValue) {

    TreeNode leftChild = fromNode.leftChild;
    TreeNode rightChild = fromNode.rightChild;
    Operator dataNode = (Operator) fromNode.dataNode;

    String readableEquation = "(";
    readableEquation += equationReadable(leftChild, variableShowValue);
    readableEquation += " " + dataNode.getChar() + " ";
    readableEquation += equationReadable(rightChild, variableShowValue);
    readableEquation += ")";

    return readableEquation;
  }

  public static String equationReadable(TreeNode anonymousNode,
      boolean variableShowValue) {
    if (anonymousNode.dataNode.getClass() == Operator.class) {
      return equationReadableOperatorNode(anonymousNode, variableShowValue);
    } else if (anonymousNode.dataNode.getClass() == Constant.class) {
      return equationReadableConstantNode(anonymousNode);
    } else if (anonymousNode.dataNode.getClass() == Variable.class) {
      return equationReadableVariableNode(anonymousNode, variableShowValue);
    } else {
      log.error("Node Type is invalid: {}", anonymousNode);
      return "";
    }
  }

  public static String equationReadableConstantNode(TreeNode constantNode) {
    Constant dataNode = (Constant) constantNode.dataNode;
    return String.valueOf(dataNode.eval());
  }

  public static String equationReadableVariableNode(TreeNode treeNode, boolean useValue) {
    Variable dataNode = (Variable) treeNode.dataNode;
    if (useValue) {
      return "V[" + dataNode.eval() + "]";
    } else {
      return "VID[" + dataNode.getId() + "]";
    }
  }

  public static List<String> equationTreeNode(TreeNode anonymousNode) {
    final List<String> treeStrings = Lists.newArrayList();

    equationTreeNode(anonymousNode, 0, treeStrings);

    return treeStrings;
  }

  private static void equationTreeNode(TreeNode anonymousNode, int depth,
      final List<String> treeStrings) {

    StringBuilder nodeString = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      nodeString.append("|  ");
    }
    nodeString.append(anonymousNode.toString());
    treeStrings.add(nodeString.toString());
    if (anonymousNode.dataNode.getClass() == Operator.class) {
      equationTreeNode(anonymousNode.leftChild, depth + 1, treeStrings);
      equationTreeNode(anonymousNode.rightChild, depth + 1, treeStrings);
    }
  }

}
