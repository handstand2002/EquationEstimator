package org.brokencircuits.equationestimator.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EqualsAndHashCode
@Builder
@Value
public class ExpressionNode {

  final private Double constant;
  final private Variable variable;
  final private Operator operator;

  public double eval() {
    if (constant != null) {
      return constant;
    } else if (variable != null) {
      return variable.eval();
    } else if (operator != null) {
      return operator.eval();
    } else {
      log.error("Node {} is invalid:\n\t\t{}", this);
      return 0;
    }
  }
}
