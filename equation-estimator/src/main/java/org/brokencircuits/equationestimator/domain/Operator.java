package org.brokencircuits.equationestimator.domain;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@ToString
@Slf4j
public class Operator implements INodeType {

  @NonNull
  final Character character;
  @NonNull
  final ExpressionNode leftChild;
  @NonNull
  final ExpressionNode rightChild;

  @Override
  public double eval() {
    double leftValue = leftChild.eval();
    double rightValue = rightChild.eval();
    switch (character) {
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

  enum Character {
    PLUS, MINUS, MULTIPLY, DIVIDE
  }

}
