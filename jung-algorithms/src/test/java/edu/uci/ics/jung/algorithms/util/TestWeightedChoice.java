/**
 * Copyright (c) 2009, The JUNG Authors 
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 * Created on Jan 13, 2009
 * 
 */
package edu.uci.ics.jung.algorithms.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author jrtom
 *
 */
public class TestWeightedChoice extends TestCase 
{
	private WeightedChoice<String> weighted_choice;
	private Map<String, Double> item_weights = new HashMap<String, Double>();
	private Map<String, Integer> item_counts = new HashMap<String, Integer>();
	
	@Override
    public void tearDown()
	{
		item_weights.clear();
		item_counts.clear();
	}

	private void initializeWeights(double[] weights)
	{
		item_weights.put("a", weights[0]);
		item_weights.put("b", weights[1]);
		item_weights.put("c", weights[2]);
		item_weights.put("d", weights[3]);
		
		for (String key : item_weights.keySet())
			item_counts.put(key, 0);

	}

	private void runWeightedChoice()
	{
		weighted_choice = new WeightedChoice<String>(item_weights, new NotRandom(100));
		
		int max_iterations = 10000;
		for (int i = 0; i < max_iterations; i++)
		{
			String item = weighted_choice.nextItem();
			int count = item_counts.get(item);
			item_counts.put(item, count+1);
		}
		
		for (String key : item_weights.keySet())
			assertEquals((int)(item_weights.get(key) * max_iterations), 
						item_counts.get(key).intValue());
	}
	
	public void testUniform() 
	{
		initializeWeights(new double[]{0.25, 0.25, 0.25, 0.25});
		
		runWeightedChoice();
	}
	
	public void testNonUniform()
	{
		initializeWeights(new double[]{0.45, 0.10, 0.13, 0.32});
		
		runWeightedChoice();
	}
}
