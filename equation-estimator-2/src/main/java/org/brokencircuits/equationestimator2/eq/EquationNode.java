package org.brokencircuits.equationestimator2.eq;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;
import org.brokencircuits.equationestimator2.domain.TreeNode;
import org.brokencircuits.equationestimator2.domain.TreeNodeDataType;
import org.brokencircuits.equationestimator2.util.MethodCallerTracker;

@Getter
public class EquationNode implements TreeNodeDataType<EquationNode, EquationTree> {

  @Setter
  private TreeNode<EquationNode, EquationTree> container;
  private final Double constantValue;
  private final EquationNodeType nodeType;
  private final EquationOperator operator;
  private final EquationVariableReference variableReference;
  private static final AtomicLong lastId = new AtomicLong(0);
  private final long id;
  private static final boolean PRINT_IDS = false;

  public EquationNode(double constantValue) {
    this.constantValue = constantValue;
    this.nodeType = EquationNodeType.CONSTANT;
    this.operator = null;
    this.variableReference = null;
    id = lastId.incrementAndGet();
  }

  public EquationNode(EquationVariableReference variableReference) {
    this.constantValue = null;
    this.nodeType = EquationNodeType.VARIABLE;
    this.operator = null;
    this.variableReference = variableReference;
    id = lastId.incrementAndGet();
  }

  public EquationNode(EquationOperator operator) {
    this.constantValue = null;
    this.nodeType = EquationNodeType.OPERATOR;
    this.operator = operator;
    this.variableReference = null;
    id = lastId.incrementAndGet();
  }

  public Double getVariableValue() {
    return Optional.ofNullable(variableReference).map(EquationVariableReference::getCurrentValue)
        .orElse(null);
  }

  public String toString() {
    return (PRINT_IDS ? "[" + id + "] " : "") + nodeType.getStringValue().apply(this);
  }

  public double eval() {
    MethodCallerTracker.onMethodCall("EquationNode#eval");

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

  @Override
  public EquationNode clone() {
    MethodCallerTracker.onMethodCall("EquationNode#clone");
    switch (nodeType) {
      case OPERATOR:
        return new EquationNode(operator);
      case VARIABLE:
        return new EquationNode(variableReference);
      case CONSTANT:
        return new EquationNode(constantValue);
      default:
        throw new IllegalStateException("Unknown node type " + nodeType);
    }
  }
}
