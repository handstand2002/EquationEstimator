package org.brokencircuits.equationestimator2.domain;

import static org.brokencircuits.equationestimator2.domain.TreeNodeOrientation.LEFT;
import static org.brokencircuits.equationestimator2.domain.TreeNodeOrientation.RIGHT;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.brokencircuits.equationestimator2.domain.TreeNode.RootNodeState;
import org.jetbrains.annotations.NotNull;

@Getter
@NoArgsConstructor
public class Tree<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> implements Cloneable {

  @Setter(AccessLevel.PROTECTED)
  protected TreeNode<T, U> rootNode;
  @Setter(AccessLevel.PROTECTED)
  protected TreeStatistics stats = new TreeStatistics();
  @Setter
  protected TreeNodePrinter<T, U> printer = new DefaultTreeNodePrinter<>();

  private TreeNode<T, U> newRootNode(TreeNode<T, U> data) {
    rootNode = data;
    stats.incrementNodeCount();
    data.getData().setContainer(rootNode);
    RootNodeState<T, U> rootState = new RootNodeState<>();
    rootState.setTree((U) this);
    data.setRootNodeState(rootState);
    data.updateChildren();
    return rootNode;
  }

  public TreeNode<T, U> newNode(TreeNode<T, U> parent,
      TreeNodeOrientation side, @NotNull T data) {
    TreeNode<T, U> newNode = TreeNode.<T, U>builder()
        .side(side)
        .parent(parent)
        .right(null)
        .left(null)
        .data(data)
        .build();

    return newNode(parent, side, newNode);
  }

  public TreeNode<T, U> newNode(TreeNode<T, U> parent,
      TreeNodeOrientation side, @NotNull TreeNode<T, U> newNode) {
    if (side == null && parent == null) {
      return newRootNode(newNode);
    }

    if (side == LEFT) {
      if (parent.getLeft() != null && parent.getLeft() != newNode) {
        throw new IllegalArgumentException("Cannot overwrite node's child");
      }
      parent.setChild(LEFT, newNode);
    } else {
      if (parent.getRight() != null && parent.getRight() != newNode) {
        throw new IllegalArgumentException("Cannot overwrite node's child");
      }
      parent.setChild(RIGHT, newNode);
    }
    newNode.getData().setContainer(newNode);
    stats.incrementNodeCount();
    newNode.updateChildren();
    return newNode;
  }

  public String toString() {
    return printer.printTree(this);
  }

  public void swap(@NotNull TreeNode<T, U> node1, @NotNull TreeNode<T, U> node2) {

    TreeNode<T, U> node2Parent = node2.getParent();
    TreeNodeOrientation node2Side = node2.getSide();

    node2.setParent(node1.getParent());
    node2.setSide(node1.getSide());

    node1.setParent(node2Parent);
    node1.setSide(node2Side);

    // update new parent node to link to the new child
    updateParentLink(node1);
    updateParentLink(node2);

    // swap root states, in case one or both of them were root nodes
    RootNodeState<T, U> node1RootState = node1.getRootNodeState();
    RootNodeState<T, U> node2RootState = node2.getRootNodeState();
    node1.setRootNodeState(node2RootState);
    node2.setRootNodeState(node1RootState);

    if (node1.isRoot()) {
      node1.getRootNodeState().getTree().setRootNode(node1);
    }
    if (node2.isRoot()) {
      node2.getRootNodeState().getTree().setRootNode(node2);
    }
  }

  private void updateParentLink(TreeNode<T, U> node) {
    if (node.getParent() != null) {
      if (node.getSide() == LEFT) {
        node.getParent().setChild(LEFT, node);
      } else if (node.getSide() == TreeNodeOrientation.RIGHT) {
        node.getParent().setChild(RIGHT, node);
      }
    }
  }

  public Tree<T, U> clone() {
    Tree<T, U> newTree = new Tree<>();
    newTree.printer = this.printer;
    TreeNode<T, U> rootNode = this.rootNode;

    Queue<NodeReplacementDetails<T, U>> nodesToProcess = new LinkedBlockingQueue<>();
    nodesToProcess.add(new NodeReplacementDetails<>(rootNode));

    while (!nodesToProcess.isEmpty()) {
      NodeReplacementDetails<T, U> processNode = nodesToProcess.poll();
      TreeNode<T, U> originalNode = processNode.getOriginal();

      // the first node will have null clonedParent and orientation, which triggers setting root node of tree
      TreeNode<T, U> clonedNode = newTree
          .newNode(processNode.getClonedParent(), processNode.getOrientation(),
              originalNode.getData().clone());

      if (originalNode.getLeft() != null) {
        NodeReplacementDetails<T, U> leftReplacementDetails = new NodeReplacementDetails<>(
            originalNode.getLeft());
        leftReplacementDetails.setClonedParent(clonedNode);
        leftReplacementDetails.setOrientation(LEFT);
        nodesToProcess.add(leftReplacementDetails);
      }

      if (originalNode.getRight() != null) {
        NodeReplacementDetails<T, U> rightReplacementDetails = new NodeReplacementDetails<>(
            originalNode.getRight());
        rightReplacementDetails.setClonedParent(clonedNode);
        rightReplacementDetails.setOrientation(TreeNodeOrientation.RIGHT);
        nodesToProcess.add(rightReplacementDetails);
      }
    }

    return newTree;
  }

  @Data
  @RequiredArgsConstructor
  private static class NodeReplacementDetails<T extends TreeNodeDataType<T, U>, U extends Tree<T, U>> {

    private final TreeNode<T, U> original;
    private TreeNode<T, U> cloned;
    private TreeNode<T, U> clonedParent;
    private TreeNodeOrientation orientation;
  }

}
