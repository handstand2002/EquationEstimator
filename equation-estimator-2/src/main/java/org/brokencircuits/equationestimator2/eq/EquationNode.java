package org.brokencircuits.equationestimator2.eq;

import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeNodeDataType;

@Getter
public class EquationNode implements TreeNodeDataType<EquationNode> {

  @Setter
  private TreeNode<EquationNode> container;
  private final Double constantValue;
  private final EquationNodeType nodeType;
  private final EquationOperator operator;
  private final EquationVariableReference variableReference;

  public EquationNode(double constantValue) {
    this.constantValue = constantValue;
    this.nodeType = EquationNodeType.CONSTANT;
    this.operator = null;
    this.variableReference = null;
  }

  public EquationNode(EquationVariableReference variableReference) {
    this.constantValue = null;
    this.nodeType = EquationNodeType.VARIABLE;
    this.operator = null;
    this.variableReference = variableReference;
  }

  public EquationNode(EquationOperator operator) {
    this.constantValue = null;
    this.nodeType = EquationNodeType.OPERATOR;
    this.operator = operator;
    this.variableReference = null;
  }

  public Double getVariableValue() {
    return Optional.ofNullable(variableReference).map(EquationVariableReference::getCurrentValue)
        .orElse(null);
  }

  public String toString() {
    if (nodeType == EquationNodeType.CONSTANT) {
      return String.valueOf(constantValue);
    } else if (nodeType == EquationNodeType.OPERATOR) {
      Objects.requireNonNull(operator);
      switch (operator) {
        case PLUS:
          return "+";
        case MULTIPLY:
          return "*";
        default:
          throw new IllegalArgumentException("Unknown Operator type: " + operator);
      }
    } else if (nodeType == EquationNodeType.VARIABLE) {
      Objects.requireNonNull(variableReference);
      return variableReference.toString();
    }
    throw new IllegalArgumentException("Unknown NodeType " + nodeType);
  }

  public double evalOperator(double arg1, double arg2) {
    Objects.requireNonNull(operator);
    return operator.getEvalMethod().apply(arg1, arg2);
  }

  public double eval() {
    switch (nodeType) {
      case CONSTANT:
        Objects.requireNonNull(constantValue);
        return constantValue;
      case OPERATOR:
        return container
            .applyConsumer((current, left, right) -> current.getOperator().getEvalMethod()
                .apply(left.eval(), right.eval()));
      case VARIABLE:
        Objects.requireNonNull(variableReference);
        return variableReference.getCurrentValue();
      default:
        throw new IllegalStateException("Unknown node type: " + nodeType);
    }
  }

}
