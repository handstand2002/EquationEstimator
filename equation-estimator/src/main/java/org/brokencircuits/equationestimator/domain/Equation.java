package org.brokencircuits.equationestimator.domain;

import static org.brokencircuits.equationestimator.util.Chance.addOperatorNode;
import static org.brokencircuits.equationestimator.util.Chance.addVariableNode;
import static org.brokencircuits.equationestimator.util.Chance.randConstant;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.IDataNode;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;

@RequiredArgsConstructor
@Slf4j
public class Equation {

  private TreeNode root;
  private boolean rootIsSet = false;
  @Getter
  private List<TreeNode> nodeList = Lists.newArrayList();
  private static Dataset dataset = Dataset.getInstance();

  /* ****************** Static Functions ****************** */
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

  /* *************************** Public Function *************************** */

  public Equation clone() {
    Equation newEq = new Equation();
    newEq.setRoot(this.root.cloneTree());
    // TODO: Finish this
    return null;
  }

  public void removeNodeSubtreeFromList(TreeNode initial) {
    nodeList.remove(initial);
    if (initial.getLeftChild() != null) {
      removeNodeSubtreeFromList(initial.getLeftChild());
    }
    if (initial.getRightChild() != null) {
      removeNodeSubtreeFromList(initial.getRightChild());
    }
  }

  public double eval() {
    return root.eval();
  }

  public String equationReadable(boolean replaceVariablesWithValues) {
    return root.equationReadable(replaceVariablesWithValues);
  }

  public String equationTree() {
    List<String> treeNodeStrings = TreeNode.equationTreeNode(root);
    return String.join("\n", treeNodeStrings);
  }

  public void simplify() {
    root.simplify();
  }

  public void setRoot(TreeNode root) {
    if (!rootIsSet) {
      this.root = root;
      rootIsSet = true;
    } else {
      log.error("Cannot re-set root node of equation: {}", this);
    }

  }
}
