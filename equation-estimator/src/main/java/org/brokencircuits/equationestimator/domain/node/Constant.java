package org.brokencircuits.equationestimator.domain.node;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class Constant implements IDataTerminal {

  private final double value;

  @Override
  public double eval() {
    return value;
  }

  @Override
  public IDataNode clone() {
    // TODO: Finish this
    return null;
  }
}
