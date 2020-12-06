package org.brokencircuits.equationestimator2.eq;

import org.brokencircuits.equationestimator2.domain.Tree;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeNodePrinter;

public class EquationTreePrinter implements TreeNodePrinter<EquationNode, EquationTree> {

  @Override
  public String printTree(Tree<EquationNode, EquationTree> tree) {
    return printEquationNode(tree.getRootNode());
  }

  private String printEquationNode(TreeNode<EquationNode, EquationTree> node) {

    if (node.getData().getNodeType() != EquationNodeType.OPERATOR) {
      return String.valueOf(node.getData());
    }
    String pattern = "(%s %s %s)";
    if (node.getParent() == null
        || (node.getData().getOperator().getPriority() <= node.getParent().getData().getOperator()
        .getPriority())) {
      // don't need to use parentheses if parent node is a lower priority operation, or if node is root
      pattern = "%s %s %s";
    }
    return String
        .format(pattern, printChild(node.getLeft()), node.getData(), printChild(node.getRight()));
  }

  private String printChild(TreeNode<EquationNode, EquationTree> node) {
    if (node != null) {
      return printEquationNode(node);
    }
    return "";
  }
}
