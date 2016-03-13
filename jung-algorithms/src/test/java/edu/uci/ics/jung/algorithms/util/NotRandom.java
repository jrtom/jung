package edu.uci.ics.jung.algorithms.util;

import java.util.Random;

/**
 * A decidedly non-random extension of {@code Random} that may be useful 
 * for testing random algorithms that accept an instance of {@code Random}
 * as a parameter.  This algorithm maintains internal counters which are 
 * incremented after each call, and returns values which are functions of
 * those counter values.  Thus the output is not only deterministic (as is
 * necessarily true of all software with no externalities) but precisely
 * predictable in distribution.
 * 
 * @author Joshua O'Madadhain
 */
@SuppressWarnings("serial")
public class NotRandom extends Random 
{
	private int i = 0;
	private int d = 0;
	private int size = 100;
	
	/**
	 * Creates an instance with the specified sample size.
	 * @param size the sample size
	 */
	public NotRandom(int size)
	{
		this.size = size;
	}
	
	/**
	 * Returns the post-incremented value of the internal counter modulo n.
	 */
	@Override
  public int nextInt(int n)
	{
		return i++ % n;
	}
	
	/**
	 * Returns the post-incremented value of the internal counter modulo 
	 * {@code size}, divided by {@code size}.
	 */
	@Override
  public double nextDouble()
	{
		return (d++ % size) / (double)size; 
	}
	
	/**
	 * Returns the post-incremented value of the internal counter modulo 
	 * {@code size}, divided by {@code size}.
	 */
	@Override
  public float nextFloat()
	{
		return (d++ % size) / (float)size; 
	}
}
