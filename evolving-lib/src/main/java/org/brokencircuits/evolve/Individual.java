package org.brokencircuits.evolve;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class Individual<T extends AttributeType<T>> implements Cloneable {

  @Getter
  private final T attributes;
  @Setter
  @Getter
  private Double fitness = null;

  public Individual<T> clone() {
    return new Individual<>(attributes.clone());
  }
}
