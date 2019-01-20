package org.brokencircuits.equationestimator.domain.node;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class Constant implements IDataTerminal {

  private final double value;

  @Override
  public double eval() {
    return value;
  }

  @Override
  public IDataNode clone() {
    return new Constant(this.value);
  }
}
