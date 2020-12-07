package org.brokencircuits.equationestimator2.util;

import java.time.Instant;
import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomUtil {

  //  public static Random RANDOM = new Random(1606598239697L);
  public static Random RANDOM = new Random(Instant.now().toEpochMilli());
}
