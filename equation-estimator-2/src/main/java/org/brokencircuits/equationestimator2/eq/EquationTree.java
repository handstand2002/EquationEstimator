package org.brokencircuits.equationestimator2.eq;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator2.domain.Tree;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeNodeOrientation;
import org.brokencircuits.equationestimator2.util.MethodCallerTracker;
import org.brokencircuits.equationestimator2.util.RandomUtil;
import org.brokencircuits.evolve.AttributeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class EquationTree extends Tree<EquationNode, EquationTree> implements
    AttributeType<EquationTree> {

  @Getter
  private final Map<EquationNodeType, Set<TreeNode<EquationNode, EquationTree>>> nodesByType = new HashMap<>();

  @Override
  public EquationTree clone() {

    MethodCallerTracker.onMethodCall("EquationTree#clone");
    Tree<EquationNode, EquationTree> clone = super.clone();
    EquationTree newTree = new EquationTree();
    newTree.setRootNode(clone.getRootNode());
    newTree.setStats(clone.getStats());
    newTree.setPrinter(clone.getPrinter());

    clone.getRootNode().getRootNodeState().setTree(newTree);
    log.debug("Cloned tree");
    return newTree;
  }

  @Override
  public TreeNode<EquationNode, EquationTree> newNode(TreeNode<EquationNode, EquationTree> parent,
      TreeNodeOrientation side,
      @NotNull EquationNode data) {
    MethodCallerTracker
        .onMethodCall("EquationTree#newNode(TreeNode, TreeNodeOrientation, EquationNode)");
    TreeNode<EquationNode, EquationTree> newNode = super.newNode(parent, side, data);
    nodesByType.computeIfAbsent(newNode.getData().getNodeType(), k -> new HashSet<>())
        .add(newNode);
    newNode.getData().setContainer(newNode);
    log.debug("Created new node");
    return newNode;
  }

  @Override
  protected void setRootNode(TreeNode<EquationNode, EquationTree> rootNode) {
    MethodCallerTracker.onMethodCall("EquationTree#setRootNode(TreeNode)");
    super.setRootNode(rootNode);
    updateNodeMap(rootNode);
    log.debug("Set root node");
  }

  protected void updateNodeMap(TreeNode<EquationNode, EquationTree> node) {
    MethodCallerTracker.onMethodCall("EquationTree#updateNodeMap(TreeNode)");
    boolean uncounted = nodesByType
        .computeIfAbsent(node.getData().getNodeType(), k -> new HashSet<>())
        .add(node);
    node.getData().setContainer(node);
    if (uncounted) {
      if (node.getLeft() != null) {
        updateNodeMap(node.getLeft());
      }
      if (node.getRight() != null) {
        updateNodeMap(node.getRight());
      }
    }
    log.debug("Updated node map");
  }

  @Override
  public TreeNode<EquationNode, EquationTree> newNode(TreeNode<EquationNode, EquationTree> parent,
      TreeNodeOrientation side,
      @NotNull TreeNode<EquationNode, EquationTree> newNode) {
    MethodCallerTracker
        .onMethodCall("EquationTree#newNode(TreeNode, TreeNodeOrientation, TreeNode)");
    newNode.getData().setContainer(newNode);

    TreeNode<EquationNode, EquationTree> newNode1 = super.newNode(parent, side, newNode);
    updateNodeMap(newNode);
    return newNode1;
  }


  private void addToNodeCounts(Map<EquationNodeType, AtomicLong> nodeCounts,
      TreeNode<EquationNode, EquationTree> node) {
    nodeCounts.computeIfAbsent(node.getData().getNodeType(), k -> new AtomicLong(0))
        .incrementAndGet();

    if (node.getLeft() != null) {
      addToNodeCounts(nodeCounts, node.getLeft());
    }
    if (node.getRight() != null) {
      addToNodeCounts(nodeCounts, node.getRight());
    }
  }

  public boolean audit() {
    TreeNode<EquationNode, EquationTree> rootNode = getRootNode();
    Map<EquationNodeType, AtomicLong> nodeCounts = new HashMap<>();
    addToNodeCounts(nodeCounts, rootNode);

    long actualOpCount = nodeCounts.getOrDefault(EquationNodeType.OPERATOR, new AtomicLong(0))
        .get();
    long actualVarCount = nodeCounts.getOrDefault(EquationNodeType.VARIABLE, new AtomicLong(0))
        .get();
    long actualConstantCount = nodeCounts.getOrDefault(EquationNodeType.CONSTANT, new AtomicLong(0))
        .get();

    Map<EquationNodeType, Set<TreeNode<EquationNode, EquationTree>>> trackedNodesByType = getNodesByType();

    long trackedOpCount = trackedNodesByType
        .getOrDefault(EquationNodeType.OPERATOR, Collections.emptySet()).size();
    long trackedVarCount = trackedNodesByType
        .getOrDefault(EquationNodeType.VARIABLE, Collections.emptySet()).size();
    long trackedConstantCount = trackedNodesByType
        .getOrDefault(EquationNodeType.CONSTANT, Collections.emptySet()).size();

    if (actualOpCount != trackedOpCount) {
      log.info("Actual Op Count: {}; tracked: {}", actualOpCount, trackedOpCount);
      return false;
    }
    if (actualVarCount != trackedVarCount) {
      log.info("Actual var Count: {}; tracked: {}", actualVarCount, trackedVarCount);
      return false;
    }
    if (actualConstantCount != trackedConstantCount) {
      log.info("Actual const Count: {}; tracked: {}", actualConstantCount, trackedConstantCount);
      return false;
    }
    return true;
  }

  @Nullable
  public TreeNode<EquationNode, EquationTree> randomOperatorNode() {
    MethodCallerTracker.onMethodCall("EquationTree#randomOperatorNode");
    Set<TreeNode<EquationNode, EquationTree>> nodeSet = nodesByType.get(EquationNodeType.OPERATOR);
    if (nodeSet == null || nodeSet.isEmpty()) {
      return rootNode;
    }
    return nodeSet.stream().skip(RandomUtil.RANDOM.nextInt(nodeSet.size())).findFirst()
        .orElse(null);
  }

  @Nullable
  private EquationTree getNodeTree(TreeNode<EquationNode, EquationTree> node) {
    while (node != null && !node.isRoot()) {
      node = node.getParent();
    }
    return node != null ? node.getRootNodeState().getTree() : null;
  }

  @Override
  public void swap(@NotNull TreeNode<EquationNode, EquationTree> node1,
      @NotNull TreeNode<EquationNode, EquationTree> node2) {
    MethodCallerTracker.onMethodCall("EquationTree#swap(TreeNode, TreeNode)");

    EquationTree node1Tree = getNodeTree(node1);
    EquationTree node2Tree = getNodeTree(node2);
    super.swap(node1, node2);

    Queue<TreeNode<EquationNode, EquationTree>> nodesToProcess = new LinkedBlockingQueue<>();

    // remove all nodes in the subtree of the old node from allNodes set
    nodesToProcess.add(node1);
    while (!nodesToProcess.isEmpty()) {
      TreeNode<EquationNode, EquationTree> nodeFromTree1 = nodesToProcess.poll();
      if (node1Tree != null) {
        node1Tree.nodesByType.computeIfPresent(nodeFromTree1.getData().getNodeType(), (k, v) -> {
          v.remove(nodeFromTree1);
          return v;
        });
      }
      if (node2Tree != null) {
        node2Tree.nodesByType
            .computeIfAbsent(nodeFromTree1.getData().getNodeType(), k -> new HashSet<>())
            .add(nodeFromTree1);
      }

      Optional.ofNullable(nodeFromTree1.getLeft()).ifPresent(nodesToProcess::add);
      Optional.ofNullable(nodeFromTree1.getRight()).ifPresent(nodesToProcess::add);
    }

    // add all nodes in the subtree of the new node to allNodes set
    nodesToProcess.add(node2);
    while (!nodesToProcess.isEmpty()) {
      TreeNode<EquationNode, EquationTree> nodeFromTree2 = nodesToProcess.poll();
      if (node2Tree != null) {
        node2Tree.nodesByType.computeIfPresent(nodeFromTree2.getData().getNodeType(), (k, v) -> {
          v.remove(nodeFromTree2);
          return v;
        });
      }
      if (node1Tree != null) {
        node1Tree.nodesByType
            .computeIfAbsent(nodeFromTree2.getData().getNodeType(), k -> new HashSet<>())
            .add(nodeFromTree2);
      }

      Optional.ofNullable(nodeFromTree2.getLeft()).ifPresent(nodesToProcess::add);
      Optional.ofNullable(nodeFromTree2.getRight()).ifPresent(nodesToProcess::add);
    }
    log.debug("Swapped nodes");
  }

  private String printMap(Map<EquationNodeType, Set<TreeNode<EquationNode, EquationTree>>> map) {
    StringBuilder sb = new StringBuilder();
    map.forEach((type, set) -> {
      sb.append("[").append(type.name()).append("]\n");
      set.forEach(node -> sb.append("\t").append(node.hashCode()).append("\n"));
    });
    return sb.toString();
  }
}
