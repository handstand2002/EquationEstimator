package org.brokencircuits.equationestimator.evolve;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.dataset.Dataset;
import org.brokencircuits.equationestimator.domain.Equation;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class EvolverTest {

  Evolver evolver = Evolver.getInstance();

  @Before
  public void setUp() {
    Dataset dataset = Dataset.getInstance();
//    dataset.addVariable(new Variable());
  }

  @Test
  public void nodeExchange() {
    Equation eq1 = Equation.generateRandom(3);
    Equation eq2 = Equation.generateRandom(3);
    List<Equation> children = evolver.nodeExchange(eq1, eq2);


  }
}