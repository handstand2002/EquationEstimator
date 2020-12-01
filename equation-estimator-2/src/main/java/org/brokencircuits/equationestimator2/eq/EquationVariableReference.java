package org.brokencircuits.equationestimator2.eq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class EquationVariableReference {

  private final String name;
  @Setter
  private Double currentValue;

  public String toString() {
    return name;
  }
}
