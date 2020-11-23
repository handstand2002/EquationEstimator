package org.brokencircuits.equationestimator2;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.brokencircuits.evolve.AttributeMutator;
import org.brokencircuits.evolve.AttributeSwapper;
import org.brokencircuits.evolve.AttributeType;
import org.brokencircuits.evolve.Controller;
import org.brokencircuits.evolve.EvolutionaryParameters;
import org.brokencircuits.evolve.FitnessEvaluator;
import org.brokencircuits.evolve.Generation;
import org.brokencircuits.evolve.Individual;
import org.brokencircuits.evolve.IndividualGenerator;
import org.brokencircuits.evolve.IndividualSelector;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

  @Bean
  public CommandLineRunner test() {
    return args -> {

      Random random = new Random(Instant.now().toEpochMilli());
      long desiredLong = random.nextLong();
      byte[] desired = longToBytes(desiredLong);
      log.info("Desired: {}: {}", Arrays.toString(desired), desiredLong);
      FitnessEvaluator<TestType> evaluator = individual -> {
        byte[] values = individual.getAttributes().values;
        double score = 0;
        for (int i = 0; i < desired.length; i++) {
          score += Math.abs((int) desired[i] - (int) values[i]) * (desired.length + 1 - i);
        }
        return score;
      };

      AttributeMutator<TestType> mutator = individual -> {
        byte[] values = individual.getAttributes().values;
        int updateIndex = random.nextInt(values.length);
        values[updateIndex] = (byte) (random.nextInt(255) - 127);
      };

      final long selectionGranularity = 1000;
      final long maxSquared = selectionGranularity * selectionGranularity;
      IndividualSelector<TestType> selector = scoredIndividuals -> {

        long selection = (random.nextLong() % selectionGranularity) + 1;
        long selectionSquared = selection * selection;
        double percentage = selectionSquared / (double) maxSquared;

        int useIndex = Math.min(
            Math.toIntExact(Math.round(Math.floor(percentage * scoredIndividuals.size()))),
            scoredIndividuals.size() - 1);
        return scoredIndividuals.get(useIndex);
      };

      AttributeSwapper<TestType> swapper = (first, second) -> {
        int length = first.getAttributes().values.length;
        int firstIndex = random.nextInt(length);
        int secondIndex = random.nextInt(length);
        byte firstValue = first.getAttributes().values[firstIndex];
        first.getAttributes().values[firstIndex] = second.getAttributes().values[secondIndex];
        second.getAttributes().values[secondIndex] = firstValue;
      };

      IndividualGenerator<TestType> generator = () -> {
        TestType attribs = new TestType();
        for (int i = 0; i < attribs.values.length; i++) {
          attribs.values[i] = (byte) (random.nextInt(255) - 127);
        }
        return new Individual<>(attribs);
      };

      EvolutionaryParameters<TestType> params = EvolutionaryParameters.<TestType>builder()
          .evaluator(evaluator)
          .mutator(mutator)
          .selector(selector)
          .swapper(swapper)
          .generator(generator)
          .completionPredicate(p -> p.get(0).getFitness() == 0D)
          .build();
      Controller<TestType> controller = new Controller<>(params);
      Generation<TestType> lastGeneration = controller.run(10000, 100, 5);
      log.info("Best solution: {}", lastGeneration.getIndividuals().get(0));
    };
  }

  @ToString
  private static class TestType implements AttributeType {

    public final byte[] values = new byte[8];

    public TestType clone() {
      TestType newObj = new TestType();
      System.arraycopy(values, 0, newObj.values, 0, values.length);
      return newObj;
    }
  }

  public byte[] longToBytes(long x) {
    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
    buffer.putLong(x);
    return buffer.array();
  }
}
