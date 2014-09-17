package com.circlet.util;

import java.util.Map;
import java.util.NavigableMap;

public interface PrefixMap<K, V> extends NavigableMap<K, V> {
	/**
	 * Returns a key-value mapping associated with the longest key that is a
	 * prefix of or equal to the given key, or {@code null} if there is no such
	 * key.
	 * 
	 * @param key
	 * @return
	 */
	Map.Entry<K, V> prefixEntry(K key);

	/**
	 * Returns the longest key that is a prefix of or equal to the given key, or
	 * {@code null} if there is no such key.
	 * 
	 * @param key
	 * @return
	 */
	K prefixKey(K key);

	/**
	 * Returns a key-value mapping associated with the least key strictly
	 * greater than but is not prefixed by the given key, or {@code null} if
	 * there is no such key.
	 * 
	 * @param key
	 * @return
	 */
	Map.Entry<K, V> nextPrefixEntry(K key);

	/**
	 * Returns the least key strictly greater than but is not prefixed by the
	 * given key, or {@code null} if there is no such key.
	 * 
	 * @param key
	 * @return
	 */
	K nextPrefixKey(K key);

	/**
	 * Returns a key-value mapping associated with the least key that is
	 * prefixed by (or equal to, if {@code inclusive} is true) the given key, or
	 * {@code null} if there is no such key.
	 * 
	 * @param key
	 * @return
	 */
	Map.Entry<K, V> leastPrefixed(K prefixKey, boolean inclusive);

	/**
	 * Returns a key-value mapping associated with the greatest key that is
	 * prefixed by (or equal to, if {@code inclusive} is true) the given key, or
	 * {@code null} if there is no such key.
	 * 
	 * @param key
	 * @return
	 */
	Map.Entry<K, V> lastPrefixed(K prefixKey, boolean inclusive);

	/**
	 * Returns a view of the portion of this map whose keys are prefixed by (or
	 * equal to, if {@code inclusive} is true) {@code prefixKey}. The returned
	 * map is backed by this map, so changes in the returned map are reflected
	 * in this map, and vice-versa. The returned map supports all optional map
	 * operations that this map supports.
	 * <p>
	 * The returned map will throw an {@code IllegalArgumentException} on an
	 * attempt to insert a key outside its range.
	 * 
	 * @param prefixKey the prefix of the keys in the returned map
	 * @param inclusive {@code true} if the prefix key is to be included in the
	 *        returned view
	 * @return a view of the portion of this map whose keys are prefixed by
	 *         {@code prefixKey}.
	 * @throws NullPointerException if {@code prefixKey} is null and this map
	 *         does not permit null keys
	 * @throws ClassCastException if {@code prefixKey} is not compatible with
	 *         this map's comparator.
	 * @throws IllegalArgumentException if this map itself has a restricted
	 *         range, and {@code prefixKey} lies outside the bounds of the range
	 */
	PrefixMap<K, V> subMap(K prefixKey, boolean inclusive);
}
