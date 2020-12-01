package org.brokencircuits.equationestimator2.domain;

public interface TreeNodePrinter<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> {

  String printTree(Tree<T, U> tree);
}
