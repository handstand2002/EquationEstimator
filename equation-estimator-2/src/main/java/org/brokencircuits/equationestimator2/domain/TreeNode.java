package org.brokencircuits.equationestimator2.domain;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brokencircuits.equationestimator2.util.MethodCallerTracker;
import org.jetbrains.annotations.NotNull;

@Builder
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class TreeNode<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> {

  @Getter
  @Setter
  public static class RootNodeState<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> {

    private U tree;
  }

  private RootNodeState<T, U> rootNodeState = null;
  private TreeNode<T, U> parent;
  private TreeNode<T, U> left;
  private TreeNode<T, U> right;
  private TreeNodeOrientation side;
  private T data;

  public boolean isRoot() {
    return rootNodeState != null;
  }

  public int getDepth() {
    int depth = 0;
    TreeNode<T, U> crawler = this;
    while (!crawler.isRoot()) {
      crawler = crawler.getParent();
      depth++;
    }
    return depth;
  }

  @NotNull
  public U getTree() {
    TreeNode<T, U> crawler = this;
    while (!crawler.isRoot()) {
      crawler = crawler.getParent();
    }
    return crawler.getRootNodeState().getTree();
  }

  public <V> V applyConsumer(ChildAwareConsumer<T, V> consumer) {
    data.setContainer(this);
    return consumer.apply(data, Optional.ofNullable(left)
            .map(TreeNode::getData)
            .orElse(null),
        Optional.ofNullable(right)
            .map(TreeNode::getData)
            .orElse(null));
  }

  public void setChild(TreeNodeOrientation side, TreeNode<T, U> child) {
    MethodCallerTracker.onMethodCall("TreeNode#setChild(TreeNodeOrientation, TreeNode)");
    if (side == TreeNodeOrientation.LEFT) {
      this.left = child;
      this.left.setParent(this);
    } else if (side == TreeNodeOrientation.RIGHT) {
      this.right = child;
      this.right.setParent(this);
    } else {
      throw new IllegalStateException("Unknown side: " + side);
    }
    updateChildren();

  }

  void updateChildren() {
    MethodCallerTracker.onMethodCall("TreeNode#updateChildren");
    if (left != null) {
      if (left.getParent() != this) {
        left.setParent(this);
        left.updateChildren();
      }
    }
    if (right != null) {
      if (right.getParent() != this) {
        right.setParent(this);
        right.updateChildren();
      }
    }
  }

}
