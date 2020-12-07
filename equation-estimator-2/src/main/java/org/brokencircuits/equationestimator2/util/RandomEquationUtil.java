package org.brokencircuits.equationestimator2.util;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator2.domain.Tree;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeNodeOrientation;
import org.brokencircuits.equationestimator2.eq.EquationNode;
import org.brokencircuits.equationestimator2.eq.EquationNodeType;
import org.brokencircuits.equationestimator2.eq.EquationOperator;
import org.brokencircuits.equationestimator2.eq.EquationTree;
import org.brokencircuits.equationestimator2.eq.EquationVariableReference;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomEquationUtil {

  public static EquationTree createRandomTree(List<EquationVariableReference> availableVariables) {

    EquationTree tree = new EquationTree();

    createSubNodes(tree, availableVariables);

    return tree;
  }

  private static TreeNode<EquationNode, EquationTree> createSubNodes(
      Tree<EquationNode, EquationTree> tree,
      List<EquationVariableReference> availableVariables) {

    return createSubtree(availableVariables,
        node -> tree.newNode(node.getParent(), node.getSide(), node));
  }

  public static TreeNode<EquationNode, EquationTree> createSubtree(
      List<EquationVariableReference> availableVariables,
      Consumer<TreeNode<EquationNode, EquationTree>> creationCallback) {
    MethodCallerTracker.onMethodCall("RandomEquationUtil#createSubtree(int, int, List, Consumer)");

    TreeNodeOrientation forSide = null;
    TreeNode<EquationNode, EquationTree> parent = null;
    TreeNode<EquationNode, EquationTree> root = null;
    Queue<TreeNode<EquationNode, EquationTree>> operatorsToFulfill = new LinkedBlockingQueue<>();
    do {

      if (parent == null && !operatorsToFulfill.isEmpty()) {
        // hit this once, immediately after processing the root node
        parent = operatorsToFulfill.poll();
        forSide = TreeNodeOrientation.LEFT;

      } else if (parent != null) {
        if (forSide == TreeNodeOrientation.LEFT) {
          // hit this after processing the left node of an operator
          forSide = TreeNodeOrientation.RIGHT;
        } else {
          // hit this after processing the right node of an operator (completed processing that op)
          parent = operatorsToFulfill.poll();
          forSide = TreeNodeOrientation.LEFT;
        }
      }

      // choose new node type
      EquationNodeType newNodeType = newNodeType();

      // Safeguard against having 0 available variables, switch it to a constant
      if (newNodeType == EquationNodeType.VARIABLE && availableVariables.isEmpty()) {
        newNodeType = EquationNodeType.CONSTANT;
      }

      TreeNode<EquationNode, EquationTree> newNode = TreeNode.<EquationNode, EquationTree>builder()
          .data(newNode(newNodeType, availableVariables))
          .left(null)
          .right(null)
          .parent(parent)
          .side(forSide)
          .build();

      if (parent != null) {
        parent.setChild(forSide, newNode);
      }

      if (root == null) {
        root = newNode;
      }

      Optional.ofNullable(creationCallback).ifPresent(c -> c.accept(newNode));

      if (newNodeType == EquationNodeType.OPERATOR) {
        operatorsToFulfill.add(newNode);
      }

      newNode.getData().setContainer(newNode);
      // if forSide is LEFT, need to process one more node for RIGHT
    } while (!operatorsToFulfill.isEmpty() || forSide == TreeNodeOrientation.LEFT);

    return root;
  }

  private static EquationNode newVariable(List<EquationVariableReference> availableVariables) {
    return new EquationNode(
        availableVariables.get(RandomUtil.RANDOM.nextInt(availableVariables.size())));
  }

  private static EquationNode newNode(EquationNodeType type,
      List<EquationVariableReference> availableVariables) {
    switch (type) {
      case VARIABLE:
        return newVariable(availableVariables);
      case CONSTANT:
        return newConstant();
      case OPERATOR:
        return newOperator();
      default:
        throw new IllegalStateException("Unknown node type: " + type);
    }
  }

  private static EquationNode newConstant() {
    int i = RandomUtil.RANDOM.nextInt(2);
    // equal chance being an increasing factor (i.e. >1 || <-1) or decreasing factor (<1 && >-1)
    double useValue;
    if (i == 0) {
      // increasing factor
      useValue = RandomUtil.RANDOM.nextLong() % 10;
      useValue += useValue > 0 ? 1 : -1;  // make useValue -10 to -1 or 1 to 10
    } else {
      // decreasing factor
      useValue = (RandomUtil.RANDOM.nextLong() % 100) / (double) 100;
    }

    return new EquationNode(useValue);
  }

  private static EquationNode newOperator() {
    EquationOperator[] availableValues = EquationOperator.values();
    int i = RandomUtil.RANDOM.nextInt(availableValues.length);
    return new EquationNode(availableValues[i]);
  }

  private static EquationNodeType newNodeType() {
    int r = RandomUtil.RANDOM.nextInt(100);

    if (r < 50) {
      return EquationNodeType.OPERATOR;
    } else {
      int i = RandomUtil.RANDOM.nextInt(100);
      if (i < 40) {
        return EquationNodeType.CONSTANT;
      } else {
        return EquationNodeType.VARIABLE;
      }
    }
  }
}
