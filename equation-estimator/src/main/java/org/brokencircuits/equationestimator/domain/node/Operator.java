package org.brokencircuits.equationestimator.domain.node;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.ExpressionNode;

@RequiredArgsConstructor
@ToString
@Slf4j
@Value
public class Operator implements INodeType {

  @NonNull
  final OpChar opChar;
  @NonNull
  final ExpressionNode leftChild;
  @NonNull
  final ExpressionNode rightChild;

  public String getOpChar() {
    switch (opChar) {
      case PLUS: return "+";
      case MINUS: return "-";
      case DIVIDE: return "/";
      case MULTIPLY: return "*";
      default:
        log.error("Op Char invalid in node {}", this);
        return "";
    }
  }

  @Override
  public double eval() {
    double leftValue = leftChild.eval();
    double rightValue = rightChild.eval();
    switch (opChar) {
      case PLUS:
        return leftValue + rightValue;
      case MINUS:
        return leftValue - rightValue;
      case MULTIPLY:
        return leftValue * rightValue;
      case DIVIDE:
        if (rightValue != 0) {
          return leftValue / rightValue;
        } else {
          return leftValue;
          // TODO: Try different methods of averting /0 errors
        }
      default:
        log.error("Operator node invalid: {}", this);
        return 0;
    }
  }

  public enum OpChar {
    PLUS, MINUS, MULTIPLY, DIVIDE
  }

}
