package org.brokencircuits.equationestimator.dataset;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
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

  public void loadCsv(File inputFile) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(inputFile));

    String line;
    boolean isFirstLine = true;
    List<String> headerFields = null;
    int dataLineNumber = 1;   // start on dataset ID 1
    while ((line = reader.readLine()) != null) {
      // -1 limit requires the split function to include all fields, even if they're empty
      List<String> lineFields = Lists.newArrayList(line.split(",", -1));
      lineFields.replaceAll(String::trim);

      if (isFirstLine) {
        headerFields = lineFields;
        isFirstLine = false;
      } else {
        this.setCurrentSetId(dataLineNumber);

        for (int i = 0; i < lineFields.size(); i++) {
          Variable variable = new Variable();

          try {
            variable.setValue(Optional.of(Double.parseDouble(lineFields.get(i))));
          } catch (NumberFormatException e) {

          }
          variable.setName(headerFields.get(i));

          this.addVariable(variable);
        }

        dataLineNumber++;
      }
    }
  }

  public String setContents(int id) {
    StringBuilder sb = new StringBuilder();
    sb.append("Set ID ").append(id).append("\n");
    Map<Integer, Variable> set = this.getSet(id);
    for (Variable var : set.values()) {
      sb.append("\t").append(var.toString()).append("\n");
    }
    return sb.toString();
  }

  public String allSetContents() {
    StringBuilder sb = new StringBuilder();
    sb.append("* Contents of all datasets *******************************\n");
    for (Integer setId : variableMapSets.keySet()) {
      sb.append(setContents(setId)).append("\n");
    }
    sb.append("**********************************************************\n");
    return sb.toString();
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
      log.warn("Cannot retrieve random variable as the variable pool is empty");
      return Optional.empty();
    }
  }

  public void clearSet() {
    Map<Integer, Variable> currentVariableMap = getSet(currentSetId);
    currentVariableMap.clear();
  }
}
