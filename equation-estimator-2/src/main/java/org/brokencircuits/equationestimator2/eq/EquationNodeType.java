package org.brokencircuits.equationestimator2.eq;

import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EquationNodeType {
  CONSTANT(node -> String.valueOf(node.getConstantValue())),
  VARIABLE(node -> node.getVariableReference().toString()),
  OPERATOR(node -> node.getOperator().getToStringValue());

  private final Function<EquationNode, String> stringValue;
}
