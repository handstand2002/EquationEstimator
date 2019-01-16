package org.brokencircuits.equationestimator.dataset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.brokencircuits.equationestimator.domain.node.Variable;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class DatasetTest {

  @Before
  public void setUp() {
    Dataset dataset = Dataset.getInstance();
    dataset.clearSet();
  }

  @Test
  @SneakyThrows
  public void add() {
    Variable expected = new Variable();
    expected.setValue(Optional.of(3D));

    Dataset dataset = Dataset.getInstance();

    // when
    int id = dataset.addVariable(expected);

    // then
    Variable actual = dataset.getById(id).get();
    assertEquals(expected, actual);
  }

  @Test
  @SneakyThrows
  public void addMuliple() {
    Variable expected = new Variable();
    expected.setValue(Optional.of(3D));

    Variable expected2 = new Variable();
    expected2.setValue(Optional.of(4D));

    Dataset dataset = Dataset.getInstance();

    // when
    int id = dataset.addVariable(expected);
    int id2 = dataset.addVariable(expected2);

    // then
    assertEquals(expected, dataset.getById(id).get());
    assertEquals(expected2, dataset.getById(id2).get());
  }

  @Test
  @SneakyThrows
  public void addMulipleGetRandom() {
    Variable expected = new Variable();
    expected.setValue(Optional.of(3D));

    Variable expected2 = new Variable();
    expected2.setValue(Optional.of(4D));

    Variable expected3 = new Variable();
    expected3.setValue(Optional.of(5D));

    List<Variable> createdList = Lists.newArrayList(
        expected, expected2, expected3
    );

    Dataset dataset = Dataset.getInstance();

    // when
    dataset.addVariable(expected);
    dataset.addVariable(expected2);
    dataset.addVariable(expected3);

    // then
    for (int i = 0; i < 10; i++) {
      assertTrue(createdList.contains(dataset.getRandom().get()));
    }

  }

  @Test
  @SneakyThrows
  public void addSets() {
    Variable expected = new Variable();
    expected.setValue(Optional.of(3D));

    Variable expected2 = new Variable();
    expected2.setValue(Optional.of(4D));

    Variable expected3 = new Variable();
    expected3.setValue(Optional.of(5D));

    Variable expected4 = new Variable();
    expected.setValue(Optional.of(6D));

    Variable expected5 = new Variable();
    expected2.setValue(Optional.of(7D));

    Variable expected6 = new Variable();
    expected3.setValue(Optional.of(8D));

    Dataset dataset = Dataset.getInstance();

    // when
    int setId1 = dataset.getCurrentSetId();
    int id1 = dataset.addVariable(expected);
    int id2 = dataset.addVariable(expected2);
    int id3 = dataset.addVariable(expected3);
    dataset.setCurrentSetId(2);
    int setId2 = dataset.getCurrentSetId();
    int id4 = dataset.addVariable(expected4);
    int id5 = dataset.addVariable(expected5);
    int id6 = dataset.addVariable(expected6);

    // then
    dataset.setCurrentSetId(setId1);
    assertEquals(expected, dataset.getById(id1).get());
    assertEquals(expected2, dataset.getById(id2).get());
    assertEquals(expected3, dataset.getById(id3).get());
    dataset.setCurrentSetId(setId2);
    assertEquals(expected4, dataset.getById(id4).get());
    assertEquals(expected5, dataset.getById(id5).get());
    assertEquals(expected6, dataset.getById(id6).get());

  }
}