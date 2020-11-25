package org.brokencircuits.equationestimator2.eq;

import lombok.Getter;

@Getter
public enum EquationNodeType {
  CONSTANT((arg1, arg2, node) -> node.getConstantValue()),
  VARIABLE((arg1, arg2, node) -> node.getVariableValue()),
  OPERATOR((arg1, arg2, node) -> node.evalOperator(arg1, arg2));

  private final EvaluationMethod method;

  EquationNodeType(EvaluationMethod method) {
    this.method = method;
  }

  private interface EvaluationMethod {

    double eval(Double arg1, Double arg2, EquationNode nodeState);
  }
}
