package org.brokencircuits.equationestimator.domain.node;

import java.util.Optional;
import lombok.Data;

@Data
public class Variable implements IDataTerminal {

  static private int lastId;

  final private int id;
  private Optional<Double> value = Optional.empty();
  private String name;

  public Variable() {
    id = ++lastId;
  }

  @Override
  public IDataNode clone() {
    return this;
  }

  @Override
  public double eval() {
    return value.get();
  }


}
