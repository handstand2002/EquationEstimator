package org.brokencircuits.equationestimator2.eq;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EquationOperator {
  ADD(Double::sum, 2, "+"),
  MULTIPLY((arg1, arg2) -> arg1 * arg2, 1, "*"),
  POW(Math::pow, 0, "^");

  private final OperatorEval evalMethod;
  private final int priority;
  private final String toStringValue;
}
