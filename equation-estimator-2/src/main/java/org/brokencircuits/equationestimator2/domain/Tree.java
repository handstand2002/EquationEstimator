package org.brokencircuits.equationestimator2.domain;

import lombok.Data;

@Data
public class Tree<T> {

  private TreeNode<T> rootNode;
  private final TreeStatistics stats = new TreeStatistics();
  private TreeNodePrinter<T> printer = new DefaultTreeNodePrinter<>();

  public TreeNode<T> setRootNode(T data) {
    rootNode = TreeNode.<T>builder()
        .data(data)
        .left(null)
        .right(null)
        .parent(null)
        .tree(this)
        .depth(0)
        .side(null)
        .build();
    stats.incrementNodeCount();
    return rootNode;
  }

  public TreeNode<T> newNode(TreeNode<T> parent,
      TreeNodeOrientation side, T data) {
    TreeNode<T> newNode = TreeNode.<T>builder()
        .side(side)
        .depth(parent.getDepth() + 1)
        .tree(this)
        .parent(parent)
        .right(null)
        .left(null)
        .data(data)
        .build();

    if (side == TreeNodeOrientation.LEFT) {
      if (parent.getLeft() != null) {
        throw new IllegalArgumentException("Cannot overwrite node's child");
      }
      parent.setLeft(newNode);
    } else {
      if (parent.getRight() != null) {
        throw new IllegalArgumentException("Cannot overwrite node's child");
      }
      parent.setRight(newNode);
    }
    stats.incrementNodeCount();
    return newNode;
  }

  public String toString() {
    return printer.printTree(this);
  }
}
