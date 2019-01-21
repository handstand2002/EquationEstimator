package org.brokencircuits.equationestimator.evolve;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.Equation;
import org.brokencircuits.equationestimator.domain.TreeNode;
import org.brokencircuits.equationestimator.util.Chance;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Evolver {

  private static Evolver instance = null;

  private Evolver() {
  }

  public static Evolver getInstance() {
    if (instance == null) {
      instance = new Evolver();
    }
    return instance;
  }

  public List<Equation> nodeExchange(Equation eq1, Equation eq2) {

    Equation childEq1 = eq1.clone();
    Equation childEq2 = eq2.clone();

    List<TreeNode> childNodeList1 = childEq1.getNodeList();
    List<TreeNode> childNodeList2 = childEq2.getNodeList();

    TreeNode randomChildNode1 = childNodeList1.get(Chance.RAND.nextInt(childNodeList1.size()));
    TreeNode randomChildNode2 = childNodeList2.get(Chance.RAND.nextInt(childNodeList2.size()));

    TreeNode.swapNodes(randomChildNode1, randomChildNode2);

    return Lists.newArrayList(childEq1, childEq2);
  }


}
