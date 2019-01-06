package org.brokencircuits.equationestimator.domain.node;

import lombok.Data;

@Data
public class Variable implements INodeType {

  static private int lastId;

  final private int id;
  private double value;

  public Variable() {
    id = ++lastId;
  }

  @Override
  public double eval() {
    return value;
  }
}
