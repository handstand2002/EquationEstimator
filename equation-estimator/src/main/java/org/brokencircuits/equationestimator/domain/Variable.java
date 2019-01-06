package org.brokencircuits.equationestimator.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Variable implements INodeType {


  @Override
  public double eval() {
    return 0;
  }
}
