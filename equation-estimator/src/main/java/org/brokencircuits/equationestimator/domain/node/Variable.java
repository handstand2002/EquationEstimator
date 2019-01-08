package org.brokencircuits.equationestimator.domain.node;

import lombok.Data;
import org.brokencircuits.equationestimator.domain.TreeNode;

@Data
public class Variable implements IDataTerminal {

  static private int lastId;

  final private int id;
  private double value;
  private TreeNode parent;

  public Variable() {
    id = ++lastId;
  }

  @Override
  public IDataNode clone() {
    // TODO: Finish this
    return this;
  }

  @Override
  public double eval() {
    return value;
  }


}
