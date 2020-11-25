package org.brokencircuits.equationestimator2.eq;

import org.brokencircuits.equationestimator2.domain.Tree;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeNodePrinter;

public class EquationTreePrinter implements TreeNodePrinter<EquationNode> {

  @Override
  public String printTree(Tree<EquationNode> tree) {
    return printEquationNode(tree.getRootNode());
  }

  private <T> String printEquationNode(TreeNode<EquationNode> node) {

    if (node.getData().isTerminal()) {
      return String.valueOf(node.getData());
    }
    return String.format("%s %s %s", printChild(node.getLeft()), node.getData(),
        printChild(node.getRight()));
  }

  private <T> String printChild(TreeNode<EquationNode> node) {
    if (node != null) {
      return printEquationNode(node);
    }
    return "";
  }
}
