package com.circlet.util;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

@SuppressWarnings("unchecked")
public class PatriciaTrie<K, V> extends AbstractPatriciaTrie<K, V> implements
		java.io.Serializable {
	private static final long serialVersionUID = 4746504995846960416L;

	public PatriciaTrie(BitwiseComparator<? super K> comparator) {
		super(comparator);
	}

	@Override
	public boolean containsKey(Object key) {
		return super.getEntry(key) != null;
	}

	@Override
	public V get(Object key) {
		Entry<K, V> e = super.getEntry(key);
		if (e != null)
			return e.value;
		return null;
	}

	@Override
	public V remove(Object key) {
		Entry<K, V> e = super.getEntry(key);
		if (e != null) {
			super.deleteEntry(e);
			return e.value;
		}
		return null;
	}

	@Override
	public Map.Entry<K, V> nearestEntry(K key) {
		return super.nearestEntry(key, false);
	}

	@Override
	public K nearestKey(K key) {
		return super.nearestEntry(key, true).key;
	}

	@Override
	public Map.Entry<K, V> prefixEntry(K key) {
		return super.prefixEntry(key, false);
	}

	@Override
	public K prefixKey(K key) {
		return super.prefixEntry(key, true).key;
	}

	@Override
	public Map.Entry<K, V> lowerEntry(K key) {
		return super.headOf(key, S_DEFAULT);
	}

	@Override
	public K lowerKey(K key) {
		return super.headOf(key, S_NEVER_NULL).key;
	}

	@Override
	public Map.Entry<K, V> floorEntry(K key) {
		return super.headOf(key, S_INCLUSIVE);
	}

	@Override
	public K floorKey(K key) {
		return super.headOf(key, S_INCLUSIVE | S_NEVER_NULL).key;
	}

	@Override
	public Map.Entry<K, V> ceilingEntry(K key) {
		return super.tailOf(key, S_INCLUSIVE);
	}

	@Override
	public K ceilingKey(K key) {
		return super.tailOf(key, S_INCLUSIVE | S_NEVER_NULL).key;
	}

	@Override
	public Map.Entry<K, V> higherEntry(K key) {
		return super.tailOf(key, S_DEFAULT);
	}

	@Override
	public K higherKey(K key) {
		return super.tailOf(key, S_NEVER_NULL).key;
	}

	@Override
	public Map.Entry<K, V> nextPrefixEntry(K key) {
		return super.tailOf(key, S_NEXT_PREFIX);
	}

	@Override
	public K nextPrefixKey(K key) {
		return super.tailOf(key, S_NEXT_PREFIX | S_NEVER_NULL).key;
	}

	@Override
	public Map.Entry<K, V> leastPrefixed(K prefixKey, boolean inclusive) {
		return super.getLeastPrefixed(prefixKey, inclusive);
	}

	@Override
	public Map.Entry<K, V> lastPrefixed(K prefixKey, boolean inclusive) {
		return super.getLastPrefixed(prefixKey, inclusive);
	}

	@Override
	public Map.Entry<K, V> firstEntry() {
		return super.getFirstEntry();
	}

	@Override
	public Map.Entry<K, V> lastEntry() {
		return super.getLastEntry();
	}

	// Views
	/**
	 * Fields initialized to contain an instance of the entry set view the first
	 * time this view is requested. Views are stateless, so there's no reason to
	 * create more than one.
	 */
	private transient EntrySet entrySet;
	private transient KeySet<K> navigableKeySet;
	private transient NavigableMap<K, V> descendingMap;

	@Override
	public Set<K> keySet() {
		return navigableKeySet();
	}

	@Override
	public NavigableSet<K> navigableKeySet() {
		KeySet<K> nks = navigableKeySet;
		return (nks != null) ? nks : (navigableKeySet = new KeySet<>(this));
	}

	@Override
	public NavigableSet<K> descendingKeySet() {
		return descendingMap().navigableKeySet();
	}

	@Override
	public NavigableMap<K, V> descendingMap() {
		NavigableMap<K, V> km = descendingMap;
		return (km != null) ? km : (descendingMap = new DescSubMap<>(this,
				true, null, true, true, null, true));
	}

	/**
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} is null and the
	 *         comparator does not permit null keys.
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public NavigableMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey,
			boolean toInclusive) {
		return new AscSubMap<>(this, false, fromKey, fromInclusive, false,
				toKey, toInclusive);
	}

	/**
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} is null and the
	 *         comparator does not permit null keys.
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
		return new AscSubMap<>(this, true, null, true, false, toKey, inclusive);
	}

	/**
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} is null and the
	 *         comparator does not permit null keys.
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
		return new AscSubMap<>(this, false, fromKey, inclusive, true, null,
				true);
	}

	/**
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} is null and the
	 *         comparator does not permit null keys.
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		return subMap(fromKey, true, toKey, false);
	}

	/**
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} is null and the
	 *         comparator does not permit null keys.
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> headMap(K toKey) {
		return headMap(toKey, false);
	}

	/**
	 * @throws ClassCastException {@inheritDoc}
	 * @throws NullPointerException if {@code fromKey} is null and the
	 *         comparator does not permit null keys.
	 * @throws IllegalArgumentException {@inheritDoc}
	 */
	@Override
	public SortedMap<K, V> tailMap(K fromKey) {
		return tailMap(fromKey, true);
	}

	@Override
	public BitwiseMap<K, V> subMap(K prefixKey, boolean inclusive) {
		return new AscPrefixSubMap<>(this, prefixKey, inclusive);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		EntrySet es = entrySet;
		return (es != null) ? es : (entrySet = new EntrySet());
	}

	private final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		EntrySet() {
		}

		@Override
		public Iterator<Map.Entry<K, V>> iterator() {
			return new AscEntryIterator<>(PatriciaTrie.this);
		}

		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			PatriciaTrie.Entry<K, V> e;
			Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			return (e = getEntry(entry.getKey())) != null
					&& eq(e.value, entry.getValue());
		}

		public boolean remove(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			PatriciaTrie.Entry<K, V> e;
			Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
			if ((e = getEntry(entry.getKey())) != null
					&& eq(e.value, entry.getValue())) {
				deleteEntry(e);
				return true;
			}
			return false;
		}

		public int size() {
			return PatriciaTrie.this.size();
		}

		public void clear() {
			PatriciaTrie.this.clear();
		}
	}

	final Iterator<K> keyIterator() {
		return new AscKeyIterator<>(this);
	}

	final Iterator<K> descKeyIterator() {
		return new DescKeyIterator<>(this);
	}

	private static final class KeySet<E> extends AbstractSet<E> implements
			NavigableSet<E> {
		private final AbstractNavigableMap<E, ?> m;

		// @formatter:off
		KeySet(AbstractNavigableMap<E, ?> map) { m = map; }
		private KeySet(NavigableMap<E, ?> map) { m = (AbstractNavigableMap<E, ?>) map; }
	
		public Iterator<E> iterator() { return m.keyIterator(); }
		public Iterator<E> descendingIterator() { return m.descKeyIterator(); }
		public int size() { return m.size(); }
		public boolean isEmpty() { return m.isEmpty(); }
		public boolean contains(Object o) { return m.containsKey(o); }
		public void clear() { m.clear(); }
		public E lower(E e) { return m.lowerKey(e); }
		public E floor(E e) { return m.floorKey(e); }
		public E ceiling(E e) { return m.ceilingKey(e); }
		public E higher(E e) { return m.higherKey(e); }
		public E first() { return m.firstKey(); }
		public E last() { return m.lastKey(); }
		public Comparator<? super E> comparator() { return m.comparator(); }
		public E pollFirst() {
			Map.Entry<E,?> e = m.pollFirstEntry();
			return (e == null) ? null : e.getKey();
		}
		public E pollLast() {
			Map.Entry<E,?> e = m.pollLastEntry();
			return (e == null) ? null : e.getKey();
		}
		public boolean remove(Object o) {
			int oldSize = size();
	        m.remove(o);
	        return size() != oldSize;
		}
		public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
									  E toElement,   boolean toInclusive) {
			return new KeySet<>(m.subMap(fromElement, fromInclusive, toElement, toInclusive));
		}
		public NavigableSet<E> headSet(E toElement, boolean inclusive) {
			return new KeySet<>(m.headMap(toElement, inclusive));
		}
		public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
			return new KeySet<>(m.tailMap(fromElement, inclusive));
		}
		public SortedSet<E> subSet(E fromElement, E toElement) {
			return subSet(fromElement, true, toElement, false);
		}
		public SortedSet<E> headSet(E toElement) {
			return headSet(toElement, false);
		}
		public SortedSet<E> tailSet(E fromElement) {
			return tailSet(fromElement, true);
		}
		public NavigableSet<E> descendingSet() {
			return new KeySet<>(m.descendingMap());
		}
	    // @formatter:on
	}

	/**
	 * Base class for PatriciaTrie Iterators
	 */
	private static abstract class PrivateIterator<T, K, V> implements
			Iterator<T> {
		private final PatriciaTrie<K, V> trie;
		private Entry<K, V> next;
		private Entry<K, V> lastReturned;
		private int expectedModCount;

		PrivateIterator(PatriciaTrie<K, V> trie) {
			this.expectedModCount = trie.modCount;
			this.trie = trie;
			// Gets the first entry
			Entry<K, V> r = trie.root;
			if (r.key == null) {
				Entry<K, V> s = r.switches;
				if (s != null) {
					next = nextOrNode(s);
					return; // early exit
				}
			}
			next = r;
		}

		PrivateIterator(PatriciaTrie<K, V> trie, Entry<K, V> first) {
			this.expectedModCount = trie.modCount;
			this.trie = trie;
			this.next = first;
		}

		public final boolean hasNext() {
			return next.bit != UNSET;
		}

		final Entry<K, V> nextEntry() {
			Entry<K, V> e = next;
			if (e.bit == UNSET)
				throw new NoSuchElementException();
			if (trie.modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = successorOf(e);
			lastReturned = e;
			return e;
		}

		final Entry<K, V> prevEntry() {
			Entry<K, V> e = next;
			if (e.bit == UNSET)
				throw new NoSuchElementException();
			if (trie.modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = predecessorOf(e);
			lastReturned = e;
			return e;
		}

		public final void remove() {
			Entry<K, V> p = lastReturned;
			if (p == null)
				throw new IllegalStateException();
			final PatriciaTrie<K, V> t = trie;
			if (t.modCount != expectedModCount)
				throw new ConcurrentModificationException();
			t.deleteEntry(p);
			expectedModCount = t.modCount;
			lastReturned = null;
		}
	}

	// @formatter:off
	private static final class AscEntryIterator<K, V> extends
			PrivateIterator<Map.Entry<K, V>, K, V> {
		AscEntryIterator(PatriciaTrie<K, V> trie) { super(trie); }
		public Map.Entry<K, V> next() { return super.nextEntry(); }
	}
	private static final class AscKeyIterator<K, V> extends
			PrivateIterator<K, K, V> {
		AscKeyIterator(PatriciaTrie<K, V> trie) { super(trie); }
		public K next() { return super.nextEntry().key; }
	}
	private static final class DescEntryIterator<K, V> extends
			PrivateIterator<Map.Entry<K, V>, K, V> {
		DescEntryIterator(PatriciaTrie<K, V> trie) {
			super(trie, apexOf(trie.root));
		}
		public Map.Entry<K, V> next() { return super.prevEntry(); }
	}
	private static final class DescKeyIterator<K, V> extends
			PrivateIterator<K, K, V> {
		DescKeyIterator(PatriciaTrie<K, V> trie) {
			super(trie, apexOf(trie.root));
		}
		public K next() { return super.nextEntry().key; }
	}
	// @formatter:on

	final void rangeClear(Entry<K, V> s, Entry<K, V> e) {
		if (s == root) {
			modCount++;
			Entry<K, V> link = s.switches;
			if (link == null) {
				size = 0;
				root = new Entry<>();
				return;
			}
			size--;
			s.switches = null;
			root = new Entry<>(link);
			s = lastLinked(link);
		}

		while (s != e) {
			modCount++;
			size--;
			Entry<K, V> n = successorOf(s);
			detach(s);
			s = n;
		}
	}

	/** @serial include */
	@SuppressWarnings("serial")
	private abstract static class NavigableSubMap<K, V> extends
			AbstractNavigableMap<K, V> implements java.io.Serializable {
		/**
		 * The backing map.
		 */
		final PatriciaTrie<K, V> t;

		/**
		 * Endpoints are represented as triples (fromStart, lo, loInclusive) and
		 * (toEnd, hi, hiInclusive). If fromStart is true, then the low
		 * (absolute) bound is the start of the backing map, and the other
		 * values are ignored. Otherwise, if loInclusive is true, lo is the
		 * inclusive bound, else lo is the exclusive bound. Similarly for the
		 * upper bound.
		 */
		final K lo, hi;
		final boolean fromStart, toEnd;
		final boolean loInclusive, hiInclusive;

		NavigableSubMap(PatriciaTrie<K, V> t, boolean fromStart, K lo,
				boolean loInclusive, boolean toEnd, K hi, boolean hiInclusive) {
			if (!fromStart && !toEnd) {
				if (t.comparator.compare(lo, hi) > 0)
					throw new IllegalArgumentException("fromKey > toKey");
			} else { // type check
				if (!fromStart)
					t.comparator.lengthBits(lo);
				if (!toEnd)
					t.comparator.lengthBits(hi);
			}

			this.t = t;
			this.fromStart = fromStart;
			this.lo = lo;
			this.loInclusive = loInclusive;
			this.toEnd = toEnd;
			this.hi = hi;
			this.hiInclusive = hiInclusive;
		}

		// Internal utilities

		final boolean tooLow(K key) {
			if (!fromStart) {
				int c = t.comparator.compare(key, lo);
				if (c < 0 || (c == 0 && !loInclusive))
					return true;
			}
			return false;
		}

		final boolean tooHigh(K key) {
			if (!toEnd) {
				int c = t.comparator.compare(key, hi);
				if (c > 0 || (c == 0 && !hiInclusive))
					return true;
			}
			return false;
		}

		final boolean inRange(Object key) {
			return !tooLow((K) key) && !tooHigh((K) key);
		}

		final boolean inClosedRange(K key) {
			return (fromStart || t.comparator.compare(key, lo) >= 0)
					&& (toEnd || t.comparator.compare(key, hi) <= 0);
		}

		final boolean inRange(Object key, boolean inclusive) {
			return inclusive ? inRange(key) : inClosedRange((K) key);
		}

		/*
		 * Absolute versions of relation operations. Subclasses map to these
		 * versions that invert senses for descending maps
		 */

		final PatriciaTrie.Entry<K, V> absLowest(boolean neverNull) {
			PatriciaTrie.Entry<K, V> e = (fromStart ? t.getFirstEntry() : (t
					.tailOf(lo, loInclusive ? S_INCLUSIVE : S_DEFAULT)));
			if (e == null || tooHigh(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absHighest(boolean neverNull) {
			PatriciaTrie.Entry<K, V> e = (toEnd ? t.getLastEntry() : (t.headOf(
					hi, hiInclusive ? S_INCLUSIVE : S_DEFAULT)));
			if (e == null || tooLow(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absCeiling(K key, boolean neverNull) {
			if (tooLow(key))
				return absLowest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.tailOf(key, S_INCLUSIVE);
			if (e == null || tooHigh(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absHigher(K key, boolean neverNull) {
			if (tooLow(key))
				return absLowest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.tailOf(key, S_DEFAULT);
			if (e == null || tooHigh(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absFloor(K key, boolean neverNull) {
			if (tooHigh(key))
				return absHighest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.headOf(key, S_INCLUSIVE);
			if (e == null || tooLow(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absLower(K key, boolean neverNull) {
			if (tooHigh(key))
				return absHighest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.headOf(key, S_DEFAULT);
			if (e == null || tooLow(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		/** Fixes the case where the unbounded low fence is the root node. */
		private PatriciaTrie.Entry<K, V> unboundLowFence() {
			PatriciaTrie.Entry<K, V> r = t.root;
			return r.bit == ROOT_UNSET ? r : DUMMY;
		}

		/** Returns the absolute high fence for ascending traversal */
		final PatriciaTrie.Entry<K, V> absHighFence() {
			return toEnd ? DUMMY : t.tailOf(hi, hiInclusive ? S_NEVER_NULL
					: S_NEVER_NULL | S_INCLUSIVE);
		}

		/** Return the absolute low fence for descending traversal */
		final PatriciaTrie.Entry<K, V> absLowFence() {
			return fromStart ? unboundLowFence() : t.headOf(lo,
					loInclusive ? S_NEVER_NULL : S_NEVER_NULL | S_INCLUSIVE);
		}

		final PatriciaTrie.Entry<K, V> absPollLowest() {
			PatriciaTrie.Entry<K, V> e = (fromStart ? t.getFirstEntry() : (t
					.tailOf(lo, loInclusive ? S_INCLUSIVE : S_DEFAULT)));
			if (e == null || tooHigh(e.key))
				return null;
			t.deleteEntry(e);
			return e;
		}

		final PatriciaTrie.Entry<K, V> absPollHighest() {
			PatriciaTrie.Entry<K, V> e = (toEnd ? t.getLastEntry() : (t.headOf(
					hi, hiInclusive ? S_INCLUSIVE : S_DEFAULT)));
			if (e == null || tooLow(e.key))
				return null;
			t.deleteEntry(e);
			return e;
		}

		/** Returns ascending iterator from the perspective of this submap */
		abstract Iterator<K> keyIterator();

		/** Returns descending iterator from the perspective of this submap */
		abstract Iterator<K> descKeyIterator();

		// Public methods

		public final boolean isEmpty() {
			return (fromStart && toEnd) ? t.isEmpty() : entrySet().isEmpty();
		}

		public final int size() {
			return (fromStart && toEnd) ? t.size() : entrySet().size();
		}

		public final void clear() {
			PatriciaTrie.Entry<K, V> lowest = absLowest(false);
			if (lowest != null)
				t.rangeClear(lowest, absHighFence());
		}

		public final boolean containsKey(Object key) {
			return inRange(key) && t.containsKey(key);
		}

		public final V put(K key, V value) {
			if (!inRange(key))
				throw new IllegalArgumentException("key out of range");
			return t.put(key, value);
		}

		public final V get(Object key) {
			return !inRange(key) ? null : t.get(key);
		}

		public final V remove(Object key) {
			return !inRange(key) ? null : t.remove(key);
		}

		// Views
		transient NavigableMap<K, V> descendingMapView = null;
		transient EntrySetView entrySetView = null;
		transient KeySet<K> navigableKeySetView = null;

		public final NavigableSet<K> navigableKeySet() {
			KeySet<K> nksv = navigableKeySetView;
			return (nksv != null) ? nksv : (navigableKeySetView = new KeySet<>(
					this));
		}

		public final Set<K> keySet() {
			return navigableKeySet();
		}

		public final NavigableSet<K> descendingKeySet() {
			return descendingMap().navigableKeySet();
		}

		public final SortedMap<K, V> subMap(K fromKey, K toKey) {
			return subMap(fromKey, true, toKey, false);
		}

		public final SortedMap<K, V> headMap(K toKey) {
			return headMap(toKey, false);
		}

		public final SortedMap<K, V> tailMap(K fromKey) {
			return tailMap(fromKey, true);
		}

		public final Set<java.util.Map.Entry<K, V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : (entrySetView = new EntrySetView());
		}

		abstract Iterator<Map.Entry<K, V>> entryIterator();

		private final class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
			private transient int size, sizeModCount = -1;

			EntrySetView() {
			}

			public Iterator<Map.Entry<K, V>> iterator() {
				return entryIterator();
			}

			public int size() {
				if (fromStart && toEnd)
					return t.size();
				if (sizeModCount < 0 || sizeModCount != t.modCount) {
					sizeModCount = t.modCount;
					int size = 0;
					for (Iterator<?> i = iterator(); i.hasNext(); i.next())
						size++;
					this.size = size;
				}
				return size;
			}

			public boolean isEmpty() {
				return absLowest(false) == null;
			}

			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				PatriciaTrie.Entry<K, V> e;
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
				K key;

				if (!inRange((key = entry.getKey())))
					return false;
				return (e = t.getEntry(key)) != null
						&& eq(e.value, entry.getValue());
			}

			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				PatriciaTrie.Entry<K, V> e;
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
				K key;

				if (!inRange((key = entry.getKey())))
					return false;
				if ((e = t.getEntry(key)) != null
						&& eq(e.value, entry.getValue())) {
					t.deleteEntry(e);
					return true;
				}
				return false;
			}
		}
	}

	/** @serial include */
	private static final class AscSubMap<K, V> extends NavigableSubMap<K, V> {
		private static final long serialVersionUID = -1044217809887122368L;

		// @formatter:off
		AscSubMap(PatriciaTrie<K, V> t,
				boolean fromStart,	K lo, boolean loInclusive,
				boolean toEnd,		K hi, boolean hiInclusive) {
			super(t, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}

		public Comparator<? super K> comparator() { return t.comparator; }

		public NavigableMap<K, V> subMap(K fromKey,	boolean fromInclusive,
										 K toKey,	boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new AscSubMap<>(t,
								false, fromKey,	fromInclusive,
								false, toKey,	toInclusive);
		}
		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new AscSubMap<>(t,
								fromStart,	lo,		loInclusive,
								false,		toKey,	inclusive);
		}
		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new AscSubMap<>(t,
								false, fromKey, inclusive,
								toEnd, hi,		hiInclusive);
		}
		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new DescSubMap<>(t,
					fromStart, lo, loInclusive, toEnd, hi, hiInclusive));
		}
		Iterator<Map.Entry<K, V>> entryIterator() {
			return new AscSubMapIterator<>(t, absLowest(true), absHighFence());
		}
		Iterator<K> keyIterator() {
			return new AscSubMapKeyIterator<>(t, absLowest(true), absHighFence());
		}
		Iterator<K> descKeyIterator() {
			return new DescSubMapKeyIterator<>(t, absHighest(true), absLowFence());
		}
		public Map.Entry<K, V> firstEntry()			{ return absLowest(false); }
		public Map.Entry<K, V> lastEntry()			{ return absHighest(false); }
		public K firstKey()							{ return absLowest(true).key; }
		public K lastKey()							{ return absHighest(true).key; }
		public Map.Entry<K, V> ceilingEntry(K key)	{ return absCeiling(key, false); }
		public K ceilingKey(K key)					{ return absCeiling(key, true).key; }
		public Map.Entry<K, V> higherEntry(K key)	{ return absHigher(key, false); }
		public K higherKey(K key)					{ return absHigher(key, true).key; }
		public Map.Entry<K, V> floorEntry(K key)	{ return absFloor(key, false); }
		public K floorKey(K key)					{ return absFloor(key, true).key; }
		public Map.Entry<K, V> lowerEntry(K key)	{ return absLower(key, false); }
		public K lowerKey(K key)					{ return absLower(key, true).key; }
		public Map.Entry<K, V> pollFirstEntry()		{ return absPollLowest(); }
		public Map.Entry<K, V> pollLastEntry()		{ return absPollHighest(); }
		// @formatter:on
	}

	/** @serial include */
	private static final class DescSubMap<K, V> extends NavigableSubMap<K, V> {
		private static final long serialVersionUID = 4569750630437153075L;

		// @formatter:off
		DescSubMap(PatriciaTrie<K, V> t,
				boolean fromStart,	K lo, boolean loInclusive,
				boolean toEnd,		K hi, boolean hiInclusive) {
			super(t, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
		}

		private final Comparator<? super K> reverseComparator = Collections
				.reverseOrder(t.comparator);

		public Comparator<? super K> comparator() { return reverseComparator; }

		public NavigableMap<K, V> subMap(K fromKey,	boolean fromInclusive,
										 K toKey,	boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescSubMap<>(t,
									false, toKey, toInclusive,
									false, fromKey, fromInclusive);
		}
		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescSubMap<>(t,
									false, toKey,	inclusive,
									toEnd, hi,		hiInclusive);
		}
		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new DescSubMap<>(t,
									fromStart,	lo,			loInclusive,
									false,		fromKey,	inclusive);
		}
		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new AscSubMap<>(t,
					fromStart, lo, loInclusive, toEnd, hi, hiInclusive));
		}
		Iterator<Map.Entry<K, V>> entryIterator() {
			if (fromStart && toEnd)
				return new DescEntryIterator<>(t);
			return new DescSubMapIterator<>(t, absHighest(true), absLowFence());
		}
		Iterator<K> keyIterator() {
			return new DescSubMapKeyIterator<>(t, absHighest(true), absLowFence());
		}
		Iterator<K> descKeyIterator() {
			return new AscSubMapKeyIterator<>(t, absLowest(true), absHighFence());
		}
		public Map.Entry<K, V> firstEntry()			{ return absHighest(false); }
		public Map.Entry<K, V> lastEntry()			{ return absLowest(false); }
		public K firstKey()							{ return absHighest(true).key; }
		public K lastKey()							{ return absLowest(true).key; }
		public Map.Entry<K, V> ceilingEntry(K key)	{ return absFloor(key, false); }
		public K ceilingKey(K key)					{ return absFloor(key, true).key; }
		public Map.Entry<K, V> higherEntry(K key)	{ return absLower(key, false); }
		public K higherKey(K key)					{ return absLower(key, true).key; }
		public Map.Entry<K, V> floorEntry(K key)	{ return absCeiling(key, false); }
		public K floorKey(K key)					{ return absCeiling(key, true).key; }
		public Map.Entry<K, V> lowerEntry(K key)	{ return absHigher(key, false); }
		public K lowerKey(K key)					{ return absHigher(key, true).key; }
		public Map.Entry<K, V> pollFirstEntry()		{ return absPollHighest(); }
		public Map.Entry<K, V> pollLastEntry()		{ return absPollLowest(); }
		// @formatter:on
	}

	/** @serial include */
	@SuppressWarnings("serial")
	private static abstract class PrefixSubMap<K, V> extends
			AbstractNavigableMap<K, V> implements java.io.Serializable {
		/**
		 * The backing map.
		 */
		final PatriciaTrie<K, V> t;

		final K prefixKey, loKey;
		final int prefixLength, loLength;
		final boolean loInclusive;

		PrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, boolean inclusive) {
			int prefixLength = t.comparator.lengthBits(prefixKey);
			this.t = t;
			this.prefixKey = prefixKey;
			this.prefixLength = prefixLength;
			this.loKey = prefixKey;
			this.loLength = prefixLength;
			this.loInclusive = inclusive;
		}

		PrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, int prefixLength,
				K loKey, boolean loInclusive) {
			this.t = t;
			this.prefixKey = prefixKey;
			this.prefixLength = prefixLength;
			this.loKey = loKey;
			this.loLength = t.comparator.lengthBits(loKey);
			this.loInclusive = loInclusive;
		}

		PrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, int prefixLength,
				K loKey, int loLength, boolean loInclusive) {
			this.t = t;
			this.prefixKey = prefixKey;
			this.prefixLength = prefixLength;
			this.loKey = loKey;
			this.loLength = loLength;
			this.loInclusive = loInclusive;
		}

		// Internal utilities

		final boolean tooLow(Object key) {
			int i = t.comparator.compare(key, loKey);
			return i < 0 || i == 0 && !loInclusive;
		}

		final boolean tooHigh(Object key) {
			BitwiseComparator<? super Object> c = t.comparator;
			int i = c.contrast(key, prefixKey);
			return i >= 0 && i < prefixLength && i != c.lengthBits(key)
					&& c.isBitSet(key, i);
		}

		final boolean isPrefixed(Object key) {
			return t.comparator.checkPrefixed(key, prefixKey, true);
		}

		final boolean isPrefixedByLowKey(Object key) {
			return t.comparator.checkPrefixed(key, loKey, loInclusive);
		}

		final boolean inRange(Object key) {
			BitwiseComparator<? super Object> c = t.comparator;
			int i = c.contrast(key, loKey);
			if (i < 0)
				return loInclusive;
			if (i >= prefixLength) {
				return i == loLength || i != c.lengthBits(key)
						&& c.isBitSet(key, i);
			}
			return false;
		}

		final boolean inRange(Object key, boolean inclusive) {
			BitwiseComparator<? super Object> c = t.comparator;
			int i = c.contrast(key, loKey);
			if (i < 0)
				return !inclusive || loInclusive;
			if (i >= prefixLength)
				return i == loLength || c.isBitSet(key, i);
			return false;
		}

		/*
		 * Absolute versions of relation operations. Subclasses map to these
		 * versions that invert senses for descending maps
		 */

		final PatriciaTrie.Entry<K, V> absLowest(boolean neverNull) {
			PatriciaTrie.Entry<K, V> e;
			if (prefixLength == loLength) {
				e = t.getLeastPrefixed(loKey, loInclusive);
				if (e == null && neverNull)
					return DUMMY;
			} else {
				e = t.tailOf(loKey, loInclusive ? S_INCLUSIVE : S_DEFAULT);
				if (e == null || !isPrefixed(e.key))
					return neverNull ? DUMMY : null;
			}
			return e;
		}

		final PatriciaTrie.Entry<K, V> absHighest(boolean neverNull) {
			PatriciaTrie.Entry<K, V> e;
			if (prefixLength == loLength) {
				e = t.getLastPrefixed(loKey, loInclusive);
				if (e == null && neverNull)
					return DUMMY;
			} else {
				e = t.getLastPrefixed(prefixKey, false);
				if (e == null || tooLow(e.key))
					return neverNull ? DUMMY : null;
			}
			return e;
		}

		final PatriciaTrie.Entry<K, V> absCeiling(K key, boolean neverNull) {
			if (tooLow(key))
				return absLowest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.tailOf(key, S_INCLUSIVE);
			if (e == null || tooHigh(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absHigher(K key, boolean neverNull) {
			if (tooLow(key))
				return absLowest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.tailOf(key, S_DEFAULT);
			if (e == null || tooHigh(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absFloor(K key, boolean neverNull) {
			if (tooHigh(key))
				return absHighest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.headOf(key, S_INCLUSIVE);
			if (e == null || tooLow(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		final PatriciaTrie.Entry<K, V> absLower(K key, boolean neverNull) {
			if (tooHigh(key))
				return absHighest(neverNull);
			PatriciaTrie.Entry<K, V> e = t.headOf(key, S_DEFAULT);
			if (e == null || tooLow(e.key))
				return neverNull ? DUMMY : null;
			return e;
		}

		/** Returns the absolute high fence for ascending traversal */
		final PatriciaTrie.Entry<K, V> absHighFence() {
			return t.tailOf(prefixKey, S_NEXT_PREFIX | S_NEVER_NULL);
		}

		/** Return the absolute low fence for descending traversal */
		final PatriciaTrie.Entry<K, V> absLowFence() {
			return t.headOf(loKey, loInclusive ? S_NEVER_NULL : S_INCLUSIVE
					| S_NEVER_NULL);
		}

		final PatriciaTrie.Entry<K, V> absPollLowest() {
			PatriciaTrie.Entry<K, V> e;
			if (prefixLength == loLength) {
				e = t.getLeastPrefixed(loKey, loInclusive);
				if (e == null)
					return null;
			} else {
				e = t.tailOf(loKey, loInclusive ? S_INCLUSIVE : S_DEFAULT);
				if (e == null || !isPrefixed(e.key))
					return null;
			}
			t.deleteEntry(e);
			return e;
		}

		final PatriciaTrie.Entry<K, V> absPollHighest() {
			PatriciaTrie.Entry<K, V> e;
			if (prefixLength == loLength) {
				e = t.getLastPrefixed(loKey, loInclusive);
				if (e == null)
					return null;
			} else {
				e = t.getLastPrefixed(prefixKey, false);
				if (e == null || tooLow(e.key))
					return null;
			}
			t.deleteEntry(e);
			return e;
		}

		/** Returns ascending iterator from the perspective of this submap */
		abstract Iterator<K> keyIterator();

		/** Returns descending iterator from the perspective of this submap */
		abstract Iterator<K> descKeyIterator();

		// Public methods

		public final boolean isEmpty() {
			return loInclusive && loLength == 0 ? t.isEmpty() : entrySet()
					.isEmpty();
		}

		public final int size() {
			return loInclusive && loLength == 0 ? t.size() : entrySet().size();
		}

		public final boolean containsKey(Object key) {
			return inRange(key) && t.containsKey(key);
		}

		public final V put(K key, V value) {
			if (!inRange(key))
				throw new IllegalArgumentException("key out of range");
			return t.put(key, value);
		}

		public final V get(Object key) {
			return !inRange(key) ? null : t.get(key);
		}

		public final V remove(Object key) {
			return !inRange(key) ? null : t.remove(key);
		}

		public final void clear() {
			PatriciaTrie.Entry<K, V> e = t.getLeastPrefixed(loKey, true);
			if (e != null && (e = e.switches) != null
					&& (e = lastLinked(e)).bit < 0) {
				t.modCount++;
				t.size -= destroyLinks(e);
			}
			// Handle the remaining entries
			if ((e = absLowest(false)) != null)
				t.rangeClear(e, absHighFence());
		}

		// Ascending BitwiseMap only methods, if loKey == prefixKey

		public final Map.Entry<K, V> nearestEntry(K key) {
			PatriciaTrie.Entry<K, V> e = t.nearestEntry(key, false);
			if (e == null)
				return null;
			if (!isPrefixedByLowKey(e.key))
				return t.getLeastPrefixed(loKey, loInclusive);
			return e;
		}

		public final K nearestKey(K key) {
			PatriciaTrie.Entry<K, V> e = t.nearestEntry(key, false);
			if (e == null)
				return null;
			if (!isPrefixedByLowKey(key = e.key))
				if ((e = t.getLeastPrefixed(loKey, loInclusive)) != null) {
					return e.key;
				} else
					return null;
			return key;
		}

		public final Map.Entry<K, V> prefixEntry(K key) {
			PatriciaTrie.Entry<K, V> e = t.prefixEntry(key, false);
			return e == null || !isPrefixedByLowKey(e.key) ? null : e;
		}

		public final K prefixKey(K key) {
			PatriciaTrie.Entry<K, V> e = t.prefixEntry(key, false);
			return e == null || !isPrefixedByLowKey(key = e.key) ? null : key;
		}

		public final Map.Entry<K, V> nextPrefixEntry(K key) {
			PatriciaTrie.Entry<K, V> e = t.tailOf(key, S_NEXT_PREFIX);
			if (e == null)
				return null;
			if (!isPrefixedByLowKey(key = e.key)) {
				if (tooLow(key))
					return t.getLeastPrefixed(loKey, loInclusive);
				return null;
			}
			return e;
		}

		public final K nextPrefixKey(K key) {
			PatriciaTrie.Entry<K, V> e = t.tailOf(key, S_NEXT_PREFIX);
			if (e == null)
				return null;
			if (!isPrefixedByLowKey(key = e.key)) {
				if (tooLow(key))
					if ((e = t.getLeastPrefixed(loKey, loInclusive)) != null)
						return e.key;
				return null;
			}
			return key;
		}

		public final Map.Entry<K, V> leastPrefixed(K key, boolean inclusive) {
			BitwiseComparator<? super Object> c = t.comparator;
			int i = c.contrast(key, loKey);
			if (i < 0)
				return t.getLeastPrefixed(key, inclusive && loInclusive);
			if (i == loLength)
				return t.getLeastPrefixed(key, inclusive); // normal operation
			if (i == c.lengthBits(key))
				return t.getLeastPrefixed(loKey, loInclusive); // absLowest
			return null;
		}

		public final Map.Entry<K, V> lastPrefixed(K key, boolean inclusive) {
			BitwiseComparator<? super Object> c = t.comparator;
			int i = c.contrast(key, loKey);
			if (i < 0)
				return t.getLastPrefixed(key, inclusive && loInclusive);
			if (i == loLength)
				return t.getLastPrefixed(key, inclusive); // normal operation
			if (i == c.lengthBits(key))
				return t.getLastPrefixed(loKey, loInclusive); // absHighest
			return null;
		}

		// Views
		transient NavigableMap<K, V> descendingMapView = null;
		transient EntrySetView entrySetView = null;
		transient KeySet<K> navigableKeySetView = null;

		public final NavigableSet<K> navigableKeySet() {
			KeySet<K> nksv = navigableKeySetView;
			return (nksv != null) ? nksv : (navigableKeySetView = new KeySet<>(
					this));
		}

		public final Set<K> keySet() {
			return navigableKeySet();
		}

		public final NavigableSet<K> descendingKeySet() {
			return descendingMap().navigableKeySet();
		}

		public final SortedMap<K, V> subMap(K fromKey, K toKey) {
			return subMap(fromKey, true, toKey, false);
		}

		public final SortedMap<K, V> headMap(K toKey) {
			return headMap(toKey, false);
		}

		public final SortedMap<K, V> tailMap(K fromKey) {
			return tailMap(fromKey, true);
		}

		public final Set<java.util.Map.Entry<K, V>> entrySet() {
			EntrySetView es = entrySetView;
			return (es != null) ? es : new EntrySetView();
		}

		abstract Iterator<Map.Entry<K, V>> entryIterator();

		private final class EntrySetView extends AbstractSet<Map.Entry<K, V>> {
			private transient int size, sizeModCount = -1;

			EntrySetView() {
			}

			public Iterator<Map.Entry<K, V>> iterator() {
				return entryIterator();
			}

			public int size() {
				if (sizeModCount < 0 || sizeModCount != t.modCount) {
					sizeModCount = t.modCount;
					int size = 0;
					for (Iterator<?> i = iterator(); i.hasNext(); i.next())
						size++;
					this.size = size;
				}
				return size;
			}

			public boolean isEmpty() {
				return absLowest(false) == null;
			}

			public boolean contains(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				PatriciaTrie.Entry<K, V> e;
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
				K key;

				if (!inRange((key = entry.getKey())))
					return false;
				return (e = t.getEntry(key)) != null
						&& eq(e.value, entry.getValue());
			}

			public boolean remove(Object o) {
				if (!(o instanceof Map.Entry))
					return false;
				PatriciaTrie.Entry<K, V> e;
				Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
				K key;

				if (!inRange((key = entry.getKey())))
					return false;
				if ((e = t.getEntry(key)) != null
						&& eq(e.value, entry.getValue())) {
					t.deleteEntry(e);
					return true;
				}
				return false;
			}
		}
	}

	/** @serial include */
	private static final class AscPrefixSubMap<K, V> extends PrefixSubMap<K, V>
			implements BitwiseMap<K, V> {
		private static final long serialVersionUID = -1044217809887122368L;

		// @formatter:off
		AscPrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, boolean inclusive) {
			super(t, prefixKey, inclusive);
		}
		AscPrefixSubMap(PatriciaTrie<K, V> t,
				K prefixKey, int prefixLength, K loKey, boolean loInclusive) {
			super(t, prefixKey, prefixLength, loKey, loInclusive);
		}
		AscPrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, int prefixLength,
				K loKey, int loLength, boolean loInclusive) {
			super(t, prefixKey, prefixLength, loKey, loLength, loInclusive);
		}

		public BitwiseComparator<? super K> comparator() { return t.comparator; }

		public NavigableMap<K, V> subMap(K fromKey,	boolean fromInclusive,
										 K toKey,	boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new AscSubMap<>(t,
								false, fromKey,	fromInclusive,
								false, toKey,	toInclusive);
		}
		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new AscSubMap<>(t,
								false,	loKey,	loInclusive,
								false,	toKey,	inclusive);
		}
		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new AscPrefixSubMap<>(t, prefixKey, prefixLength, fromKey, inclusive);
		}
		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new DescPrefixSubMap<>(t,
					prefixKey, prefixLength, loKey, loLength, loInclusive));
		}
		public BitwiseMap<K,V> subMap(K prefixKey, boolean inclusive) {
			if (!inRange(prefixKey, inclusive))
				throw new IllegalArgumentException("prefixKey out of range");
			return new AscPrefixSubMap<>(t, prefixKey, inclusive);
		}
		Iterator<Map.Entry<K, V>> entryIterator() {
			return new AscSubMapIterator<>(t, absLowest(true), absHighFence());
		}
		Iterator<K> keyIterator() {
			return new AscSubMapKeyIterator<>(t, absLowest(true), absHighFence());
		}
		Iterator<K> descKeyIterator() {
			return new DescSubMapKeyIterator<>(t, absHighest(true), absLowFence());
		}
		public Map.Entry<K, V> firstEntry()			{ return absLowest(false); }
		public Map.Entry<K, V> lastEntry()			{ return absHighest(false); }
		public K firstKey()							{ return absLowest(true).key; }
		public K lastKey()							{ return absHighest(true).key; }
		public Map.Entry<K, V> ceilingEntry(K key)	{ return absCeiling(key, false); }
		public K ceilingKey(K key)					{ return absCeiling(key, true).key; }
		public Map.Entry<K, V> higherEntry(K key)	{ return absHigher(key, false); }
		public K higherKey(K key)					{ return absHigher(key, true).key; }
		public Map.Entry<K, V> floorEntry(K key)	{ return absFloor(key, false); }
		public K floorKey(K key)					{ return absFloor(key, true).key; }
		public Map.Entry<K, V> lowerEntry(K key)	{ return absLower(key, false); }
		public K lowerKey(K key)					{ return absLower(key, true).key; }
		public Map.Entry<K, V> pollFirstEntry()		{ return absPollLowest(); }
		public Map.Entry<K, V> pollLastEntry()		{ return absPollHighest(); }
		// @formatter:on
	}

	/** @serial include */
	private static final class DescPrefixSubMap<K, V> extends
			PrefixSubMap<K, V> {
		private static final long serialVersionUID = 4569750630437153075L;

		// @formatter:off
		DescPrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, boolean inclusive) {
			super(t, prefixKey, inclusive);
		}
		DescPrefixSubMap(PatriciaTrie<K, V> t,
				K prefixKey, int prefixLength, K loKey, boolean loInclusive) {
			super(t, prefixKey, prefixLength, loKey, loInclusive);
		}
		DescPrefixSubMap(PatriciaTrie<K, V> t, K prefixKey, int prefixLength,
				K loKey, int loLength, boolean loInclusive) {
			super(t, prefixKey, prefixLength, loKey, loLength, loInclusive);
		}

		private final Comparator<? super K> reverseComparator = Collections
				.reverseOrder(t.comparator);

		public Comparator<? super K> comparator() { return reverseComparator; }

		public NavigableMap<K, V> subMap(K fromKey,	boolean fromInclusive,
										 K toKey,	boolean toInclusive) {
			if (!inRange(fromKey, fromInclusive))
				throw new IllegalArgumentException("fromKey out of range");
			if (!inRange(toKey, toInclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescSubMap<>(t,
									false, toKey, toInclusive,
									false, fromKey, fromInclusive);
		}
		public NavigableMap<K, V> headMap(K toKey, boolean inclusive) {
			if (!inRange(toKey, inclusive))
				throw new IllegalArgumentException("toKey out of range");
			return new DescPrefixSubMap<>(t, prefixKey, prefixLength, toKey, inclusive);
		}
		public NavigableMap<K, V> tailMap(K fromKey, boolean inclusive) {
			if (!inRange(fromKey, inclusive))
				throw new IllegalArgumentException("fromKey out of range");
			return new DescSubMap<>(t,
									false,	loKey,		loInclusive,
									false,	fromKey,	inclusive);
		}
		public NavigableMap<K, V> descendingMap() {
			NavigableMap<K, V> mv = descendingMapView;
			return (mv != null) ? mv : (descendingMapView = new AscPrefixSubMap<>(t,
					prefixKey, prefixLength, loKey, loLength, loInclusive));
		}
		Iterator<Map.Entry<K, V>> entryIterator() {
			if (loInclusive && loLength == 0)
				return new DescEntryIterator<>(t);
			return new DescSubMapIterator<>(t, absHighest(true), absLowFence());
		}
		Iterator<K> keyIterator() {
			return new DescSubMapKeyIterator<>(t, absHighest(true), absLowFence());
		}
		Iterator<K> descKeyIterator() {
			return new AscSubMapKeyIterator<>(t, absLowest(true), absHighFence());
		}
		public Map.Entry<K, V> firstEntry()			{ return absHighest(false); }
		public Map.Entry<K, V> lastEntry()			{ return absLowest(false); }
		public K firstKey()							{ return absHighest(true).key; }
		public K lastKey()							{ return absLowest(true).key; }
		public Map.Entry<K, V> ceilingEntry(K key)	{ return absFloor(key, false); }
		public K ceilingKey(K key)					{ return absFloor(key, true).key; }
		public Map.Entry<K, V> higherEntry(K key)	{ return absLower(key, false); }
		public K higherKey(K key)					{ return absLower(key, true).key; }
		public Map.Entry<K, V> floorEntry(K key)	{ return absCeiling(key, false); }
		public K floorKey(K key)					{ return absCeiling(key, true).key; }
		public Map.Entry<K, V> lowerEntry(K key)	{ return absHigher(key, false); }
		public K lowerKey(K key)					{ return absHigher(key, true).key; }
		public Map.Entry<K, V> pollFirstEntry()		{ return absPollHighest(); }
		public Map.Entry<K, V> pollLastEntry()		{ return absPollLowest(); }
		// @formatter:on
	}

	/** Base class for SubMap Iterators */
	private static abstract class PrivateSubMapIterator<T, K, V> implements
			Iterator<T> {
		private final PatriciaTrie<K, V> trie;
		private final Entry<K, V> fence;
		private Entry<K, V> next;
		private Entry<K, V> lastReturned;
		private int expectedModCount;

		PrivateSubMapIterator(PatriciaTrie<K, V> trie, Entry<K, V> first,
				Entry<K, V> fence) {
			this.expectedModCount = trie.modCount;
			this.trie = trie;
			this.next = first;
			this.fence = first.bit == ROOT_UNSET ? first : fence;
		}

		public final boolean hasNext() {
			return next != fence;
		}

		final Entry<K, V> nextEntry() {
			Entry<K, V> e = next;
			if (e == fence)
				throw new NoSuchElementException();
			if (trie.modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = successorOf(e);
			lastReturned = e;
			return e;
		}

		final Entry<K, V> prevEntry() {
			Entry<K, V> e = next;
			if (e == fence)
				throw new NoSuchElementException();
			if (trie.modCount != expectedModCount)
				throw new ConcurrentModificationException();
			next = predecessorOf(e);
			lastReturned = e;
			return e;
		}

		public final void remove() {
			Entry<K, V> p = lastReturned;
			if (p == null)
				throw new IllegalStateException();
			final PatriciaTrie<K, V> t = trie;
			if (t.modCount != expectedModCount)
				throw new ConcurrentModificationException();
			t.deleteEntry(p);
			expectedModCount = t.modCount;
			lastReturned = null;
		}
	}

	// @formatter:off
	private static final class AscSubMapIterator<K, V> extends
			PrivateSubMapIterator<Map.Entry<K, V>, K, V> {
		AscSubMapIterator(PatriciaTrie<K, V> t,
				Entry<K, V> first, Entry<K, V> fence) {
			super(t, first, fence);
		}
		public Map.Entry<K, V> next() { return super.nextEntry(); }
	}
	private static final class AscSubMapKeyIterator<K, V> extends
			PrivateSubMapIterator<K, K, V> {
		AscSubMapKeyIterator(PatriciaTrie<K, V> t,
				Entry<K, V> first, Entry<K, V> fence) {
			super(t, first, fence);
		}
		public K next() { return super.nextEntry().key; }
	}
	private static final class DescSubMapIterator<K, V> extends
			PrivateSubMapIterator<Map.Entry<K, V>, K, V> {
		DescSubMapIterator(PatriciaTrie<K, V> t,
				Entry<K, V> last, Entry<K, V> fence) {
			super(t, last, fence);
		}
		public Map.Entry<K, V> next() { return super.prevEntry(); }
	}
	private static final class DescSubMapKeyIterator<K, V> extends
			PrivateSubMapIterator<K, K, V> {
		DescSubMapKeyIterator(PatriciaTrie<K, V> t,
				Entry<K, V> last, Entry<K, V> fence) {
			super(t, last, fence);
		}
		public K next() { return super.nextEntry().key; }
	}
	// @formatter:on
}
