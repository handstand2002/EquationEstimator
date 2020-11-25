package org.brokencircuits.equationestimator2.domain;

public class DefaultTreeNodePrinter<T> implements TreeNodePrinter<T> {

  @Override
  public String printTree(Tree<T> tree) {
    return printTreeNode(tree.getRootNode());
  }

  private String printTreeNode(TreeNode<T> node) {
    return printTreeNodePrefix(node.getDepth())
        + printNodeOrientation(node) + node.getData()
        + printChild(node.getLeft())
        + printChild(node.getRight());
  }

  private String printNodeOrientation(TreeNode<T> node) {
    if (node == null || node.getSide() == null) {
      return "";
    }
    switch (node.getSide()) {
      case LEFT:
        return "L ";
      case RIGHT:
        return "R ";
      default:
        return "";
    }
  }

  private String printChild(TreeNode<T> node) {
    if (node != null) {
      return printTreeNode(node);
    }
    return "";
  }

  private String printTreeNodePrefix(int depth) {
    StringBuilder sb = new StringBuilder("\n");
    for (int i = 0; i < depth; i++) {
      sb.append("|  ");
    }
    return sb.toString();
  }
}
