package com.scottlogic.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import org.junit.Test;

/**
 * Units tests for the AlterationList of the PatchWorkArray class.
 *
 * @author Mark Rhodes
 */
public class AlterationListTest {

  //Calculates the performance hit of the given hit from first principles..
  private static int getRealPerformanceHit(AlterationList alts) {
    Iterator<Alteration> itr = alts.iterator();
    int totalPerformanceHit = 0;
    while (itr.hasNext()) {
      totalPerformanceHit += itr.next().getPerformanceHit();
    }
    return totalPerformanceHit;
  }

  @Test
  public void removesToLeft() {
    AlterationList alts = new AlterationList();
    alts.add(new Removal(1));
    alts.add(new Removal(0));

    ArrayLocation loc = alts.getLocationOf(5);
    assertEquals(new ArrayLocation(7), loc);
  }

  @Test
  public void removesToRight() {
    AlterationList alts = new AlterationList();
    alts.add(new Removal(0));
    alts.add(new Removal(1));

    ArrayLocation loc = alts.getLocationOf(5);
    assertTrue(loc.equals(new ArrayLocation(7)));
  }

  @Test
  public void additionsToLeft() {
    AlterationList alts = new AlterationList();
    alts.add(new Addition(1));
    alts.add(new Addition(0));

    ArrayLocation loc = alts.getLocationOf(5);
    assertTrue(loc.equals(new ArrayLocation(3)));
  }

  @Test
  public void additionsToRight() {
    AlterationList alts = new AlterationList();
    alts.add(new Addition(0));
    alts.add(new Addition(1));

    ArrayLocation loc = alts.getLocationOf(5);
    assertTrue(loc.equals(new ArrayLocation(3)));
  }

  @Test
  public void noAlterations() {
    AlterationList alts = new AlterationList();
    ArrayLocation loc = alts.getLocationOf(5);
    assertTrue(loc.equals(new ArrayLocation(5)));
  }

  @Test
  public void sameIndexRemove() {
    AlterationList alts = new AlterationList();
    alts.add(new Removal(0));
    ArrayLocation loc = alts.getLocationOf(0);
    assertTrue(loc.equals(new ArrayLocation(1)));
  }

  @Test
  public void sameIndexAddditionFirst() {
    AlterationList alts = new AlterationList();
    alts.add(new Addition(0));
    ArrayLocation loc = alts.getLocationOf(0);
    assertTrue(loc.equals(new ArrayLocation(0, 0)));
  }

  @Test
  public void sameIndexAddditionMiddle() {
    AlterationList alts = new AlterationList();

    Addition addition = new Addition(0);
    addition.addElement();
    addition.addElement();

    alts.add(addition);
    ArrayLocation loc = alts.getLocationOf(1);
    assertTrue(loc.equals(new ArrayLocation(0, 1)));
  }

  @Test
  public void sameIndexAddditionLast() {
    AlterationList alts = new AlterationList();

    Addition addition = new Addition(0);
    addition.addElement();
    addition.addElement();

    alts.add(addition);
    ArrayLocation loc = alts.getLocationOf(4);
    assertTrue(loc.equals(new ArrayLocation(1)));
  }

  //Tests when an element is removed to the left of an existing removal.
  @Test
  public void removeToLeftOfRemoval() {
    AlterationList alts = new AlterationList();

    Removal r = new Removal(1);
    r.removeElementToLeft();
    alts.add(r);

    Removal r2 = new Removal(5);
    r2.removeElementToLeft();
    alts.add(r2);

    //gives: [R, R, E, E, R, R, E, ..., E] (R is removal, E is element)

    //test element at start middle and end..
    assertTrue(alts.getLocationOf(0).equals(new ArrayLocation(2)));
    assertEquals(new ArrayLocation(3), alts.getLocationOf(1));
    assertTrue(alts.getLocationOf(9).equals(new ArrayLocation(13)));
  }

  @Test
  public void iteratorWithStartIndexTest() {
    AlterationList alts = new AlterationList();

    for (int i = 0; i < 10; i++) {
      alts.add(new Removal(i * 2));
    }

    Iterator<Alteration> itr = alts.iterator(3);
    assertEquals(true, itr.hasNext());
  }

  //Tests to see if the cached total performance hit is correctly maintained..
  @Test
  public void totalPerformanceHitTest() {
    //set up a list with some of removals and additions..
    AlterationList alts = new AlterationList();

    Addition a = new Addition(10);
    a.addElement();
    a.addElement();
    alts.add(a);

    Removal r = new Removal(0);
    r.indexDiff = 7; //covers indices 0-6
    alts.add(r);

    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //remove middle of r..
    alts.deleteAlterationAtIndex(3);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //remove start of r..
    alts.deleteAlterationAtIndex(0);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //remove old end of r..
    System.out.println(alts);
    alts.deleteAlterationAtIndex(6);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //make the addition two bigger..
    alts.changeIndexDiffForAlterationAtIndex(10, -2);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //make it two smaller again..
    alts.changeIndexDiffForAlterationAtIndex(10, 2);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //make the removal ar index 1 bigger..
    alts.moveRemovalLeftAndIncrementSize(1);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());

    //remove the addition..
    alts.deleteAlterationAtIndex(10);
    assertEquals(getRealPerformanceHit(alts), alts.getTotalPerformanceHit());
  }
}



