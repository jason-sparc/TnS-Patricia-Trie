package com.circlet.util;

import java.util.Comparator;

/**
 * The <tt>BitwiseComparator</tt> provides bit-level access and comparison of
 * objects. It is a specialized {@link Comparator} intended for the comparison
 * of keys in bitwise-based data structures.
 */
public interface BitwiseComparator<T> extends Comparator<T> {
	/**
	 * Returns the length in bits for the specified object. The return value
	 * should never be a negative integer.
	 * 
	 * @param o the object to inspect
	 * @return the length in bits of the specified object.
	 * @throws NullPointerException if an argument is null and this comparator
	 *         does not permit null arguments
	 * @throws ClassCastException if the arguments' types prevent them from
	 *         being compared by this comparator.
	 */
	int lengthBits(T o);

	/**
	 * Returns {@code true} if the bit at the specified index is set to one.
	 * 1-bits are considered higher than 0-bits; therefore, a mechanism might
	 * have been set to make the return value agree with
	 * {@link Comparator#compare(Object, Object) compare()}.
	 * 
	 * @param o the object to inspect
	 * @param index the bit index to inspect
	 * @return {@code true} if the bit at the specified index is set.
	 * @throws IndexOutOfBoundsException if index is out of bounds
	 * @throws NullPointerException if an argument is null and this comparator
	 *         does not permit null arguments
	 * @throws ClassCastException if the arguments' types prevent them from
	 *         being compared by this comparator.
	 */
	boolean isBitSet(T o, int index);

	/**
	 * Returns the index of the first bit that is different between the objects.
	 * If an object's bits is a prefix of another, the returned index should be
	 * the length in bits of that prefix object. Otherwise, if all bits and the
	 * lengths in bits are equal, returns a negative integer (preferably -1).
	 * <p>
	 * The return value should never be greater than the length in bits of the
	 * object with the least lengths in bits. As a side effect, the return value
	 * should never be greater than the lengths in bits of any of the objects.
	 * <p>
	 * The following demonstrates an example usage of the comparator:
	 * 
	 * <pre>
	 * String msg;
	 * int index = comp.contrast(a, b);
	 * if (index == -1) {
	 * 	msg = &quot;Exact match found!&quot;;
	 * } else if (index == comp.lengthBits(a)) {
	 * 	msg = &quot;'a' is a prefix of 'b'&quot;;
	 * } else if (index == comp.lengthBits(b)) {
	 * 	msg = &quot;'b' is a prefix of 'a'&quot;;
	 * } else {
	 * 	msg = &quot;Mismatch was found at bit index &quot; + index;
	 * 	if (comp.isBitSet(a, index)) {
	 * 		msg = &quot;'a' is greater than 'b'&quot;;
	 * 	} else {
	 * 		msg = &quot;'b' is greater than 'a'&quot;;
	 * 	}
	 * }
	 * System.out.println(msg);
	 * </pre>
	 * 
	 * @param o1 the first object to be compared
	 * @param o2 the second object to be compared
	 * @return the index of the first bit that is different between the objects
	 *         or a negative integer if both objects are equal.
	 * @throws NullPointerException if an argument is null and this comparator
	 *         does not permit null arguments
	 * @throws ClassCastException if the arguments' types prevent them from
	 *         being compared by this comparator.
	 * @see BitwiseComparator#lengthBits(T)
	 * @see BitwiseComparator#isBitSet(T, int)
	 */
	int contrast(T o1, T o2);

	/**
	 * <p>
	 * Checks whether the bits of the specified object is prefixed by (or equal
	 * to, if {@code inclusive} is true) the bits of the specified prefix. This
	 * is considered synonymous to (and more efficient than): <blockquote>
	 * 
	 * <pre>
	 * int i = c.contrast(o, prefix);
	 * return inclusive &amp;&amp; i &lt; 0 || i == c.lengthBits(prefix);
	 * </pre>
	 * 
	 * </blockquote>
	 * </p>
	 * 
	 * @param o the object to inspect
	 * @param prefix the prefix
	 * @param inclusive {@code true} if the specified prefix can be equal to the
	 *        specified object.
	 * @return whether the specified object is prefixed by the specified prefix.
	 * @throws NullPointerException if an argument is null and this comparator
	 *         does not permit null arguments
	 * @throws ClassCastException if the arguments' types prevent them from
	 *         being compared by this comparator.
	 * @see BitwiseComparator#contrast(T, int)
	 * @see BitwiseComparator#lengthBits(T)
	 */
	boolean checkPrefixed(T o, T prefix, boolean inclusive);
}
