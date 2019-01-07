package org.brokencircuits.equationestimator.domain;

import static org.brokencircuits.equationestimator.util.Chance.addOperatorNode;
import static org.brokencircuits.equationestimator.util.Chance.addVariableNode;
import static org.brokencircuits.equationestimator.util.Chance.randConstant;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.IDataNode;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;

@RequiredArgsConstructor
public class Equation {

  @NonNull
  final private TreeNode root;

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

  /* ****************** Static Functions ****************** */
  static public Equation generateRandom(int avgNumOperators) {

    TreeNode rootNode = Equation.generateRandom(avgNumOperators, 0);
    return new Equation(rootNode);
  }

  static public TreeNode generateRandom(final int avgOpNodesDesired, int currentDepth) {

    IDataNode newDataNode;
    TreeNode newTreeNode;
    if (addOperatorNode(avgOpNodesDesired, currentDepth)) {
      TreeNode leftChild = generateRandom(avgOpNodesDesired, currentDepth + 1);
      TreeNode rightChild = generateRandom(avgOpNodesDesired, currentDepth + 1);
      newDataNode = new Operator(Operator.randOpChar());
      newTreeNode = new TreeNode(newDataNode, leftChild, rightChild);
    } else {
      if (addVariableNode()) {
        newDataNode = new Variable();
        ((Variable) newDataNode).setValue(randConstant());
        // TODO: Make any new variables retrieve variables from predefined pool
      } else {
        newDataNode = new Constant((double) randConstant());
      }
      newTreeNode = new TreeNode(newDataNode);
    }

    return newTreeNode;
  }

}
