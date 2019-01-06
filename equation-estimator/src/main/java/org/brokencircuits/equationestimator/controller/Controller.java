package org.brokencircuits.equationestimator.controller;


import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.ExpressionNode;
import org.brokencircuits.equationestimator.domain.node.Constant;
import org.brokencircuits.equationestimator.domain.node.Operator;
import org.brokencircuits.equationestimator.domain.node.Operator.OpChar;
import org.brokencircuits.equationestimator.domain.node.Variable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Controller implements Runnable {

  @Override
  public void run() {
    log.info("Running something");

    Variable v = new Variable();
    v.setValue(5.2);
    Constant c = new Constant(4);

    ExpressionNode node1 = ExpressionNode.builder().baseNode(v).build();
    ExpressionNode node2 = ExpressionNode.builder().baseNode(c).build();
    Operator op = new Operator(OpChar.MULTIPLY, node1, node2);
    ExpressionNode node3 = ExpressionNode.builder().baseNode(op).build();

    log.info("Equation: {}", node3.equationReadable(true));
  }

}
