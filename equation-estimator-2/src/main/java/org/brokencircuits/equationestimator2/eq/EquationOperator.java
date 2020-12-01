package org.brokencircuits.equationestimator2.eq;

import lombok.Getter;

@Getter
public enum EquationOperator {
  ADD(Double::sum),
  MULTIPLY((arg1, arg2) -> arg1 * arg2);

  private final OperatorEval evalMethod;

  EquationOperator(OperatorEval evalMethod) {
    this.evalMethod = evalMethod;
  }
}
