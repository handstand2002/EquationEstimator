package org.brokencircuits.equationestimator.domain;

import java.util.List;
import org.brokencircuits.equationestimator.controller.Controller;

public class Generation {

  private List<Equation> equationList;

  private void addEquation(Equation eq) {
    equationList.add(eq);
  }

  public void generateRandomPop() {
    for (int i = 0; i < Controller.POP_SIZE; i++) {
      addEquation(Equation.generateRandom(Controller.INIT_OP_NODE_COUNT));
    }
  }
}
