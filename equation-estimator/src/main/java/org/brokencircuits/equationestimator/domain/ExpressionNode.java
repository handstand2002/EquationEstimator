package org.brokencircuits.equationestimator.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.INodeType;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Variable;

@Slf4j
@EqualsAndHashCode
@Builder
@Value
public class ExpressionNode{

  @NonNull
  final private INodeType baseNode;

  public double eval() {
    return baseNode.eval();
  }

  public String equationReadable(boolean replaceVariablesWithValues) {
    return equationReadable(baseNode, replaceVariablesWithValues);
  }

  public static String equationReadable(Operator operatorNode, boolean variableShowValue) {

    INodeType leftChild = operatorNode.getLeftChild().baseNode;
    INodeType rightChild = operatorNode.getRightChild().baseNode;

    String readableEquation = "(";
    readableEquation += equationReadable(leftChild, variableShowValue);
    readableEquation += " " + operatorNode.getOpChar() + " ";
    readableEquation += equationReadable(rightChild, variableShowValue);
    readableEquation += ")";

    return readableEquation;
  }

  public static String equationReadable(INodeType anonymousNode, boolean variableShowValue) {
    if (anonymousNode.getClass() == Operator.class) {
      return equationReadable((Operator) anonymousNode, variableShowValue);
    } else if (anonymousNode.getClass() == Constant.class) {
      return equationReadable((Constant) anonymousNode);
    } else if (anonymousNode.getClass() == Variable.class) {
      return equationReadable((Variable) anonymousNode, variableShowValue);
    } else {
      log.error("Node Type is invalid: {}", anonymousNode);
      return "";
    }
  }

  public static String equationReadable(Constant constantNode) {
    return String.valueOf(constantNode.eval());
  }

  public static String equationReadable(Variable variableNode, boolean useValue) {
    if (useValue) {
      return "V[" + variableNode.eval() + "]";
    } else {
      return "VID[" + variableNode.getId() + "]";
    }
  }
}
