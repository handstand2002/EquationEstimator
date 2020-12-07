package org.brokencircuits.equationestimator2.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "evolve.equation")
public class EquationEvolutionaryParamConfig {

  private int numGenerations = 100;
  private int numIndividuals = 100;
  private int numElites = 0;
  private int numNewIndividualsPerGeneration = 0;
  private ParallelPopulationsConfig parallel = new ParallelPopulationsConfig();

  @Data
  public static class ParallelPopulationsConfig {

    private int numPopulations = 1;
    private long numSwapPerPop = 0;
    private float chanceForSwap = 0;
  }

  public String toString() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "[" + getClass() + "] error in toString(): " + e.getMessage();
    }
  }
}
