package org.brokencircuits.evolve;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class Individual<T extends AttributeType> implements Cloneable {

  @Getter
  private final T attributes;
  @Setter
  @Getter
  private Double fitness = null;

  @SuppressWarnings("unchecked")
  public Individual<T> clone() {
    return new Individual<>((T) attributes.clone());
  }
}
