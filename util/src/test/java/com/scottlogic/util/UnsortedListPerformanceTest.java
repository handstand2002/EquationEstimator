package com.scottlogic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Ignore;
import org.junit.Test;

//Test the speed of the UnsortedList against the performance of java.util.ArrayList..
public class UnsortedListPerformanceTest {

  //Tests adding many elements to the list then removing them all from the start..
  @Test
  @Ignore
  public void addThenRemoveMillion() {

    int[] elemsArr = {1000, 10000, 100000, 1000000};
    for (int j = 0; j < elemsArr.length; j++) {
      int elems = elemsArr[j];

      //ArrayList..
      long alStart = System.currentTimeMillis();

      List<Integer> al = new ArrayList<Integer>();
      for (int i = 0; i < elems; i++) {
        al.add(i);
      }
      for (int i = 0; i < elems; i++) {
        al.remove(0);
      }
      long alEnd = System.currentTimeMillis();
      System.out.println("ArrayList took:" + (alEnd - alStart) + "ms to add then remove: " + elems);

      //UnsortedList..
      long ulStart = System.currentTimeMillis();

      List<Integer> ul = new UnsortedList<Integer>();
      for (int i = 0; i < elems; i++) {
        ul.add(i);
      }
      for (int i = elems - 1; i >= 0; i--) {
        ul.remove(i);
      }
      long ulEnd = System.currentTimeMillis();
      System.out
          .println("UnsortedList took:" + (ulEnd - ulStart) + "ms to add then remove: " + elems);

    }
  }

  //Tests running a million iterations of add/remove/contains calls based on Random data..
  @SuppressWarnings("unchecked")
  @Test
  @Ignore
  public void randomTest() {

    for (List<Integer> list : Arrays
        .asList(new ArrayList<Integer>(), new UnsortedList<Integer>())) {

      //add 10,000 elements..
      for (int i = 0; i < 1000; i++) {
        list.add(i);
      }

      for (int ops : Arrays.asList(1000, 10000, 100000)) {

        long startTime = System.currentTimeMillis();

        Random rand = new Random(0); //use same seed to make it fair..
        for (int i = 0; i < ops; i++) {
          double r = rand.nextDouble();
          if (r < 1.0 / 3.0) { //add an element at some position..
            if (!list.isEmpty()) {
              int indexToAdd = rand.nextInt(list.size());
              int valueToAdd = rand.nextInt(1000000);
              list.add(indexToAdd, valueToAdd);
            }
          } else if (r < 2.0 / 3.0) { //remove an element if possible..
            if (!list.isEmpty()) {
              boolean byIndex = rand.nextBoolean();
              if (byIndex) {
                int indexToRemove = rand.nextInt(list.size());
                list.remove(indexToRemove);
              } else { //by object..
                int valueToRemove = rand.nextInt(1000000);
                list.remove(new Integer(valueToRemove));
              }
            }
          } else { //run the contains method..
            int valueToFind = rand.nextInt(1000000);
            list.contains(valueToFind);
          }
        }

        long endTime = System.currentTimeMillis();

        if (list instanceof ArrayList) {
          System.out.println("ArrayList took: " + (endTime - startTime) + "ms to complete: " + ops
              + " operations.");
        } else {
          System.out.println(
              "UnsortedList took: " + (endTime - startTime) + "ms to complete: " + ops
                  + " operations.");
        }
      }
    }
  }
}
