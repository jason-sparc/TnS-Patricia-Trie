package com.circlet.util;

import java.util.Map;
import java.util.TreeMap;

import com.circlet.util.BitwiseComparator;
import com.circlet.util.PrefixMap;

public final class MockPrefixMap<K, V> extends TreeMap<K, V> implements
		PrefixMap<K, V> {
	private static final long serialVersionUID = 3201827335870304611L;

	/**
	 * The comparator used to maintain order in this prefix map.
	 */
	private transient final BitwiseComparator<? super K> comparator;

	public MockPrefixMap(BitwiseComparator<? super K> comparator) {
		super(comparator);
		this.comparator = comparator;
	}

	private MockPrefixMap(BitwiseComparator<? super K> comparator,
			Map<? extends K, ? extends V> m) {
		super(comparator);
		this.comparator = comparator;
		super.putAll(m);
	}

	@Override
	public Map.Entry<K, V> prefixEntry(K key) {
		BitwiseComparator<? super K> c = comparator;
		Map.Entry<K, V> e = super.floorEntry(key);
		K otherKey;
		if (e == null || c.checkPrefixed(key, otherKey = e.getKey(), true))
			return e;
		for (;;) {
			e = super.lowerEntry(otherKey);
			if (e == null || c.checkPrefixed(key, otherKey = e.getKey(), true))
				return e;
		}
	}

	/*
	 * The following assumes that keys will never be null.
	 */

	@Override
	public K prefixKey(K key) {
		BitwiseComparator<? super K> c = comparator;
		K otherKey = super.floorKey(key);
		if (otherKey == null || c.checkPrefixed(key, otherKey, true))
			return otherKey;
		for (;;) {
			otherKey = super.lowerKey(otherKey);
			if (otherKey == null || c.checkPrefixed(key, otherKey, true))
				return otherKey;
		}
	}

	@Override
	public Map.Entry<K, V> nextPrefixEntry(K key) {
		Map.Entry<K, V> e = super.higherEntry(key);
		if (e == null)
			return null;
		BitwiseComparator<? super K> c = comparator;
		K otherKey;
		while (c.checkPrefixed(otherKey = e.getKey(), key, true)) {
			if ((e = super.higherEntry(otherKey)) == null)
				return null;
		}
		return e;
	}

	@Override
	public K nextPrefixKey(K key) {
		K otherKey = super.higherKey(key);
		if (otherKey == null)
			return null;
		BitwiseComparator<? super K> c = comparator;
		while (c.checkPrefixed(otherKey, key, true)) {
			if ((otherKey = super.higherKey(otherKey)) == null)
				return null;
		}
		return otherKey;
	}

	@Override
	public Map.Entry<K, V> leastPrefixed(K prefixKey, boolean inclusive) {
		Map.Entry<K, V> e;
		if (inclusive) {
			e = super.ceilingEntry(prefixKey);
		} else
			e = super.higherEntry(prefixKey);
		if (e == null || comparator.checkPrefixed(e.getKey(), prefixKey, true))
			return e;
		return null;
	}

	@Override
	public Map.Entry<K, V> lastPrefixed(K prefixKey, boolean inclusive) {
		Map.Entry<K, V> e;
		if (inclusive) {
			e = super.ceilingEntry(prefixKey);
		} else
			e = super.higherEntry(prefixKey);
		if (e == null)
			return null;
		BitwiseComparator<? super K> c = comparator;
		K otherKey;

		Map.Entry<K, V> r = null;
		for (;;) {
			if (!c.checkPrefixed(otherKey = e.getKey(), prefixKey, true))
				return r;
			r = e;
			if (((e = super.higherEntry(otherKey)) == null))
				return r;
		}
	}

	/**
	 * Creates a new map with shallow copies of entries whose keys are prefixed
	 * by prefixKey. This is a fake implementation of
	 * {@link #subMap(Object, boolean)}.
	 */
	@Override
	public PrefixMap<K, V> subMap(K prefixKey, boolean inclusive) {
		K nextPrefix = nextPrefixKey(prefixKey);
		return new MockPrefixMap<>(comparator,
				nextPrefix == null ? super.tailMap(prefixKey, inclusive)
						: super.subMap(prefixKey, inclusive, nextPrefix, false));
	}
}
