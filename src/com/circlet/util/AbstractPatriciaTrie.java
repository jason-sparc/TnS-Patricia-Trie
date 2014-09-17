package com.circlet.util;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;

public abstract class AbstractPatriciaTrie<K, V> extends
		AbstractNavigableMap<K, V> implements BitwiseMap<K, V>,
		java.io.Serializable {
	private static final long serialVersionUID = 1389871297282553574L;

	private final static boolean DEBUG = false;
	private final static boolean DEBUG_TREE = DEBUG;

	/**
	 * The bitwise comparator used to maintain order in this patricia trie.
	 * <p>
	 * Specific type was intentionally not specified to allow operations on
	 * varied types without excessive (fake) casting.
	 * 
	 * @serial include
	 */
	@SuppressWarnings("rawtypes")
	final BitwiseComparator comparator;

	transient Entry<K, V> root;

	/**
	 * The number of entries in the tree
	 */
	transient int size;

	/**
	 * The number of structural modifications to the tree.
	 */
	transient int modCount;

	AbstractPatriciaTrie(BitwiseComparator<? super K> comparator) {
		this.comparator = comparator;
		this.root = new Entry<K, V>();
	}

	@Override
	public BitwiseComparator<? super K> comparator() {
		@SuppressWarnings("unchecked")
		BitwiseComparator<? super K> c = comparator;
		return c;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		modCount++;
		size = 0;
		Entry<K, V> node = root;
		if (node.bit != ROOT_UNSET) {
			root = new Entry<K, V>();
		}
		// Clears all sub-entries to avoid garbage retention, since some entries
		// might still be referenced outside of the trie. This simply detaches
		// the nodes, the keys and values are unaffected.
		destroyLinks(node);
	}

	@Override
	public boolean containsValue(Object value) {
		Entry<K, V> next = root;
		if (next.bit == UNSET) {
			if ((next = next.switches) == null)
				return false; // empty
		}
		// XXX Would using an unordered traversal be more efficient?
		if (value == null) {
			do {
				if (next.value == null)
					return true;
				next = successorOf(next);
			} while (next.bit != UNSET);
		} else {
			do {
				if (value.equals(next.value))
					return true;
				next = successorOf(next);
			} while (next.bit != UNSET);
		}
		return false;
	}

	static <T, K, V> Entry<K, V> findNearest(BitwiseComparator<? super T> c,
			Entry<K, V> track, T key, int length) {
		Entry<K, V> next = track.switches;
		while (next != null) {
			int bit = next.bit;
			if (bit < 0) {
				int bitm = bit & BIT_INDEX_MASK;
				if (length <= bitm)
					break;
				if (c.isBitSet(key, bitm)) {
					if ((bit & BIT_ZERO_FLAG) != UNSET)
						break;
				} else if ((bit & BIT_ZERO_FLAG) == UNSET) {
					next = next.next;
					if (next == null) {
						break;
					}
				}
			} else {
				if (length <= bit)
					break;
				if (!c.isBitSet(key, bit)) {
					next = next.next;
					continue;
				}
			}
			next = (track = next).switches; // Change track
		}
		return track;
	}

	static <K, V> Entry<K, V> backwardsRetrace(Entry<K, V> track, int index) {
		if ((track.bit & BIT_INDEX_MASK) > index) {
			// Find correct track
			do {
				Entry<K, V> current = track;
				while ((track = current.prev).switches != current)
					current = track;
			} while ((track.bit & BIT_INDEX_MASK) >= index);
			if (DEBUG)
				assert (track.bit & BIT_INDEX_MASK) != index;
		}
		return track;
	}

	static <K, V> Entry<K, V> findInsertionNode(Entry<K, V> cur, int index) {
		Entry<K, V> ins = null;
		cur = cur.switches;
		while (cur != null && (cur.bit & BIT_INDEX_MASK) <= index) {
			cur = (ins = cur).next;
		}
		return ins;
	}

	static <K, V> Entry<K, V> findMarkedInsertionNode(Entry<K, V> cur, int index) {
		Entry<K, V> ins = null;
		cur = cur.switches;
		for (;;) {
			if (cur == null)
				break;
			int bit;
			if ((bit = cur.bit & BIT_INDEX_MASK) >= index) {
				if (bit == index) {
					cur.bit |= BIT_EDGE_FLAG; // Mark new edge switch
					return cur;
				}
				break;
			}
			cur = (ins = cur).next;
		}
		return ins;
	}

	@Override
	public V put(K key, V value) {
		@SuppressWarnings("unchecked")
		BitwiseComparator<? super K> c = comparator;
		int length = c.lengthBits(key); // Type (and possibly null) check

		Entry<K, V> track = findNearest(c, root, key, length);

		int index;
		int otherLength;

		if (track.bit == ROOT_UNSET) {
			// Handle special case
			if (length == 0) {
				track.bit = ROOT_SET;
				track.key = key;
				track.value = value;
				size++;
				modCount++;
				return null;
			}
			// Otherwise, jump after the other side...
			index = otherLength = 0;
		} else {
			K otherKey = track.key;
			index = c.contrast(key, otherKey);
			if (index < 0) {
				// Found exact match!
				V old = track.value;
				track.value = value;
				return old;
			}
			if (index == length) {
				// Query key is a prefix
				Entry<K, V> entry = new Entry<>(key, value);
				replace(track, entry);
				track.bit = c.isBitSet(otherKey, length) ? length
						| BIT_EDGE_FLAG : length | BIT_EDGE_ZERO_SPEC;
				Entry<K, V> prev = findMarkedInsertionNode(track, index);
				if (prev != null) {
					// Transfer covered switches
					(entry.switches = track.switches).prev = entry;
					Entry<K, V> next = prev.next;
					// Set 'track' as the new entry's next switch
					(prev.next = track).prev = prev;
					// Give 'track' a new set of switches
					if ((track.switches = next) != null)
						next.prev = track;
				} else {
					// Set 'track' as the new entry's next switch
					entry.switches = track;
					track.prev = entry;
				}
				size++;
				modCount++;
				return null;
			}
			// Otherwise, fall through...
			otherLength = c.lengthBits(otherKey);
		}

		Entry<K, V> entry = new Entry<>(key, value);
		if (index == otherLength) {
			// Existing key is a prefix
			int bit;
			Entry<K, V> prev = track.switches;
			if (prev == null) {
				if (DEBUG)
					assert track.switches == null;
				entry.bit = c.isBitSet(key, index) ? index | BIT_EDGE_FLAG
						: index | BIT_EDGE_ZERO_SPEC;
				(track.switches = entry).prev = track;
			} else if ((bit = (prev = lastLinked(prev)).bit) < 0) {
				// Mismatch at the edge
				if ((bit & BIT_ZERO_FLAG) != UNSET) {
					// Mismatch was caused by a 1-bit in the query key
					entry.bit = index | BIT_EDGE_FLAG;
					// Found 0-bit edge switch! Prepend to it.
					prepend(prev, entry);
				} else {
					// Mismatch was caused by a 0-bit in the query key
					entry.bit = index | BIT_EDGE_ZERO_SPEC;
					// Found 1-bit edge switch! Append at list end.
					(entry.prev = prev).next = entry;
				}
			} else {
				entry.bit = c.isBitSet(key, index) ? index | BIT_EDGE_FLAG
						: index | BIT_EDGE_ZERO_SPEC;
				// Simply append at the current location
				append(prev, entry);
			}
		} else {
			// The query key has a prefix that is a prefix of an existing key
			// (i.e. the 2 keys have intersecting start bits)
			track = backwardsRetrace(track, index);
			Entry<K, V> prev = findInsertionNode(track, index);

			if (c.isBitSet(key, index)) {
				entry.bit = index;
				// Simply insert to the list
				if (prev != null) {
					append(prev, entry);
				} else {
					enlist(track, entry);
				}
			} else {
				replace(track, entry);
				track.bit = index;
				if (prev != null) {
					// Transfer covered switches
					(entry.switches = track.switches).prev = entry;
					Entry<K, V> next = prev.next;
					// Set 'track' as the new entry's next switch
					(prev.next = track).prev = prev;
					// Give 'track' a new set of switches
					if ((track.switches = next) != null)
						next.prev = track;
				} else {
					// Set 'track' as the new entry's next switch
					entry.switches = track;
					track.prev = entry;
				}
			}
		}
		size++;
		modCount++;
		return null;
	}

	@SuppressWarnings("unchecked")
	final Entry<K, V> getEntry(Object key) {
		BitwiseComparator<? super Object> c = comparator;
		int length = c.lengthBits(key); // Type (and possibly null) check

		// Here, we extract the first roll from the loop, eliminated some
		// impassable operations, and then added some special checks.

		Entry<K, V> e = root;
		if (length == 0) {
			if (e.bit == ROOT_UNSET)
				return null;
		} else {
			if ((e = e.switches) == null)
				return null;
			if (c.isBitSet(key, 0)) {
				if ((e.bit & BIT_ZERO_FLAG) != UNSET)
					return null;
			} else if ((e.bit & BIT_ZERO_FLAG) == UNSET) {
				if ((e = e.next) == null)
					return null;
			}
			if (c.compare(key, (e = findNearest(c, e, key, length)).key) != 0)
				return null;
		}
		return e;
	}

	final Entry<K, V> nearestEntry(Object key, boolean neverNull) {
		@SuppressWarnings("unchecked")
		BitwiseComparator<? super Object> c = comparator;
		int length = c.lengthBits(key); // Type (and possibly null) check

		// Here, we extract the first roll from the loop, eliminated some
		// impassable operations, and then added some special checks.

		Entry<K, V> next;
		Entry<K, V> track = root;
		if (length == 0 || (next = track.switches) == null)
			return neverNull || track.bit != ROOT_UNSET ? track : null;
		// Does some corrections which cannot be done alone by findNearest()
		if (c.isBitSet(key, 0)) { // should throw exception if length < 0
			if ((next.bit & BIT_ZERO_FLAG) != UNSET)
				return track.bit == ROOT_UNSET ? next : track;
		} else if ((next.bit & BIT_ZERO_FLAG) == UNSET) {
			Entry<K, V> prev;
			if ((next = (prev = next).next) == null)
				return track.bit == ROOT_UNSET ? prev : track;
		}
		return findNearest(c, next, key, length);
	}

	final Entry<K, V> prefixEntry(K key, boolean neverNull) {
		@SuppressWarnings("unchecked")
		BitwiseComparator<? super K> c = comparator;
		int length = c.lengthBits(key); // Type (and possibly null) check

		Entry<K, V> track = findNearest(c, root, key, length);

		int index;
		if (track.bit == ROOT_UNSET) {
			return !neverNull ? null : track;
		} else {
			K otherKey = track.key;
			index = c.contrast(key, otherKey);
			if (index < 0 || index != length && index == c.lengthBits(otherKey))
				return track;
		}
		for (;;) {
			int bit = track.bit;
			Entry<K, V> current = track;
			while ((track = current.prev).switches != current)
				current = track;
			if (bit < 0 && (bit & BIT_INDEX_MASK) <= index) {
				if (DEBUG)
					assert (bit & BIT_INDEX_MASK) <= index;
				break;
			}
		}
		return neverNull || track.bit != ROOT_UNSET ? track : null;
	}

	// Lookup spec modifiers
	static final int S_NEVER_NULL = 0x80000000; // this is set if spec < 0
	static final int S_INCLUSIVE = 0x1;
	static final int S_NEXT_PREFIX = 0x2;
	static final int S_DEFAULT = 0;

	final Entry<K, V> headOf(K key, int spec) {
		@SuppressWarnings("unchecked")
		BitwiseComparator<? super K> c = comparator;
		int length = c.lengthBits(key); // Type (and possibly null) check

		Entry<K, V> track = findNearest(c, root, key, length);

		if (track.bit == ROOT_UNSET) {
			// Nothing is lower than root
			Entry<K, V> s;
			if (length != 0 && (s = track.switches) != null
					&& s.bit >= BIT_EDGE_ZERO_SPEC) {
				return apexOf(s);
			}
			return spec < 0 ? track : null;
		}

		K otherKey = track.key;
		int index = c.contrast(key, otherKey);

		if (index < 0) {
			// Found an exact match!
			if ((spec & S_INCLUSIVE) != UNSET)
				return track;
		} else if (index != length) {
			if (index == c.lengthBits(otherKey)) {
				// Existing key is a prefix
				Entry<K, V> prev = track.switches;
				if (prev != null
						&& ((prev = lastLinked(prev)).bit & BIT_ZERO_FLAG) != UNSET) {
					// Mismatch was caused by a 1-bit in the query key
					// Found 0-bit edge switch; query key > prev
					return apexOf(prev);
				}
				// Found 1-bit edge/middle switch; query key < prev
				return spec < 0 || track.bit != ROOT_UNSET ? track : null;
			} else {
				track = backwardsRetrace(track, index);
				Entry<K, V> next;
				if (c.isBitSet(key, index)) {
					// The query key is higher
					Entry<K, V> prev = findInsertionNode(track, index);
					if (prev != null) {
						next = prev.next;
					} else {
						next = track.switches;
					}
					if (next != null) {
						return apexOf(next);
					}
					return track;
				}
				// The query key is lower. Return predecessor entry.
			}
		}
		// The following is similar to predecessorOf(track), directly
		// returning values to avoid extra "unset root" checks.
		Entry<K, V> next = track.next;
		if (next != null) {
			return apexOf(next);
		}
		track = parentOf(track);
		return spec < 0 || track.bit != ROOT_UNSET ? track : null;
	}

	@SuppressWarnings("unchecked")
	final Entry<K, V> tailOf(K key, int spec) {
		BitwiseComparator<? super K> c = comparator;
		int length = c.lengthBits(key); // Type (and possibly null) check

		Entry<K, V> track = findNearest(c, root, key, length);

		if (track.bit == ROOT_UNSET) {
			Entry<K, V> prev;
			if ((prev = track.switches) != null) {
				if (length == 0) {
					if ((spec & S_NEXT_PREFIX) == UNSET)
						return nextOrNode(prev);
				} else if ((prev.bit & BIT_ZERO_FLAG) == UNSET)
					return prev;
			}
			if (spec < 0)
				return DUMMY; // Mimics the behavior of higherSuccessorOf
			return null;
		}

		int index = c.contrast(key, track.key);

		if (index < 0) {
			// Found an exact match!
			Entry<K, V> prev;
			if ((spec & S_INCLUSIVE) != UNSET) {
				return track;
			} else if ((prev = track.switches) != null) {
				if ((spec & S_NEXT_PREFIX) == UNSET) {
					return lastLinked(prev);
				} else if ((prev = lastLinked(prev)).bit >= 0
						|| (prev = prev.prev) != track
						&& (prev.bit >= 0 || (prev = prev.prev) != track))
					return prev;
			}
		} else if (index == length) {
			// Query key is a prefix
			if ((spec & S_NEXT_PREFIX) == UNSET) {
				return track;
			} else {
				// The following is a modified version of
				// findInsertionNode(track)
				Entry<K, V> prev = null;
				Entry<K, V> cur = track.switches;
				if (cur != null && (cur.bit & BIT_INDEX_MASK) < length) {
					do {
						cur = (prev = cur).next;
					} while (cur != null && (cur.bit & BIT_INDEX_MASK) < length);
					return prev;
				}
			}
		} else {
			Entry<K, V> prev;
			if (index == c.lengthBits(track.key)) {
				// Existing key is a prefix
				if ((prev = track.switches) != null
						&& (((prev = lastLinked(prev)).bit & BIT_ZERO_FLAG) == UNSET || (prev = prev.prev) != track))
					return prev;
			} else {
				track = backwardsRetrace(track, index);
				if (!c.isBitSet(key, index)) {
					// Query key < track.key
					if (DEBUG)
						assert track.bit != ROOT_UNSET;
					return track;
				}
				// Query key > track.key
				if ((prev = findInsertionNode(track, index)) != null)
					return prev;
			}
		}
		track = higherSuccessorOf(track);
		if (spec < 0 || track.bit != ROOT_UNSET)
			return track;
		return null;
	}

	@SuppressWarnings("unchecked")
	final Entry<K, V> getLeastPrefixed(K prefixKey, boolean inclusive) {
		BitwiseComparator<? super K> c = comparator;
		int length = c.lengthBits(prefixKey); // Type (and possibly null) check

		// Here, we extract the first roll from the loop, eliminated some
		// impassable operations, and then added some special checks.

		Entry<K, V> e = root;
		search: if (length == 0) {
			if (inclusive && e.bit != ROOT_UNSET)
				return e;
			if ((e = e.switches) != null)
				return nextOrNode(e);
		} else if ((e = e.switches) != null) {
			if (c.isBitSet(prefixKey, 0)) {
				if ((e.bit & BIT_ZERO_FLAG) != UNSET)
					break search;
			} else if ((e.bit & BIT_ZERO_FLAG) == UNSET) {
				if ((e = e.next) == null)
					break search;
			}
			int index = c.contrast(prefixKey,
					(e = findNearest(c, e, prefixKey, length)).key);
			if (index < 0) {
				if (inclusive || (e = e.switches) != null
						&& (e = lastLinked(e)).bit < 0)
					return e;
			} else if (index == length) {
				return e;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	final Entry<K, V> getLastPrefixed(K prefixKey, boolean inclusive) {
		BitwiseComparator<? super K> c = comparator;
		int length = c.lengthBits(prefixKey); // Type (and possibly null) check

		// Here, we extract the first roll from the loop, eliminated some
		// impassable operations, and then added some special checks.

		Entry<K, V> e = root;
		search: if (length == 0) {
			if (inclusive) {
				if (e.bit != ROOT_UNSET || (e = e.switches) != null)
					return apexOf(e);
			} else if ((e = e.switches) != null)
				return apexOf(e);
		} else if ((e = e.switches) != null) {
			if (c.isBitSet(prefixKey, 0)) {
				if ((e.bit & BIT_ZERO_FLAG) != UNSET)
					break search;
			} else if ((e.bit & BIT_ZERO_FLAG) == UNSET) {
				if ((e = e.next) == null)
					break search;
			}
			int index = c.contrast(prefixKey,
					(e = findNearest(c, e, prefixKey, length)).key);
			if (index < 0) {
				Entry<K, V> cur = e.switches;
				if (cur != null && (cur = lastLinked(cur)).bit < 0) {
					if (e != (e = cur.prev) && e.bit < 0)
						return apexOf(e);
					return apexOf(cur);
				}
				if (inclusive) {
					return e;
				} else
					break search;
			} else if (index == length) {
				// The following is a modified version of
				// findInsertionNode(track)
				Entry<K, V> cur = e.switches;
				if (cur == null) {
					return e;
				} else if ((cur.bit & BIT_INDEX_MASK) >= length)
					return apexOf(cur);
				for (;;) {
					cur = cur.next;
					if (cur == null) {
						return e;
					} else if ((cur.bit & BIT_INDEX_MASK) >= length)
						return apexOf(cur);
				}
			}
		}
		return null;
	}

	final Entry<K, V> getFirstEntry() {
		Entry<K, V> first = root;
		if (first.bit == UNSET) {
			first = first.switches;
			if (first != null)
				return nextOrNode(first);
		}
		return first; // either root or null
	}

	final Entry<K, V> getLastEntry() {
		Entry<K, V> last = apexOf(root);
		if (last.bit == UNSET) {
			return null;
		}
		return last;
	}

	@Override
	public K firstKey() {
		Entry<K, V> first = root;
		if (first.bit != UNSET) {
			return first.key;
		} else {
			first = first.switches;
			if (first != null) {
				return nextOrNode(first).key;
			} else
				return null;
		}
	}

	@Override
	public K lastKey() {
		return apexOf(root).key;
	}

	@Override
	public Map.Entry<K, V> pollFirstEntry() {
		Entry<K, V> first = root;
		if (first.bit == UNSET) {
			first = first.switches;
			if (first == null)
				return null;
			modCount++;
			size--;
			detach(first = nextOrNode(first));
		} else {
			modCount++;
			size--;
			root = new Entry<>(first.switches);
			first.switches = null;
		}
		return first;
	}

	@Override
	public Map.Entry<K, V> pollLastEntry() {
		Entry<K, V> last = apexOf(root);
		if (last == root) {
			if (last.bit == UNSET)
				return null;
			modCount++;
			size--;
			root = new Entry<>(last.switches);
			last.switches = null;
		} else {
			modCount++;
			size--;
			detach(last);
		}
		return last;
	}

	final void deleteEntry(Entry<K, V> e) {
		modCount++;
		size--;
		if (e == root) {
			root = new Entry<>(e.switches);
			e.switches = null;
		} else
			detach(e);
	}

	// Node navigation operations

	static <K, V> Entry<K, V> successorOf(Entry<K, V> node) {
		Entry<K, V> s = node.switches;
		if (s != null) {
			// same as lastLinked(s)
			while ((node = s.next) != null)
				s = node;
			return s;
		}
		// same as higherSuccessorOf(node)
		while ((s = node.prev).switches == node)
			node = s;
		return s;
	}

	/**
	 * Returns next successor entry whose key is not prefixed by the key of the
	 * specified entry.
	 * 
	 * @param node
	 * @return
	 */
	static <K, V> Entry<K, V> higherSuccessorOf(Entry<K, V> node) {
		Entry<K, V> s;
		while ((s = node.prev).switches == node)
			node = s;
		return s;
	}

	static <K, V> Entry<K, V> predecessorOf(Entry<K, V> node) {
		Entry<K, V> p = node.next;
		if (p != null) {
			// same as apexOf(p)
			while ((node = p.switches) != null)
				p = node;
			return p;
		}
		// same as parentOf(node)
		while ((p = node.prev).switches != node)
			node = p;
		return p;
	}

	static <K, V> Entry<K, V> parentOf(Entry<K, V> entry) {
		Entry<K, V> prev;
		while ((prev = entry.prev).switches != entry)
			entry = prev;
		return prev;
	}

	/**
	 * Returns the highest child entry of the specified entry. If no such entry
	 * is found, the entry itself is returned.
	 * 
	 * @param entry
	 * @return
	 */
	static <K, V> Entry<K, V> apexOf(Entry<K, V> entry) {
		for (;;) {
			Entry<K, V> next = entry.switches;
			if (next == null)
				break;
			entry = next;
		}
		return entry;
	}

	static <K, V> Entry<K, V> nextOrNode(Entry<K, V> node) {
		Entry<K, V> next = node.next;
		if (next != null)
			return next;
		return node;
	}

	static <K, V> Entry<K, V> lastLinked(Entry<K, V> node) {
		for (;;) {
			Entry<K, V> next = node.next;
			if (next == null)
				break;
			node = next;
		}
		return node;
	}

	/**
	 * Returns the highest child entry prefixed by the specified entry.
	 * 
	 * @param p
	 * @return
	 */
	static <K, V> Entry<K, V> lastPrefixed(Entry<K, V> p) {
		Entry<K, V> s = p.switches;
		if (s != null && (s = lastLinked(s)).bit < 0) {
			return apexOf(s);
		}
		return p;
	}

	// Node manipulation operations

	static <K, V> void enlist(Entry<K, V> dest, Entry<K, V> node) {
		Entry<K, V> next = dest.switches;
		(node.prev = dest).switches = node;
		if (next != null) {
			(node.next = next).prev = node;
		}
	}

	static <K, V> void append(Entry<K, V> dest, Entry<K, V> node) {
		Entry<K, V> next = dest.next;
		(node.prev = dest).next = node;
		if (next != null) {
			(node.next = next).prev = node;
		}
	}

	static <K, V> void prepend(Entry<K, V> dest, Entry<K, V> node) {
		Entry<K, V> prev = dest.prev;
		node.prev = prev;
		if (prev.switches == dest) {
			prev.switches = node;
		} else {
			prev.next = node;
		}
		(node.next = dest).prev = node;
	}

	static <K, V> void replace(Entry<K, V> node, Entry<K, V> repl) {
		Entry<K, V> prev = node.prev;
		repl.bit = node.bit;
		repl.prev = prev;
		if (prev.switches == node) {
			prev.switches = repl;
		} else {
			prev.next = repl;
		}
		Entry<K, V> next = node.next;
		if (next != null) {
			(repl.next = next).prev = repl;
			node.next = null; // null out
		}
	}

	static <K, V> void detach(Entry<K, V> entry) {
		Entry<K, V> next = entry.switches;
		if (next != null) {
			Entry<K, V> last = next;
			Entry<K, V> linked = last.next;
			if (linked != null) {
				// lastLinked(linked) inlined
				do {
					linked = (last = linked).next;
				} while (linked != null);
				Entry<K, V> prev = last.prev;
				linked = last.switches;
				prev.bit &= BIT_INDEX_MASK;
				(next.prev = last).switches = next;
				if ((prev.next = linked) != null)
					linked.prev = prev;
			}
			replace(entry, last);
		} else {
			next = entry.next;
			Entry<K, V> prev = entry.prev;
			if (prev.switches == entry) {
				prev.switches = next;
			} else {
				prev.next = next;
			}
			if (next != null) {
				next.prev = prev;
				entry.next = null; // null out
			}
		}
		// Null out links to avoid garbage retention in case this entry is
		// still referenced outside the trie.
		entry.prev = entry.switches = null;
	}

	static <K, V> int destroyLinks(Entry<K, V> node) {
		// The following uses an unordered traversal to null out all node links.

		Entry<K, V> link = node.switches;
		if (link == null)
			return 0;
		node.switches = null;
		(node = link).prev = null;

		outer: for (int i = 1;;) {
			link = node.next;
			if (link != null) {
				node.next = null;
				node = link;
				continue;
			}
			link = node.switches;
			if (link != null) {
				node.switches = null;
				node = link;
				continue;
			}
			for (;;) {
				link = node.prev;
				if (link == null)
					return i;
				node.prev = null;
				i++;
				node = link.switches;
				if (node != null) {
					link.switches = null;
					continue outer;
				}
				node = link;
			}
		}
	}

	// Node representation

	/**
	 * Utility method to test for equality, checking for nulls.
	 */
	static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	@SuppressWarnings("rawtypes")
	static final Entry DUMMY = new Entry<>("dummy");

	/** Common value for unset bit flags, that is, 0 */
	static final int UNSET = 0;

	static final int BIT_INDEX_MASK = 0x3fffffff;
	static final int BIT_EDGE_FLAG = 0x80000000; // set if bit < 0
	static final int BIT_ZERO_FLAG = 0x40000000;
	static final int BIT_EDGE_ZERO_SPEC = BIT_ZERO_FLAG | BIT_EDGE_FLAG;

	static final int ROOT_UNSET = UNSET;
	static final int ROOT_SET = BIT_ZERO_FLAG;

	// TODO Fully document algorithm

	/**
	 * <pre>
	 * --=
	 *   0
	 *   0------=1
	 *   1       0
	 *   0--=1   0-------=1
	 *   0   0   0        0----=1
	 *           +-=--=   0     0
	 *             1  0   +-=   0
	 *             0  1     0
	 *             0  0     1
	 * </pre>
	 * 
	 * Note that all middle switches are 1-bits (and appears only in the middle
	 * of the track). A 0-bit switch can only appear in the end of a switch list
	 * with an index set to the end of the track. Also, only 1 middle switch at
	 * a time can be assigned to an index, whereas the end of the track can have
	 * 2, a 1-bit and a 0-bit switch. The end switches are also referred to as
	 * edge switches in the source code. In comparison with a node in a basic
	 * patricia trie implementation, the 1-bit switch is the right child node
	 * and the 0-bit switch is the left.
	 */
	static final class Entry<K, V> implements Map.Entry<K, V>,
			java.io.Serializable {
		private static final long serialVersionUID = 5690857436435870662L;

		K key;
		V value;

		/**
		 * If a positive integer, points to the bit index in the key that is a 1
		 * bit. If negative, its value masked by {@link #BIT_INDEX_MASK}
		 * specifies the bit index in the key that is a 1 bit or a 0-bit if
		 * {@link #BIT_ZERO_FLAG} is present.
		 * <p>
		 * By default, this is {@link #UNSET} when the key and value has never
		 * been set.
		 */
		transient int bit;

		/**
		 * A linked list of nodes, representing alternative tracks.
		 */
		transient Entry<K, V> switches;

		/**
		 * The next node in the linked list of {@link #switches}.
		 */
		transient Entry<K, V> next;

		/**
		 * Either the parent node or a previous node in the {@link #switches}
		 * linked list.
		 */
		transient Entry<K, V> prev;

		Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}

		// Root entry constructors

		@SuppressWarnings("unchecked")
		Entry() {
			this.prev = DUMMY;
		}

		@SuppressWarnings("unchecked")
		Entry(Entry<K, V> switches) {
			this.prev = DUMMY;
			if (switches != null)
				(switches.prev = this).switches = switches;
		}

		// Dummy entry constructor

		Entry(V dummy) {
			this.value = dummy; // Dummy indicator
			this.prev = this;
			this.switches = this;
		}

		// Constructors used in deserialization

		Entry(int bit, Entry<K, V> prev) {
			this.bit = bit;
			this.prev = prev;
		}

		// Overridden/Implemented Methods

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			V old = this.value;
			this.value = value;
			return old;
		}

		/**
		 * Compares the specified object with this entry for equality. Returns
		 * {@code true} if the given object is also a map entry and the two
		 * entries represent the same mapping. More formally, two entries
		 * {@code e1} and {@code e2} represent the same mapping if
		 * 
		 * <pre>
		 * (e1.getKey() == null ? e2.getKey() == null : e1.getKey().equals(e2.getKey()))
		 * 		&amp;&amp; (e1.getValue() == null ? e2.getValue() == null : e1.getValue()
		 * 				.equals(e2.getValue()))
		 * </pre>
		 * 
		 * This ensures that the {@code equals} method works properly across
		 * different implementations of the {@code Map.Entry} interface.
		 * 
		 * @param o object to be compared for equality with this map entry
		 * @return {@code true} if the specified object is equal to this map
		 *         entry
		 * @see #hashCode
		 */
		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			@SuppressWarnings("rawtypes")
			Map.Entry e = (Map.Entry) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}

		/**
		 * Returns the hash code value for this map entry. The hash code of a
		 * map entry {@code e} is defined to be:
		 * 
		 * <pre>
		 * (e.getKey() == null ? 0 : e.getKey().hashCode())
		 * 		&circ; (e.getValue() == null ? 0 : e.getValue().hashCode())
		 * </pre>
		 * 
		 * This ensures that {@code e1.equals(e2)} implies that
		 * {@code e1.hashCode()==e2.hashCode()} for any two Entries {@code e1}
		 * and {@code e2}, as required by the general contract of
		 * {@link Object#hashCode}.
		 * 
		 * @return the hash code value for this map entry
		 * @see #equals
		 */
		public int hashCode() {
			return (key == null ? 0 : key.hashCode())
					^ (value == null ? 0 : value.hashCode());
		}

		/**
		 * Returns a String representation of this map entry. This
		 * implementation returns the string representation of this entry's key
		 * followed by the equals character ("<tt>=</tt>") followed by the
		 * string representation of this entry's value.
		 * 
		 * @return a String representation of this map entry
		 */
		public String toString() {
			return key + "=" + value;
		}

		// Methods used for debugging purposes.
		// TODO Document tree string format.

		final void toTreeString(StringBuilder out, int indent) {
			indent(out, indent);
			out.append('[').append(this).append(']');
			out.append('[').append(bit & BIT_INDEX_MASK).append(':');
			if (bit < 0) {
				out.append((bit & BIT_ZERO_FLAG) != UNSET ? 'Z' : '1');
				out.append(",E");
			} else {
				out.append("1");
			}
			out.append("] P:[");
			out.append(prev).append(']');
			Entry<K, V> e = switches;
			String ln = System.lineSeparator();
			if (e != null) {
				out.append(" {").append(ln);
				indent++;
				do {
					e.toTreeString(out, indent);
					out.append(',').append(ln);
					e = e.next;
				} while (e != null);
				indent(out, indent - 1);
				out.append('}');
			}
		}

		private static void indent(StringBuilder out, int indent) {
			for (int i = 0; i < indent; i++) {
				out.append("  ");
			}
		}
	}

	@Override
	public String toString() {
		if (DEBUG_TREE) {
			return toTreeString();
		}
		return super.toString();
	}

	/**
	 * Generates a hierarchical representation of the patricia trie. Used for
	 * debugging purposes only.
	 */
	public final String toTreeString() {
		StringBuilder out = new StringBuilder();
		root.toTreeString(out, 0);
		return out.toString();
	}

	// Serialization and Deserialization
	// TODO Document serialization process

	private static final byte HAS_NEXT = 0x1;
	private static final byte HAS_SWITCH = 0x2;

	private static final int IS_EMPTY = UNSET;

	private void writeObject(java.io.ObjectOutputStream s) throws IOException {
		// Write out the Comparator and any hidden stuff
		s.defaultWriteObject();

		Entry<K, V> node = this.root;
		if (node.bit == ROOT_UNSET && (node = node.switches) == null) {
			s.writeInt(IS_EMPTY);
			return;
		}

		// The serialization format for each entry is defined as follows:
		//
		// Entry
		// ::= ['bit' 'key' 'value' Links]
		//
		// Links
		// ::= ['HAS_NEXT' Next]
		// ::= ['HAS_SWITCH' Switches]
		// ::= ['HAS_NEXT|HAS_SWITCH' Next Switches]
		//
		// Next
		// ::= Entry
		//
		// Switches
		// ::= Entry

		// The following uses an unordered traversal similar to destoryLinks()

		outer: for (;;) {
			s.writeInt(node.bit);
			s.writeObject(node.key);
			s.writeObject(node.value);

			Entry<K, V> link = node.next;
			if (link != null) {
				s.writeByte(node.switches != null ? HAS_NEXT | HAS_SWITCH
						: HAS_NEXT);
				node = link;
				continue;
			}
			link = node.switches;
			if (link != null) {
				s.writeByte(HAS_SWITCH);
				node = link;
				continue;
			}
			s.writeByte(0);
			for (;;) {
				Entry<K, V> prev = node.prev;
				link = prev.switches;
				if (link != null && link != node) {
					if (link == prev)
						break outer; // Found DUMMY
					node = link;
					continue outer;
				}
				node = prev;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in the Comparator and any hidden stuff
		s.defaultReadObject();

		Entry<K, V> prev = root = new Entry<>();
		int i = s.readInt();
		if (i == IS_EMPTY)
			return;
		if (i < 0) {
			// Found first edge switch!
			prev = prev.switches = new Entry<>(i, prev);
		} else
			prev.bit = i; // if 'i' isn't ROOT_SET, the map is left broken

		for (i = 0;;) {
			prev.key = (K) s.readObject();
			prev.value = (V) s.readObject();
			i++;

			switch (s.read()) {
			case 0:
				// Search for placeholder
				do {
					prev = prev.prev;
				} while (prev.switches != prev);
				if (prev == DUMMY) {
					size = i;
					return; // Nothing found. We're done here!
				}
			case HAS_SWITCH:
				prev = prev.switches = new Entry<>(s.readInt(), prev);
				break;
			case HAS_NEXT | HAS_SWITCH:
				prev.switches = prev; // Set as placeholder
			case HAS_NEXT:
				prev = prev.next = new Entry<>(s.readInt(), prev);
				break;
			default:
				throw new java.io.StreamCorruptedException();
			}
		}
	}
}

abstract class AbstractNavigableMap<K, V> extends AbstractMap<K, V> implements
		NavigableMap<K, V> {

	abstract Iterator<K> keyIterator();

	abstract Iterator<K> descKeyIterator();
}
