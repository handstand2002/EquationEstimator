package org.brokencircuits.equationestimator2.domain;

public class DefaultTreeNodePrinter<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> implements
    TreeNodePrinter<T, U> {

  @Override
  public String printTree(Tree<T, U> tree) {
    return printTreeNode(tree.getRootNode());
  }

  private String printTreeNode(TreeNode<T, U> node) {
    return printTreeNode(0, node);
  }

  private String printTreeNode(int depth, TreeNode<T, U> node) {
    return printTreeNodePrefix(depth)
        + printNodeOrientation(node) + node.getData()
        + ((node.getLeft() != null) ? printTreeNode(depth + 1, node.getLeft()) : "")
        + ((node.getRight() != null) ? printTreeNode(depth + 1, node.getRight()) : "");
  }

  private String printNodeOrientation(TreeNode<T, U> node) {
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

  private String printTreeNodePrefix(int depth) {
    StringBuilder sb = new StringBuilder("\n");
    for (int i = 0; i < depth; i++) {
      sb.append("|  ");
    }
    return sb.toString();
  }
}
