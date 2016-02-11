package edu.uci.ics.jung.graph.util;

import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.jung.graph.util.Pair;

import junit.framework.TestCase;

public class PairTest extends TestCase {
    
    Pair<Number> pair;
    

    @Override
    protected void setUp() throws Exception {
        pair = new Pair<Number>(1,2);
        super.setUp();
    }

    public void testGetFirst() {
        assertEquals(pair.getFirst(), 1);
    }

    public void testGetSecond() {
        assertEquals(pair.getSecond(), 2);
    }

    public void testEqualsObject() {
        Pair<Number> ipair = new Pair<Number>(1,2);
        assertTrue(pair.equals(ipair));
    }

    public void testAdd() {
        try {
            pair.add(3);
            fail("should not be able to add to Pair");
        } catch(Exception e) { 
            // all is well
        }
    }

    public void testAddAll() {
        try {
            List<Number> list = new ArrayList<Number>(pair);
            pair.addAll(list);
            fail("should not be able to addAll to Pair");
        } catch(Exception e) { 
            // all is well
        }
    }

    public void testClear() {
        try {
            pair.clear();
            fail("should not be able to clear a Pair");
        } catch(Exception e) { 
            // all is well
        }
    }

    public void testContains() {
        assertTrue(pair.contains(1));
    }

    public void testContainsAll() {
        List<Number> list = new ArrayList<Number>(pair);
        assertTrue(pair.containsAll(list));
    }

    public void testIsEmpty() {
        assertFalse(pair.isEmpty());
    }

    public void testRemove() {
        try {
            pair.remove(1);
            fail("should not be able to remove from a Pair");
        } catch(Exception e) { 
            // all is well
        }
    }

    public void testRemoveAll() {
        try {
            List<Number> list = new ArrayList<Number>(pair);
            pair.removeAll(list);
            fail("should not be able to removeAll from Pair");
        } catch(Exception e) { 
            // all is well
        }
    }

    public void testRetainAll() {
        try {
            List<Number> list = new ArrayList<Number>(pair);
            pair.retainAll(list);
            fail("should not be able to retainAll from Pair");
        } catch(Exception e) { 
            // all is well
        }
    }

    public void testSize() {
        assertEquals(pair.size(), 2);
    }

    public void testToArray() {
        @SuppressWarnings("unused")
        Object[] arr = pair.toArray();
    }

    public void testToArraySArray() {
        @SuppressWarnings("unused")
        Integer[] arr = pair.<Integer>toArray(new Integer[2]);
    }

}
