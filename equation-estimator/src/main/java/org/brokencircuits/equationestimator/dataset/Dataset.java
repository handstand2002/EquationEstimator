package org.brokencircuits.equationestimator.dataset;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.equationestimator.domain.node.Variable;
import org.brokencircuits.equationestimator.util.Chance;

@Slf4j
public class Dataset {

  private static Dataset instance = null;

  private Map<Integer, Map<Integer, Variable>> variableMapSets = Maps.newHashMap();
  @Getter
  @Setter
  private int currentSetId = 0;
  private int lastIntegerId = 0;

  private Dataset() {
  }

  public static Dataset getInstance() {
    if (instance == null) {
      instance = new Dataset();
    }
    return instance;
  }

  private Map<Integer, Variable> getSet(int id) {
    if (variableMapSets.containsKey(id)) {
      return variableMapSets.get(id);
    } else {
      Map<Integer, Variable> newSet = Maps.newHashMap();
      variableMapSets.put(id, newSet);
      return newSet;
    }
  }

  public int addVariable(Variable add) {
    int thisId = ++lastIntegerId;

    getSet(currentSetId).put(thisId, add);
    return thisId;
  }

  public Optional<Variable> getById(int id) {
    Map<Integer, Variable> currentVariableMap = getSet(currentSetId);
    if (currentVariableMap.containsKey(id)) {
      return Optional.of(currentVariableMap.get(id));
    }
    return Optional.empty();
  }

  public Optional<Variable> getRandom() {
    Map<Integer, Variable> currentVariableMap = getSet(currentSetId);
    if (!currentVariableMap.isEmpty()) {
      Integer[] keyArray = currentVariableMap.keySet().toArray(new Integer[0]);
      int chooseId = keyArray[Chance.RAND.nextInt(keyArray.length)];
      return Optional.of(currentVariableMap.get(chooseId));
    } else {
      return Optional.empty();
    }
  }

  public void clearSet() {
    Map<Integer, Variable> currentVariableMap = getSet(currentSetId);
    currentVariableMap.clear();
  }
}
