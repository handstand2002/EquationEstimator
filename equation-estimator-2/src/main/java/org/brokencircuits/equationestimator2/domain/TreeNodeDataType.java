package org.brokencircuits.equationestimator2.domain;

public interface TreeNodeDataType<U extends TreeNodeDataType> {

  void setContainer(TreeNode<U> container);
}
