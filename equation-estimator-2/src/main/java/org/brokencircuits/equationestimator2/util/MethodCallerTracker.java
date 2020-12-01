package org.brokencircuits.equationestimator2.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodCallerTracker {

  private static final Map<String, AtomicLong> callTimes = new HashMap<>();

  public static void onMethodCall(String signature) {
//    log.info("Calling {}", signature);
    callTimes.computeIfAbsent(signature, k -> new AtomicLong(0)).incrementAndGet();
  }

  public static void log() {
    callTimes.forEach((k, v) -> log.info("Call: {}: {}", k, v.get()));
  }
}
