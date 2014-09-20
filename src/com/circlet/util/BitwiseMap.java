package com.circlet.util;

import java.util.Map;

/**
 * A {@link PrefixMap} treating keys bitwise, providing additional bit-level
 * access with bitwise related query operations.
 * 
 * @author Jason
 * 
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public interface BitwiseMap<K, V> extends PrefixMap<K, V> {
	/**
	 * Returns the bitwise comparator used to order the keys in this map.
	 * 
	 * @return the comparator used to order the keys in this map.
	 */
	BitwiseComparator<? super K> comparator();

	/**
	 * Returns a key-value mapping associated with the closest key in a bitwise
	 * XOR metric to the given key, or {@code null} if there is no such key,
	 * that is, the map is empty. This is NOT lexicographic closeness. For
	 * example, given the keys:
	 * 
	 * <pre>
	 * D = 1000100
	 * H = 1001000
	 * L = 1001100
	 * </pre>
	 * 
	 * If the map contained {@code H} and {@code L}, a lookup of {@code D} would
	 * return {@code L}, because the XOR distance between {@code D} and
	 * {@code L} is smaller than the XOR distance between {@code D} and
	 * {@code H}.
	 * 
	 * @param key the key
	 * @return an entry with the closest key in a bitwise XOR metric to
	 *         {@code key}, or {@code null} if the map is empty.
	 * @throws ClassCastException if the specified key cannot be compared with
	 *         the keys currently in the map
	 * @throws NullPointerException if the specified key is null and this map
	 *         does not permit null keys
	 */
	Map.Entry<K, V> nearestEntry(K key);

	/**
	 * Returns the closest key in a bitwise XOR metric to the given key, or
	 * {@code null} if there is no such key, that is, the map is empty. This is
	 * NOT lexicographic closeness. For example, given the keys:
	 * 
	 * <pre>
	 * D = 1000100
	 * H = 1001000
	 * L = 1001100
	 * </pre>
	 * 
	 * If the map contained {@code H} and {@code L}, a lookup of {@code D} would
	 * return {@code L}, because the XOR distance between {@code D} and
	 * {@code L} is smaller than the XOR distance between {@code D} and
	 * {@code H}.
	 * 
	 * @param key the key
	 * @return the closest key in a bitwise XOR metric to {@code key}, or
	 *         {@code null} if the map is empty.
	 * @throws ClassCastException if the specified key cannot be compared with
	 *         the keys currently in the map
	 * @throws NullPointerException if the specified key is null and this map
	 *         does not permit null keys
	 */
	K nearestKey(K key);

	/**
	 * @throws NullPointerException {@inheritDoc}
	 * @throws ClassCastException {@inheritDoc}
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	BitwiseMap<K, V> subMap(K prefixKey, boolean inclusive);
}
