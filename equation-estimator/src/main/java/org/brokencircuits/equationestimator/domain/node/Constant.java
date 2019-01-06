package org.brokencircuits.equationestimator.domain.node;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Constant implements INodeType {

  private final double value;

  @Override
  public double eval() {
    return value;
  }
}
