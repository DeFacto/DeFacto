package org.aksw.defacto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author http://stackoverflow.com/questions/379551/java-split-a-list-into-two-sub-lists
 */
public class ListUtil {

	/**
	 * @author http://stackoverflow.com/questions/379551/java-split-a-list-into-two-sub-lists
	 * 
	 * Splits a list into smaller sublists. The original list remains unmodified
	 * and changes on the sublists are not propagated to the original list.
	 * 
	 * 
	 * @param original
	 *            The list to split
	 * @param maxListSize
	 *            The max amount of element a sublist can hold.
	 * @param listImplementation
	 *            The implementation of List to be used to create the returned
	 *            sublists
	 * @return A list of sublists
	 * @throws IllegalArgumentException
	 *             if the argument maxListSize is zero or a negative number
	 * @throws NullPointerException
	 *             if arguments original or listImplementation are null
	 */
	public static final <T> List<List<T>> split(final List<T> original, final int maxListSize, final Class<? extends List> listImplementation) {

		if (maxListSize <= 0) {
			throw new IllegalArgumentException("maxListSize must be greater than zero");
		}

		final T[] elements = (T[]) original.toArray();
		final int maxChunks = (int) Math.ceil(elements.length / (double) maxListSize);

		final List<List<T>> lists = new ArrayList<List<T>>(maxChunks);
		for (int i = 0; i < maxChunks; i++) {
			final int from = i * maxListSize;
			final int to = Math.min(from + maxListSize, elements.length);
			final T[] range = Arrays.copyOfRange(elements, from, to);

			lists.add(createSublist(range, listImplementation));
		}

		return lists;
	}

	/**
	 * @author http://stackoverflow.com/questions/379551/java-split-a-list-into-two-sub-lists
	 * 
	 * Splits a list into smaller sublists. The sublists are of type ArrayList.
	 * The original list remains unmodified and changes on the sublists are not
	 * propagated to the original list.
	 * 
	 * 
	 * @param original
	 *            The list to split
	 * @param maxListSize
	 *            The max amount of element a sublist can hold.
	 * @return A list of sublists
	 */
	public static final <T> List<List<T>> split(final List<T> original, final int maxListSize) {

		return split(original, maxListSize, ArrayList.class);
	}

	/**
	 * @author http://stackoverflow.com/questions/379551/java-split-a-list-into-two-sub-lists
	 * 
	 * @param <T>
	 * @param elements
	 * @param listImplementation
	 * @return
	 */
	private static <T> List<T> createSublist(final T[] elements, final Class<? extends List> listImplementation) {

		List<T> sublist;
		final List<T> asList = Arrays.asList(elements);
		try {
			sublist = listImplementation.newInstance();
			sublist.addAll(asList);
		}
		catch (final InstantiationException e) {
			sublist = asList;
		}
		catch (final IllegalAccessException e) {
			sublist = asList;
		}

		return sublist;
	}
}