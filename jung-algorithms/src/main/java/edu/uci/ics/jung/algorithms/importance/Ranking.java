/*
* Copyright (c) 2003, The JUNG Authors 
*
* All rights reserved.
*
* This software is open-source under the BSD license; see either
* "license.txt" or
* https://github.com/jrtom/jung/blob/master/LICENSE for a description.
*/
package edu.uci.ics.jung.algorithms.importance;


/**
 * Abstract data container for ranking objects. Stores common data relevant to both node and edge rankings, namely,
 * the original position of the instance in the list and the actual ranking score.
 * @author Scott White
 */
@SuppressWarnings("rawtypes")
public class Ranking<V> implements Comparable {
    /**
     * The original (0-indexed) position of the instance being ranked
     */
    public int originalPos;
    /**
     * The actual rank score (normally between 0 and 1)
     */
    public double rankScore;
    
    /**
     * what is being ranked
     */
    private V ranked;

    /**
     * Constructor which allows values to be set on construction
     * @param originalPos The original (0-indexed) position of the instance being ranked
     * @param rankScore The actual rank score (normally between 0 and 1)
     * @param ranked the vertex being ranked
     */
    public Ranking(int originalPos, double rankScore, V ranked) {
        this.originalPos = originalPos;
        this.rankScore = rankScore;
        this.ranked = ranked;
    }

    /**
     * Compares two ranking based on the rank score.
     * @param other The other ranking
     * @return -1 if the other ranking is higher, 0 if they are equal, and 1 if this ranking is higher
     */
    public int compareTo(Object other) {
    	@SuppressWarnings("unchecked")
		Ranking<V> otherRanking = (Ranking<V>) other;
        return Double.compare(otherRanking.rankScore,rankScore);
    }

    /**
     * Returns the rank score as a string.
     * @return the stringified rank score
     */
    @Override
    public String toString() {
        return String.valueOf(rankScore);
    }

	/**
	 * @return the ranked element
	 */
	public V getRanked() {
		return ranked;
	}

	/**
	 * @param ranked the ranked to set
	 */
	public void setRanked(V ranked) {
		this.ranked = ranked;
	}
}
