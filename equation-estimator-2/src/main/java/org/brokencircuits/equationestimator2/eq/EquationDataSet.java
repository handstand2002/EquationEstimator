package org.brokencircuits.equationestimator2.eq;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Value;

@Value
@Getter
public class EquationDataSet {

  Map<EquationVariableReference, Double> values = new HashMap<>();
  Double expectedOutput;

}
