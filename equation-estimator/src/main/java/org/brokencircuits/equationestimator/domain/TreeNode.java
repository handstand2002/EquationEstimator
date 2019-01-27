package org.brokencircuits.equationestimator.domain;

import com.google.common.collect.Lists;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.controller.Controller;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.IDataNode;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;
import org.json.JSONObject;

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
  @Getter
  private Equation container;
  @Getter
  private Statistic statistics;

  private static final Dataset DATASET = Dataset.getInstance();

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

    // set properties of replacement node
    if (initialParent != null) {
      // initial node is not root
      initialParent.setChild(replacement, initialWhichChild);
      replacement.setParent(initialParent, initialWhichChild);
    } else {
      // initial node is root, set replacement node to root
      if (initialContainer != null) {
        initialContainer.setRoot(replacement);
      }
      replacement.setParent(null, null);
    }

    // set properties of initial node
    if (replaceParent != null) {
      // initial node is not root
      replaceParent.setChild(initial, replaceWhichChild);
      initial.setParent(replaceParent, replaceWhichChild);
    } else {
      // replace node is root, set initial node to root
      if (replaceContainer != null) {
        replaceContainer.setRoot(initial);
      }
      initial.setParent(null, null);
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
      return "(" + dataNode.eval() + ")";
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
      // TODO: Cache result of operations in nodes, retrieve it as result as long as there aren't any variable descendants
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

  String equationReadable(boolean variableShowValue) {
    return TreeNode.equationReadable(this, variableShowValue);
  }

  void simplify() {
    if (dataNode.getClass() == Operator.class) {
      Operator opDataNode = (Operator) dataNode;
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

        if (opDataNode.getChar().equals("-")) {
          if (this.getId() == 16467543) {
            log.info("left and right child datanodes equal: {}",
                this.getLeftChild().dataNode == this.getRightChild().dataNode);
            log.info("Left: {}", this.getLeftChild().dataNode);
            log.info("Right: {}", this.getRightChild().dataNode);
          }
          if (this.getLeftChild().dataNode == this.getRightChild().dataNode) {
            this.dataNode = new Constant(0);
            this.container.removeNodeSubtreeFromEquation(this.leftChild);
            this.container.removeNodeSubtreeFromEquation(this.rightChild);
            this.leftChild = null;
            this.rightChild = null;
            this.statistics.onChange();
          }
        } else if (opDataNode.getChar().equals("/")) {
          if (this.getRightChild().dataNode.getClass() == Constant.class &&
              (this.getRightChild().eval() == 0 || this.getRightChild().eval() == 1)) {
//            swapper.addToQueue(this, this.getLeftChild().clone(null));
            TreeNode.swapNodes(this, this.getLeftChild().clone(null));
          }
        } else if (opDataNode.getChar().equals("*")) {
          if ((this.getRightChild().dataNode.getClass() == Constant.class
              && this.getRightChild().eval() == 0)
              || (this.getLeftChild().dataNode.getClass() == Constant.class
              && this.getLeftChild().eval() == 0)) {
            this.dataNode = new Constant(0);
            this.container.removeNodeSubtreeFromEquation(this.getLeftChild());
            this.container.removeNodeSubtreeFromEquation(this.getRightChild());
            this.leftChild = null;
            this.rightChild = null;
            this.statistics.onChange();
          }
        }
      }
    }
//    if (this.getParent() == null) {
//      // root node
//      swapper.performSwaps();
//    }
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
      clone = new TreeNode(newContainer, newDataNode, leftChildClone, rightChildClone);
    } else {
      clone = new TreeNode(newContainer, newDataNode);
    }

    if (newContainer != null) {
      newContainer.getNodeList().add(clone);
    }

    return clone;
  }

  public JSONObject json() {
    JSONObject obj = new JSONObject();
    obj.put("Type", this.dataNode.getClass().getName());

    if (this.dataNode.getClass() == Operator.class) {
      obj.put("Operator", ((Operator)this.dataNode).getChar());
      obj.put("Left", this.getLeftChild().json());
      obj.put("Right", this.getRightChild().json());
    } else if (this.dataNode.getClass() == Constant.class) {
      obj.put("Constant", ((Constant)this.dataNode).eval());
    } else if (this.dataNode.getClass() == Variable.class) {
      obj.put("Variable", ((Variable)this.dataNode).getName());
    }
    return obj;
  }

  public static TreeNode fromJson(JSONObject obj, Equation container)
      throws ClassNotFoundException {

    TreeNode left = null;
    TreeNode right = null;
    TreeNode node = null;
    if (obj.has("Left")) {
      left = fromJson((JSONObject) obj.get("Left"), container);
    }
    if (obj.has("Right")) {
      right = fromJson((JSONObject) obj.get("Right"), container);
    }
    Class nodeType = Class.forName(obj.getString("Type"));
    if (nodeType == Operator.class) {
      Operator dataNode = new Operator(Operator.getOpChar(obj.getString("Operator")));
      node = new TreeNode(container, dataNode, left, right);
    } else if (nodeType == Variable.class) {
      Optional<Variable> dataNode = DATASET.getVariableByName(obj.getString("Variable"));
      if (dataNode.isPresent()) {
        node = new TreeNode(container, dataNode.get());
      } else {
        log.error("Unable to find variable with name '{}'", obj.getString("Variable"));
      }
    } else if (nodeType == Constant.class) {
      Constant dataNode = new Constant(obj.getDouble("Constant"));
      node = new TreeNode(container, dataNode);
    }
    return node;
  }

  public enum WhichChild {
    LEFT, RIGHT
  }
}
