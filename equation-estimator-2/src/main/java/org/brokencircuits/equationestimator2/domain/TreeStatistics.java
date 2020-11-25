package org.brokencircuits.equationestimator2.domain;

import lombok.Getter;

@Getter
public class TreeStatistics {

  private long nodeCount = 0;

  void incrementNodeCount() {
    nodeCount++;
  }
}
