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
  @Setter
  private String solutionName = "";

  private Map<Integer, Map<String, Double>> valueSets = Maps.newHashMap();
  private Map<String, Variable> variables = Maps.newHashMap();

  @Getter
  private int currentSetId = 0;

  private Dataset() {
  }

  public static Dataset getInstance() {
    if (instance == null) {
      instance = new Dataset();
    }
    return instance;
  }

  public void setCurrentSetId(int id) {
    this.currentSetId = id;
    assignValuesToVariables();
  }

  private Map<String, Double> getSet(int id) {
    if (valueSets.containsKey(id)) {
      return valueSets.get(id);
    } else {
      Map<String, Double> newSet = Maps.newHashMap();
      valueSets.put(id, newSet);
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
        for (String headerField : headerFields) {
          if (headerField.equals(solutionName)) {
            continue;
          }
          Variable var = new Variable();
          var.setName(headerField);
          variables.put(var.getName(), var);
        }
        isFirstLine = false;
      } else {
        this.setCurrentSetId(dataLineNumber);

        for (int i = 0; i < lineFields.size(); i++) {
          String varName = headerFields.get(i);
          Double varValue = 0D;
          try {
            varValue = Double.parseDouble(lineFields.get(i));
          } catch (NumberFormatException expected) {

          }

          this.addValue(varName, varValue);
        }

        dataLineNumber++;
      }
    }
    this.setCurrentSetId(1);
    this.assignValuesToVariables();
  }

  public Double getCurrentSetSolution() {
    Map<String, Double> currentSet = getSet(currentSetId);
    if (!currentSet.containsKey(solutionName)) {
      log.error("current dataset {} does not contain solution {}", currentSetId, solutionName);
      return null;
    }
    return currentSet.get(solutionName);
  }

  private void assignValuesToVariables() {
    Map<String, Double> currentSet = getSet(currentSetId);
    for (String name : variables.keySet()) {

      Variable var = variables.get(name);
      if (currentSet.containsKey(name)) {
        var.setValue(Optional.of(currentSet.get(name)));
      } else {
        var.setValue(Optional.empty());
      }

    }
  }

  public Optional<Variable> getVariableByName(String varName) {
    if (variables.containsKey(varName)) {
      return Optional.of(variables.get(varName));
    }
    return Optional.empty();
  }

  private void addValue(String varName, Double varValue) {
    getSet(currentSetId).put(varName, varValue);
  }

  public String getSetContents(int id) {
    StringBuilder sb = new StringBuilder();
    sb.append("Set ID ").append(id).append("\n");
    Map<String, Double> set = this.getSet(id);
    for (String key : set.keySet()) {
      String solutionText = "";
      if (key.equals(solutionName)) {
        solutionText = " - Solution Value";
      }
      sb.append("\t").append(key).append(": ").append(set.get(key)).append(solutionText)
          .append("\n");
    }
    return sb.toString();
  }

  public String getAllSetContents() {
    StringBuilder sb = new StringBuilder();
    sb.append("* Contents of all datasets *******************************\n");
    sb.append("Variables:\n");
    for (Variable var : variables.values()) {
      sb.append("\t").append(var).append("\n");
    }
    sb.append("***********************\n\n");

    for (Integer setId : valueSets.keySet()) {
      sb.append(getSetContents(setId)).append("\n");
    }
    sb.append("**********************************************************\n");
    return sb.toString();
  }

  public Optional<Variable> getRandom() {
    List<Variable> varSet = Lists.newArrayList(variables.values());

    if (!variables.isEmpty()) {
      return Optional.of(varSet.get(Chance.RAND.nextInt(varSet.size())));
    }
    return Optional.empty();
  }

  public List<Integer> getIdList() {
    return Lists.newArrayList(valueSets.keySet());
  }
}
