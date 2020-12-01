package org.brokencircuits.equationestimator2.domain;

public interface TreeNodeDataType<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> {

  void setContainer(TreeNode<T, U> container);

  T clone();
}
