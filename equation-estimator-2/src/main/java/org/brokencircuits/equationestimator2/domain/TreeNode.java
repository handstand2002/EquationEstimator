package org.brokencircuits.equationestimator2.domain;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter(AccessLevel.PACKAGE)
public class TreeNode<T extends TreeNodeDataType<T>> {

  private final Tree<T> tree;
  private final TreeNode<T> parent;
  private TreeNode<T> left;
  private TreeNode<T> right;
  private int depth;
  private TreeNodeOrientation side;
  private final T data;

  public <U> U applyConsumer(ChildAwareConsumer<T, U> consumer) {
    data.setContainer(this);
    return consumer.apply(data, opt(left).map(TreeNode::getData).orElse(null),
        opt(right).map(TreeNode::getData).orElse(null));
  }

  private static <U> Optional<U> opt(U value) {
    return Optional.ofNullable(value);
  }
}
