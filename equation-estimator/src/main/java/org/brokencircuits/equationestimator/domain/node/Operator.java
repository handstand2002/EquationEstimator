package org.brokencircuits.equationestimator.domain.node;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.util.Chance;

@ToString
@Slf4j
public class Operator implements IDataNode {

  final public static OpChar PLUS = OpChar.PLUS;
  final public static OpChar MINUS = OpChar.MINUS;
  final public static OpChar MULTIPLY = OpChar.MULTIPLY;
  final public static OpChar DIVIDE = OpChar.DIVIDE;

  private static int lastId = 0;

  @NonNull
  final OpChar opChar;
  final int id;

  @java.beans.ConstructorProperties({"opChar"})
  public Operator(OpChar opChar) {
    this.id = ++lastId;
    this.opChar = opChar;
  }

  @Override
  public IDataNode clone() {
    // TODO: finish this
    return new Operator(opChar);
  }

  public double operation(double x, double y) {
    switch (opChar) {
      case PLUS:
        return x + y;
      case MINUS:
        return x - y;
      case DIVIDE:
        if (y != 0) {
          return x / y;
        } else {
          return x;
        }
      case MULTIPLY:
        return x * y;
      default:
        log.error("Op Char invalid in node {}", this);
        return 0;
    }
  }

  public static OpChar getOpChar(int i) {
    switch (Math.floorMod(i, 4)) {
      case 0:
        return OpChar.PLUS;
      case 1:
        return OpChar.MINUS;
      case 2:
        return OpChar.MULTIPLY;
      case 3:
        return OpChar.DIVIDE;
      default:
        log.error("Something went horribly wrong retrieving opChar from int");
        return null;
    }
  }

  public static OpChar randOpChar() {
    return getOpChar(Math.floorMod(Chance.RAND.nextInt(), 4));
  }

  public String getChar() {
    switch (opChar) {
      case PLUS:
        return "+";
      case MINUS:
        return "-";
      case DIVIDE:
        return "/";
      case MULTIPLY:
        return "*";
      default:
        log.error("Op Char invalid in node {}", this);
        return "";
    }
  }

  private enum OpChar {
    PLUS, MINUS, MULTIPLY, DIVIDE
  }

}
