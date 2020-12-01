package org.brokencircuits.equationestimator2.util;

import java.util.Collections;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator2.eq.EquationTree;
import org.junit.Test;

@Slf4j
public class RandomEquationUtilTest {

  @Test
  public void testSubCreation() {
    RandomUtil.RANDOM = new Random(1606598239697L);

    EquationTree randomTree = RandomEquationUtil.createRandomTree(Collections.emptyList());
    log.info("Original: {}", randomTree);
//    randomTree.setPrinter(new DefaultTreeNodePrinter<>());
//    RandomUtil.RANDOM = new Random(1606598239697L);

//    EquationTree tree = new EquationTree();
//    TreeNode<EquationNode> subtree = RandomEquationUtil
//        .createSubtree(5, 10, Collections.emptyList(), null);
//    tree.newNode(null, null, subtree);
//    tree.setPrinter(new DefaultTreeNodePrinter<>());
//
//    log.info("Partial created tree: {}", tree);
//    TreeNode<EquationNode> subtree = RandomEquationUtil
//        .createSubtree(0, 10, Collections.emptyList(), null);

//    Tree<EquationNode> newTree = new Tree<>();
//    TreeNode<EquationNode> root = newTree.newNode(null, null, new EquationNode(5D));
//    newTree.swap(root, subtree);

//    log.info("New: {}", randomTree2);

  }
}