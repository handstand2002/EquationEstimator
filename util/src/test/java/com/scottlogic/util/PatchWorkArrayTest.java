package com.scottlogic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

/**
 * Units tests for the {@code PatchWorkArray} class.  Works under the assumption that {@code
 * ArrayList} works correctly and the same as the {@code PatchWorkArray}.
 *
 * @author Mark Rhodes
 */
public class PatchWorkArrayTest {

  /**
   * Tests that adding to the end of the array works like an {@code ArrayList} when there are no
   * alterations to the list.
   */
  @Test
  public void testAddToEnd() {

    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();
    for (int i = 0; i < 10; i++) {
      al.add(i % 3);
      pwa.add(i % 3);
    }

    checkEquals(al, pwa);
  }

  /**
   * Tests that adding to the end of the array works list a {@code LinkedList}.
   */
  @Test
  public void testAddToFront() {
    LinkedList<Integer> ll = new LinkedList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();
    for (int i = 0; i < 10; i++) {
      ll.addFirst(i % 3);
      pwa.add(0, i % 3);
    }

    checkEquals(ll, pwa);
  }

  /**
   * Tests that adding to the middle of the array works list an {@code ArrayList}.
   */
  @Test
  public void testAddToMiddle() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 10; i++) {
      al.add(i);
      pwa.add(i);
    }

    //now add elements to the middle..
    al.add(5, 5);
    pwa.add(5, 5);

    //add some more, slightly left and right..
    al.add(2, 2);
    pwa.add(2, 2);
    al.add(2, 2);
    pwa.add(2, 2);

    al.add(10, 8);
    pwa.add(10, 8);
    al.add(10, 8);
    pwa.add(10, 8);

    assertEquals(al, pwa);
  }

  /**
   * Tests that the fix method work as expected.
   */
  @Test
  public void testFixWithRemovals() {

    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //fix when there's no alterations..
    for (int i = 0; i < 5; i++) {
      al.add(i);
      pwa.add(i);
    }
    assertEquals(al, pwa);
    pwa.fix();
    assertEquals(al, pwa);

    //removals at the start..
    al.clear();
    pwa.clear();
    for (int i = 0; i < 5; i++) {
      al.add(i);
      pwa.add(i);
    }
    al.remove(0);
    al.remove(0);
    pwa.remove(0);
    pwa.remove(0);
    assertEquals(al, pwa);
    pwa.fix();
    assertEquals(al, pwa);

    //removals in the middle..
    al.clear();
    pwa.clear();
    for (int i = 0; i < 5; i++) {
      al.add(i);
      pwa.add(i);
    }
    al.remove(1);
    al.remove(2);
    pwa.remove(1);
    pwa.remove(2);

    assertEquals(al, pwa);
    pwa.fix();
    assertEquals(al, pwa);

    //removals in the middle..
    al.clear();
    pwa.clear();
    for (int i = 0; i < 5; i++) {
      al.add(i);
      pwa.add(i);
    }
    al.remove(al.size() - 1);
    al.remove(al.size() - 1);
    pwa.remove(pwa.size() - 1);
    pwa.remove(pwa.size() - 1);

    assertEquals(al, pwa);
    pwa.fix();
    assertEquals(al, pwa);
  }

  //Random testing of adding/removing..
  @Test
  public void numerousOperations() {
    int tests = 1000;
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();
    ArrayList<Integer> al = new ArrayList<Integer>();
    Random rand = new Random(0);

    for (int i = 0; i < tests; i++) {
      al.add(i);
      pwa.add(i);
    }
    assertEquals(al, pwa);

    for (int i = 0; i < tests; i++) {
      double r = rand.nextDouble();
      int nextIndex = (int) Math.floor(rand.nextDouble() * al.size());
      if (r < 0.5) {
        al.add(nextIndex, i);
        pwa.add(nextIndex, i);
      } else {
        al.remove(nextIndex);
        pwa.remove(nextIndex);
      }
      checkEquals(al, pwa);
    }
  }

  //Attempts to add an element where there is a Removal, to the start and the end.
  @Test
  public void addingWhereThereIsARemoval() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 10; i++) {
      al.add(i);
      pwa.add(i);
    }
    //removal a block in the middle..
    for (int i = 0; i < 3; i++) {
      al.remove(4);
      pwa.remove(4);
    }
    checkEquals(al, pwa);

    //trigger removal from front of removal.
    al.add(3, -1);
    pwa.add(3, -1);
    checkEquals(al, pwa);

    //should trigger removal from end of removal.
    al.add(5, -2);
    pwa.add(5, -2);
    checkEquals(al, pwa);
  }

  //Tests when you remove an element when there is a removal to the left and the right..
  @Test
  public void mergingRemovals() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 10; i++) {
      al.add(i);
      pwa.add(i);
    }

    al.remove(3);
    pwa.remove(3);
    checkEquals(al, pwa);
    al.remove(3);
    pwa.remove(3);
    checkEquals(al, pwa);

    al.remove(4);
    pwa.remove(4);
    checkEquals(al, pwa);
    al.remove(4);
    pwa.remove(4);
    checkEquals(al, pwa);

    //this should cause the removals to merge into one..
    al.remove(3);
    pwa.remove(3);
    checkEquals(al, pwa);
  }

  //Tests that when you add to the end of a sublist and there is a removal on the right of it..
  @Test
  public void addingToEndOfSubListWithRemovalOnTheRight() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 10; i++) {
      al.add(i);
      pwa.add(i);
    }

    al.add(3, -1);
    pwa.add(3, -1);
    checkEquals(al, pwa);

    al.add(3, -2);
    pwa.add(3, -2);
    checkEquals(al, pwa);

    //remove the element to the right of the subList..
    al.remove(6);
    pwa.remove(6);
    al.remove(6);
    pwa.remove(6);
    checkEquals(al, pwa);

    //now add to the end of the sublist..
    al.add(5, -3);
    pwa.add(5, -3);
    checkEquals(al, pwa);
  }

  //Tests that when you add to the start of a sublist and there is a removal on the left hand side of it..
  @Test
  public void addingToStartOfSubListWithRemovalOnTheLeft() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 10; i++) {
      al.add(i);
      pwa.add(i);
    }

    al.add(4, -1);
    pwa.add(4, -1);
    checkEquals(al, pwa);

    al.add(4, -2);
    pwa.add(4, -2);
    checkEquals(al, pwa);

    //remove the element to the right of the subList..
    al.remove(3);
    pwa.remove(3);
    al.remove(2);
    pwa.remove(2);
    checkEquals(al, pwa);

    //now add to the start of the sublist..
    al.add(2, -3);
    pwa.add(2, -3);
    checkEquals(al, pwa);
  }

  /**
   * Tests whether the fix method can cope with additions throughout the list.
   */
  @Test
  public void testFixWithAdditons() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 3; i++) {
      al.add(i);
      pwa.add(i);
    }
    //add some at the front..
    for (int i = 0; i < 3; i++) {
      al.add(0, 0);
      pwa.add(0, 0);
    }
    //add some in the middle..
    for (int i = 0; i < 3; i++) {
      al.add(5, 500);
      pwa.add(5, 500);
    }
    //add at penultimate position, then remove last..
    for (int i = 0; i < 3; i++) {
      al.add(al.size() - 2, -1);
      pwa.add(pwa.size() - 2, -1);
    }
    al.remove(al.size() - 1);
    pwa.remove(pwa.size() - 1);

    checkEquals(al, pwa);
  }

  /**
   * Test removing elements from the start of the list, when there are sublists involves as well as
   * not.
   */
  @Test
  public void testRemoveFromStart() {
    ArrayList<Integer> al = new ArrayList<Integer>();
    PatchWorkArray<Integer> pwa = new PatchWorkArray<Integer>();

    //just add some elements to start off with..
    for (int i = 0; i < 3; i++) {
      al.add(i);
      pwa.add(i);
    }
    al.add(0, 100);
    pwa.add(0, 100);
    al.add(1, 1);
    pwa.add(1, 1);
    //add some more to create sub-lists..
    for (int i = 0; i < 3; i++) {
      al.add(i, i);
      pwa.add(i, i);
      al.add(i, i);
      pwa.add(i, i);
    }

    //check they're the same now..
    checkEquals(al, pwa);

    //now remove elements and check they remain the same..
    for (int i = 0, size = al.size(); i < size; i++) {
      al.remove(0);
      pwa.remove(0);
      checkEquals(al, pwa);
    }
  }

  //Throws an assertion error in the case that the lists aren't equal (also checks that the actual list
  //has it's alterations and backing list in sync..
  private <T> void checkEquals(List<T> expected, PatchWorkArray<T> actual) {
    assertEquals(expected.size(), actual.size());

    Iterator<T> exItr = expected.iterator();
    Iterator<T> actItr = actual.iterator();
    while (exItr.hasNext()) {
      T exNext = exItr.next();
      T actNext = actItr.next();
      assertEquals(exNext, actNext);
    }
    assertTrue(actual.__checkBackingListAndAlterationsInSync__());
  }
}
