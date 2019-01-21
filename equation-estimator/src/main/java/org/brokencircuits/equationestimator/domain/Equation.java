package org.brokencircuits.equationestimator.domain;

import static org.brokencircuits.equationestimator.util.Chance.addOperatorNode;
import static org.brokencircuits.equationestimator.util.Chance.addVariableNode;
import static org.brokencircuits.equationestimator.util.Chance.randConstant;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.IDataNode;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;

@RequiredArgsConstructor
@Slf4j
public class Equation {

  private static Dataset dataset = Dataset.getInstance();
  @Getter
  @Setter
  private TreeNode root;
  @Getter
  private List<TreeNode> nodeList = Lists.newArrayList();
  @Setter
  private Double lastFitness = -1D;

  /* ****************** Static Functions ****************** */
  static public String nodeListReadable(List<TreeNode> list) {
    StringBuilder sb = new StringBuilder();
    for (TreeNode node : list) {
      sb.append(node.toString()).append("\n");
    }
    return sb.toString();
  }

  static public Equation generateRandom(int avgNumOperators) throws IllegalStateException {

    Equation newEq = new Equation();
    TreeNode rootNode = Equation.generateRandom(avgNumOperators, 0, newEq);
    newEq.setRoot(rootNode);
    return newEq;
  }

  static public TreeNode generateRandom(final int avgOpNodesDesired, int currentDepth,
      Equation container) throws IllegalStateException {

    IDataNode newDataNode;
    TreeNode newTreeNode;
    if (addOperatorNode(avgOpNodesDesired, currentDepth)) {
      TreeNode leftChild = generateRandom(avgOpNodesDesired, currentDepth + 1, container);
      TreeNode rightChild = generateRandom(avgOpNodesDesired, currentDepth + 1, container);
      newDataNode = new Operator(Operator.randOpChar());
      newTreeNode = new TreeNode(container, newDataNode, leftChild, rightChild);
    } else {
      if (addVariableNode()) {
        Optional<Variable> chosen = dataset.getRandom();
        if (chosen.isPresent()) {
          newDataNode = chosen.get();
        } else {
          log.error("Cannot add variable");
          throw new IllegalStateException(
              "Unable to generate tree node as there are no variables to use");
        }
      } else {
        newDataNode = new Constant((double) randConstant());
      }
      newTreeNode = new TreeNode(container, newDataNode);
    }

    container.nodeList.add(newTreeNode);

    return newTreeNode;
  }

  public Double getLastFitness() {
    if (lastFitness < 0) {
      Generation.evaluateEquation(this);
    }
    return lastFitness;
  }

  /* *************************** Public Function *************************** */

  public Statistic statistic() {
    return root.getStatistics();
  }

  public Equation clone() {
    Equation newEq = new Equation();
    newEq.setRoot(this.root.clone(newEq));
    return newEq;
  }

  public void replaceNode(TreeNode initial, TreeNode replacement) {
    log.info("Equation to replace node in: {}.\n\tReplacing node {} with node {}", this,
        initial.getId(), replacement.getId());

    if (nodeList.contains(initial)) {
      removeNodeSubtreeFromEquation(initial);
      TreeNode.swapNodes(initial, replacement);
      addNodeSubtreeToEquation(replacement);
    }
  }

  public void addNodeSubtreeToEquation(TreeNode initial) {
    nodeList.add(initial);
    initial.setContainer(this);
    if (initial.getLeftChild() != null) {
      addNodeSubtreeToEquation(initial.getLeftChild());
    }
    if (initial.getRightChild() != null) {
      addNodeSubtreeToEquation(initial.getRightChild());
    }
  }

  public void removeNodeSubtreeFromEquation(TreeNode initial) {
    nodeList.remove(initial);
    if (initial.getLeftChild() != null) {
      removeNodeSubtreeFromEquation(initial.getLeftChild());
    }
    if (initial.getRightChild() != null) {
      removeNodeSubtreeFromEquation(initial.getRightChild());
    }
  }

  public double eval() {
    return root.eval();
  }

  public String equationReadable(boolean replaceVariablesWithValues) {
    StringBuilder sb = new StringBuilder();
    sb.append(root.equationReadable(replaceVariablesWithValues));
    if (replaceVariablesWithValues) {
      sb.append(" = ").append(this.eval());
    }
    return sb.toString();
  }

  public String equationTree() {
    List<String> treeNodeStrings = TreeNode.equationTreeNode(root);
    return TreeNode.treeStringsToString(treeNodeStrings);
  }

  public void simplify() {
    root.simplify();
  }

}
