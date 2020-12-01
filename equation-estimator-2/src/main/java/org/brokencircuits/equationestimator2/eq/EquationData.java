package org.brokencircuits.equationestimator2.eq;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EquationData {

  private final List<EquationDataSet> dataSets;
  private final List<EquationVariableReference> variables;

}
