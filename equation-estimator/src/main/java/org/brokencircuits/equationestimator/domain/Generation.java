package org.brokencircuits.equationestimator.domain;

import java.util.List;

public class Generation {

  private List<Equation> equationList;

  public void addEquation(Equation eq) {
    equationList.add(eq);
  }
}
