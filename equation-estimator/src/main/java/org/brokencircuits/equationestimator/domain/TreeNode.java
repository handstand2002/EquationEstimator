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
@EqualsAndHashCode(exclude = {"leftChild", "rightChild", "parent", "container", "statistics"})
@ToString(exclude = {"leftChild", "rightChild", "parent", "container"})
public class TreeNode {

  private static int lastId = 0;

  @Getter
  final private int id;
  @Getter
  @NonNull
  private IDataNode dataNode;
  @Getter
  private TreeNode leftChild = null;
  @Getter
  private TreeNode rightChild = null;
  @Getter
  private TreeNode parent = null;
  @Getter
  private WhichChild whichChild;
  @Setter
  private Equation container;
  @Getter
  private Statistic statistics;

  private TreeNode(int id, Equation container, IDataNode dataNode, TreeNode leftChild,
      TreeNode rightChild) {
    this.dataNode = dataNode;
    this.leftChild = leftChild;
    this.rightChild = rightChild;
    this.leftChild.setParent(this, WhichChild.LEFT);
    this.rightChild.setParent(this, WhichChild.RIGHT);
    this.container = container;
    statistics = new Statistic(this);
    this.id = id;
  }

  private TreeNode(int id, Equation container, IDataNode dataNode) {
    this.container = container;
    this.dataNode = dataNode;
    statistics = new Statistic(this);
    this.id = id;
  }

  public TreeNode(Equation container, IDataNode dataNode, TreeNode leftChild, TreeNode rightChild) {
    this.dataNode = dataNode;
    this.leftChild = leftChild;
    this.rightChild = rightChild;
    this.leftChild.setParent(this, WhichChild.LEFT);
    this.rightChild.setParent(this, WhichChild.RIGHT);
    this.container = container;
    statistics = new Statistic(this);
    id = ++lastId;
  }

  @java.beans.ConstructorProperties({"dataNode"})
  public TreeNode(Equation container, IDataNode dataNode) {
    this.container = container;
    this.dataNode = dataNode;
    statistics = new Statistic(this);
    id = ++lastId;
  }

  public static void swapNodes(TreeNode initial, TreeNode replacement) {
    Equation initialContainer = initial.container;
    Equation replaceContainer = replacement.container;
    WhichChild initialWhichChild = initial.whichChild;
    WhichChild replaceWhichChild = replacement.whichChild;
    TreeNode initialParent = initial.parent;
    TreeNode replaceParent = replacement.parent;

    if (initialParent != null) {
      initialParent.setChild(replacement, initialWhichChild);
    } else {
      initialContainer.setRoot(replacement);
    }

    if (replaceParent != null) {
      replaceParent.setChild(initial, replaceWhichChild);
    } else {
      replaceContainer.setRoot(initial);
    }

    initial.whichChild = replaceWhichChild;
    replacement.whichChild = initialWhichChild;

    TreeNode.setSubtreeContainer(replacement, initialContainer);
    TreeNode.setSubtreeContainer(initial, replaceContainer);

    initial.statistics.onChange();
    replacement.statistics.onChange();
  }

  private static void setSubtreeContainer(TreeNode node, Equation container) {
    if (node.container != null) {
      node.container.getNodeList().remove(node);
    }

    node.setContainer(container);
    if (node.container != null) {
      node.container.getNodeList().add(node);
    }

    if (node.leftChild != null) {
      TreeNode.setSubtreeContainer(node.leftChild, container);
    }
    if (node.rightChild != null) {
      TreeNode.setSubtreeContainer(node.rightChild, container);
    }
  }

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

  public static String treeStringsToString(List<String> input) {
    return String.join("\n", input);
  }

  public static List<String> equationTreeNode(TreeNode anonymousNode) {
    final List<String> treeStrings = Lists.newArrayList();

    equationTreeNode(anonymousNode, 0, treeStrings);
    return treeStrings;
  }

//  public void replaceNode(TreeNode replacement) {
//    log.info("Tree from parent before swap:\n{}",
//        TreeNode.treeStringsToString(TreeNode.equationTreeNode(this.getParent())));
//    parent.setChild(replacement, whichChild);
//    log.info("Tree from parent after swap:\n{}",
//        TreeNode.treeStringsToString(TreeNode.equationTreeNode(this.getParent())));
//  }

  /* ***************************** STATIC FUNCTIONS ***************************** */

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

  public void setParent(TreeNode parent, WhichChild which) {
    this.parent = parent;
    this.whichChild = which;
  }

  public void setChild(TreeNode child, WhichChild which) {
    switch (which) {
      case LEFT:
        this.leftChild = child;
        break;
      case RIGHT:
        this.rightChild = child;
        break;
      default:
        log.error("Unable to set child due to invalid 'which' parameter");
    }
  }

  public double eval() {
    if (dataNode.getClass() == Operator.class) {
      // Variables may not exist, so we need to check if they do first, and if they don't, use a value
      // that won't affect the equation
      Operator opNode = (Operator) dataNode;
      double leftChildValue;
      if (leftChild.dataNode.getClass() == Variable.class
          && !((Variable) leftChild.dataNode).getValue().isPresent()) {
        leftChildValue = opNode.noEffectValue();
      } else {
        leftChildValue = leftChild.eval();
      }
      double rightChildValue;
      if (rightChild.dataNode.getClass() == Variable.class
          && !((Variable) rightChild.dataNode).getValue().isPresent()) {
        rightChildValue = opNode.noEffectValue();
      } else {
        rightChildValue = rightChild.eval();
      }
      return opNode.operation(leftChildValue, rightChildValue);
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
    if (dataNode.getClass() == Operator.class) {
      if ((statistics.getNumDescendantVariable() == 0)) {

        this.dataNode = new Constant(this.eval());
        this.container.removeNodeSubtreeFromEquation(this.leftChild);
        this.container.removeNodeSubtreeFromEquation(this.rightChild);
        this.leftChild = null;
        this.rightChild = null;
        this.statistics.onChange();

      } else {
        this.getLeftChild().simplify();
        this.getRightChild().simplify();
      }
    }
  }

  public TreeNode clone(Equation newContainer) {

    TreeNode leftChildClone = null;
    TreeNode rightChildClone = null;
    if (this.leftChild != null && this.rightChild != null) {
      leftChildClone = this.leftChild.clone(newContainer);
      rightChildClone = this.rightChild.clone(newContainer);
    }

    IDataNode newDataNode = this.dataNode.clone();
    TreeNode clone;
    if (leftChildClone != null && rightChildClone != null) {
      clone = new TreeNode(this.id, newContainer, newDataNode, leftChildClone, rightChildClone);
    } else {
      clone = new TreeNode(this.id, newContainer, newDataNode);
    }

    newContainer.getNodeList().add(clone);

    return clone;
  }

  public enum WhichChild {
    LEFT, RIGHT
  }
}
