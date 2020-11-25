package org.brokencircuits.equationestimator2.domain;

public interface ChildAwareConsumer<T, U> {

  U apply(T nodeData, T leftChild, T rightChild);
}
