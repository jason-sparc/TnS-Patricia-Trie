package com.circlet.util;

import static com.circlet.util.AbstractPatriciaTrie.BIT_INDEX_MASK;
import static com.circlet.util.AbstractPatriciaTrie.DUMMY;
import static com.circlet.util.AbstractPatriciaTrie.ROOT_UNSET;
import static com.circlet.util.AbstractPatriciaTrie.lastLinked;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

import com.circlet.util.AbstractPatriciaTrie.Entry;

@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class PatriciaTrieTest<K> {

	private final Random rn = new Random();
	private final BitwiseComparator<K> comparator;

	private ArrayList<Map.Entry<K, Integer>> ordered, shuffled, holed;
	private ArrayList<Map.Entry<K, Integer>> queryList, removeList;
	private MockPrefixMap<K, Integer> tree;
	private PatriciaTrie<K, Integer> trie;
	volatile int testCount;

	private static final TreeMap<String, Integer> EMPTY = new TreeMap<>();

	public PatriciaTrieTest(BitwiseComparator<K> comparator) {
		this.comparator = comparator;
	}

	protected abstract void fill(MockPrefixMap<K, Integer> map);

	private void newRandomContent() {
		fill(tree = new MockPrefixMap<>(comparator));

		ordered = new ArrayList<>(new LinkedHashSet<>(tree.entrySet()));
		queryList = new ArrayList<>();
		int size = ordered.size();
		{
			int holes = random((size >> 2) + (size >> 3), (size >> 1)
					+ (size >> 2) + (size >> 4));
			int queries = random((holes >> 2) + (holes >> 1), holes);
			int i = 0;
			for (; i < queries; i++) {
				Map.Entry<K, Integer> e = ordered.remove(rn.nextInt(size--));
				queryList.add(e);
			}
			for (; i < holes; i++)
				ordered.remove(rn.nextInt(size--));
		}
		tree.clear();
		for (int i = 0; i < size; i++) {
			Map.Entry<K, Integer> e = ordered.get(i);
			Integer value = i;
			e.setValue(value);
			tree.put(e.getKey(), value);
		}

		shuffled = (ArrayList<Map.Entry<K, Integer>>) ordered.clone();

		Collections.shuffle(shuffled, rn);
		Collections.shuffle(queryList, rn);

		removeList = new ArrayList<>();
		int sel = random((sel = rn.nextInt(size >> 1) + (size >> 2)) >> 2, sel);
		for (int i = 0; i < size; i++) {
			if (sel <= 0) break;
			if (rn.nextBoolean()) {
				removeList.add(shuffled.get(i));
				sel--;
			}
		}
		holed = (ArrayList<Map.Entry<K, Integer>>) ordered.clone();
		holed.removeAll(removeList);
		Collections.shuffle(shuffled, rn);
		Collections.shuffle(queryList, rn);
		Collections.shuffle(removeList, rn);

		testPut(trie = new PatriciaTrie<>(comparator), shuffled, false);
		assert trie.equals(tree);
		assert tree.equals(trie);
	}

	public final void test() {
		test(rn.rnd);
	}

	public final void test(long seed) {
		rn.rnd = seed;
		testCount++;
		try {
			newRandomContent();
			testOrder();
			testRemove();
			// We pass the random seed so that when debugging and dropping stack
			// frames, we would be able to restore the random generator state.
			testMap(trie, tree, 2, rn.rnd);
			testEdges();
			testOrder();
		} catch (Throwable t) {
			throw new TestFailed(seed, t);
		}
	}

	public final void runTestLoop() {
		for (;;)
			test(rn.rnd);
	}

	public final void runTestLoop(long seed) {
		for (;;)
			test(seed);
	}

	private <M extends NavigableMap<K, Integer>> void testMap(M testMap,
			M checkMap, int subMapTestDepth, long rnd) {
		rn.rnd = rnd; // restore random generator state

		if (checkMap instanceof PrefixMap) {
			PrefixMap<K, Integer> testPrefixMap = (PrefixMap<K, Integer>) testMap;
			PrefixMap<K, Integer> checkPrefixMap = (PrefixMap<K, Integer>) checkMap;
			testQueryMethods(testPrefixMap, checkPrefixMap, queryList);
			testQueryMethods(testPrefixMap, checkPrefixMap, shuffled);
		} else {
			testQueryMethods(testMap, checkMap, queryList);
			testQueryMethods(testMap, checkMap, shuffled);
		}
		testPoll(testMap, checkMap, rn.rnd);
		testEndPoints(testMap, checkMap);
		testDestroyLinks(testMap, checkMap);
		testSerialization(testMap);

		if (subMapTestDepth <= 0) return;

		subMapTestDepth--;
		NavigableMap<K, Integer> a, b;

		a = testMap.descendingMap();
		b = checkMap.descendingMap();
		testMap(a, b, subMapTestDepth, rn.rnd);

		Object[] choices = checkMap.keySet().toArray();
		int length = choices.length;
		if (length == 0) return;

		int from, to;
		K fromKey, toKey;
		boolean fromInclusive, toInclusive;

		from = rn.nextInt(length);
		fromKey = (K) choices[from];
		fromInclusive = rn.nextBoolean();

		a = testMap.tailMap(fromKey, fromInclusive);
		b = checkMap.tailMap(fromKey, fromInclusive);
		testMap(a, b, subMapTestDepth, rn.rnd);

		to = rn.nextInt(length);
		toKey = (K) choices[to];
		toInclusive = rn.nextBoolean();

		a = testMap.headMap(toKey, toInclusive);
		b = checkMap.headMap(toKey, toInclusive);
		testMap(a, b, subMapTestDepth, rn.rnd);

		from = rn.nextInt(length);
		to = rn.nextInt(length);

		if (from > to) {
			int i = from;
			from = i;
			to = from;
		}

		fromKey = (K) choices[from];
		fromInclusive = rn.nextBoolean();
		toKey = (K) choices[to];
		toInclusive = rn.nextBoolean();

		a = testMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
		b = checkMap.subMap(fromKey, fromInclusive, toKey, toInclusive);
		testMap(a, b, subMapTestDepth, rn.rnd);

		if (checkMap instanceof PrefixMap) {
			K prefixKey = (K) choices[rn.nextInt(length)];
			boolean prefixInclusive = rn.nextBoolean();

			PrefixMap<K, Integer> testPrefixMap = (PrefixMap<K, Integer>) testMap;
			PrefixMap<K, Integer> checkPrefixMap = (PrefixMap<K, Integer>) checkMap;
			a = testPrefixMap.subMap(prefixKey, prefixInclusive);
			b = checkPrefixMap.subMap(prefixKey, prefixInclusive);
			testMap(a, b, subMapTestDepth, rn.rnd);
		}

		assert testMap.equals(checkMap);
	}

	static <K> void testPut(Map<K, Integer> testMap,
			ArrayList<Map.Entry<K, Integer>> putList, boolean clearBeforeInsert) {
		if (clearBeforeInsert) testMap.clear();
		for (Map.Entry<K, Integer> e : putList) {
			testMap.put(e.getKey(), e.getValue());
		}
		assert testMap.entrySet().containsAll(putList);
	}

	static <K> void testPutAll(Map<K, Integer> testMap,
			Map<K, Integer> checkMap, boolean clearBeforeInsert) {
		if (clearBeforeInsert) testMap.clear();
		testMap.putAll(checkMap);
		assert testMap.equals(checkMap);
		assert checkMap.equals(testMap);
	}

	static <K> void testOrder(SortedMap<K, Integer> testMap,
			ArrayList<Map.Entry<K, Integer>> checkList) {
		int i = 0;
		for (Map.Entry<K, Integer> a : testMap.entrySet()) {
			Map.Entry<K, Integer> b = checkList.get(i++);
			assert eq(a, b) : mismatchMsg(a, b);
		}
	}

	static <K> void testReverseOrder(NavigableMap<K, Integer> testMap,
			ArrayList<Map.Entry<K, Integer>> checkList) {
		int i = checkList.size();
		for (Map.Entry<K, Integer> a : testMap.descendingMap().entrySet()) {
			Map.Entry<K, Integer> b = checkList.get(--i);
			assert eq(a, b) : mismatchMsg(a, b);
		}
	}

	static <K> void testEndPoints(NavigableMap<K, Integer> testMap,
			NavigableMap<K, Integer> checkMap) {
		assert eq(testMap.firstEntry(), checkMap.firstEntry());
		assert eq(testMap.lastEntry(), checkMap.lastEntry());
	}

	static <K> void testQueryMethods(NavigableMap<K, Integer> testMap,
			NavigableMap<K, Integer> checkMap,
			Collection<Map.Entry<K, Integer>> testList) {
		for (Map.Entry<K, Integer> e : testList) {
			for (QueryDelegate method : QUERY_METHODS)
				testQuery(method, testMap, checkMap, e);
		}
	}

	static <K> void testQueryMethods(PrefixMap<K, Integer> testMap,
			PrefixMap<K, Integer> checkMap,
			Collection<Map.Entry<K, Integer>> testList) {
		for (Map.Entry<K, Integer> e : testList) {
			for (QueryDelegate method : QUERY_METHODS)
				testQuery(method, testMap, checkMap, e);
			for (PrefixMapQueryDelegate method : PM_QUERY_METHODS)
				testQuery(method, testMap, checkMap, e);
		}
	}

	static <K> void testQuery(QueryDelegate method,
			NavigableMap<K, Integer> testMap,
			NavigableMap<K, Integer> checkMap, Map.Entry<K, Integer> e) {
		K key = e.getKey();
		Object a = method.query(testMap, key);
		Object b = method.query(checkMap, key);
		assert eq(a, b) : mismatchMsg(method, key, a, b);
	}

	static <K> void testQuery(PrefixMapQueryDelegate method,
			PrefixMap<K, Integer> testMap, PrefixMap<K, Integer> checkMap,
			Map.Entry<K, Integer> e) {
		K key = e.getKey();
		Object a = method.query(testMap, key);
		Object b = method.query(checkMap, key);
		assert eq(a, b) : mismatchMsg(method, key, a, b);
	}

	private void testPoll(NavigableMap<K, Integer> testMap,
			NavigableMap<K, Integer> checkMap, long seed) {
		rn.rnd = seed;
		TreeMap<K, Integer> check = new TreeMap<>(checkMap);
		Map.Entry<K, Integer> a, b;
		do {
			if (rn.nextBoolean()) {
				a = testMap.pollFirstEntry();
				b = check.pollFirstEntry();
			} else {
				a = testMap.pollLastEntry();
				b = check.pollLastEntry();
			}
			assert eq(a, b) : mismatchMsg(a, b);
		} while (a != null);
		testPutAll(testMap, checkMap, true); // restore
	}

	static <K> void testDestroyLinks(Map<K, Integer> testMap,
			Map<K, Integer> checkMap) {
		ArrayList<Map.Entry<K, Integer>> a = new ArrayList<>(testMap.entrySet());
		testMap.clear();
		assert testMap.equals(EMPTY);
		for (Map.Entry<K, Integer> r : a) {
			Entry<K, Integer> e = (Entry<K, Integer>) r;
			assert e.prev == null || e.prev == DUMMY;
			assert e.switches == null;
			assert e.next == null;
		}
		testPutAll(testMap, checkMap, false); // restore
	}

	static <K> void testSerialization(Map<K, Integer> testMap) {
		try {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bytes);
			out.writeObject(testMap);
			out.close();

			ObjectInputStream in = new ObjectInputStream(
					new ByteArrayInputStream(bytes.toByteArray()));
			Map<K, Integer> newMap = (Map<K, Integer>) in.readObject();
			assert testMap.equals(newMap);
			assert newMap.equals(testMap);
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void testOrder() {
		testOrder(trie, ordered);
		testReverseOrder(trie, ordered);
	}

	private void testEdges() {
		BitwiseComparator<? super K> c = trie.comparator();
		Entry<K, Integer> node = trie.root;

		if (node.bit == ROOT_UNSET && (node = node.switches) == null) {
			return;
		}

		// The following uses an unordered traversal similar to destoryLinks()

		outer: for (;;) {
			Entry<K, Integer> link = node.switches;
			if (link != null) {
				link = lastLinked(node.switches);
				if ((link.bit & BIT_INDEX_MASK) == c.lengthBits(node.key)) {
					assert link.bit < 0 : causeMsg(link);
					Entry<K, Integer> prev = link.prev;
					if (prev != link
							&& (prev.bit & BIT_INDEX_MASK) == c
									.lengthBits(node.key))
						assert prev.bit < 0 || prev == trie.root : causeMsg(prev);
				}
			}
			link = node.next;
			if (link != null) {
				node = link;
				continue;
			}
			link = node.switches;
			if (link != null) {
				node = link;
				continue;
			}
			for (;;) {
				Entry<K, Integer> prev = node.prev;
				link = prev.switches;
				if (link != null && link != node) {
					if (link == prev) break outer; // Found DUMMY
					node = link;
					continue outer;
				}
				node = prev;
			}
		}
	}

	private void testRemove() {
		PrefixMap<K, Integer> testMap = trie;
		for (Map.Entry<K, Integer> e : removeList) {
			K key = e.getKey();
			Integer value = testMap.remove(key);
			assert value != null : causeMsg(e);
			assert !testMap.containsKey(key) : causeMsg(e);
		}
		testEdges();
		testOrder(testMap, holed);
		testReverseOrder(testMap, holed);
		testPut(testMap, removeList, false); // restore
		testOrder(testMap, ordered);
		testReverseOrder(testMap, ordered);
	}

	private int random(int min, int max) {
		return min + rn.nextInt(max - min + 1);
	}

	protected java.util.Random random() {
		return rn;
	}

	static <K> String causeMsg(Object o) {
		return "caused by: " + o;
	}

	static <K> String mismatchMsg(Object a, Object b) {
		return "mismatch between \"" + a + "\" and \"" + b + "\"";
	}

	static <K> String mismatchMsg(K key, Object a, Object b) {
		return "mismatch between \"" + a + "\" and \"" + b
				+ "\" caused by key \"" + key + "\"";
	}

	static <K> String mismatchMsg(Named method, K key, Object a, Object b) {
		return "mismatch between \"" + a + "\" and \"" + b
				+ "\" caused by key \"" + key + "\" via \"" + method + "\"";
	}

	/**
	 * Utility method to test for equality, checking for nulls.
	 */
	static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private static final ArrayList<QueryDelegate> QUERY_METHODS;
	private static final ArrayList<PrefixMapQueryDelegate> PM_QUERY_METHODS;

	private static abstract class Named {
		private final String name;

		public Named(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}

	private static abstract class QueryDelegate extends Named {
		public QueryDelegate(String name) {
			super(name);
		}

		public abstract <K> Object query(NavigableMap<K, Integer> m, K k);
	}

	private static abstract class PrefixMapQueryDelegate extends QueryDelegate {
		public PrefixMapQueryDelegate(String name) {
			super(name);
		}

		public <K> Object query(NavigableMap<K, Integer> m, K k) {
			return query((PrefixMap<K, Integer>) m, k);
		}

		public abstract <K> Object query(PrefixMap<K, Integer> m, K k);
	}

	static {
		ArrayList methods = QUERY_METHODS = new ArrayList<>();
		// Basic Map Methods
		methods.add(new QueryDelegate("get(k)") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.get(k);
			};
		});
		methods.add(new QueryDelegate("containsKey(k)") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.containsKey(k);
			};
		});
		// NavigableMap Methods
		methods.add(new QueryDelegate("lowerEntry") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.lowerEntry(k);
			};
		});
		methods.add(new QueryDelegate("lowerKey") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.lowerKey(k);
			}
		});
		methods.add(new QueryDelegate("floorEntry") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.floorEntry(k);
			};
		});
		methods.add(new QueryDelegate("floorKey") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.floorKey(k);
			}
		});
		methods.add(new QueryDelegate("ceilingEntry") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.ceilingEntry(k);
			};
		});
		methods.add(new QueryDelegate("ceilingKey") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.ceilingKey(k);
			}
		});
		methods.add(new QueryDelegate("higherEntry") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.higherEntry(k);
			};
		});
		methods.add(new QueryDelegate("higherKey") {
			public <K> Object query(NavigableMap<K, Integer> m, K k) {
				return m.higherKey(k);
			}
		});
		// PrefixMap Methods
		methods = PM_QUERY_METHODS = new ArrayList<>();
		methods.add(new PrefixMapQueryDelegate("prefixEntry") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.prefixEntry(k);
			};
		});
		methods.add(new PrefixMapQueryDelegate("prefixKey") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.prefixKey(k);
			}
		});
		methods.add(new PrefixMapQueryDelegate("nextPrefixEntry") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.nextPrefixEntry(k);
			};
		});
		methods.add(new PrefixMapQueryDelegate("nextPrefixKey") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.nextPrefixKey(k);
			}
		});
		methods.add(new PrefixMapQueryDelegate("leastPrefixed(k, true)") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.leastPrefixed(k, true);
			};
		});
		methods.add(new PrefixMapQueryDelegate("leastPrefixed(k, false)") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.leastPrefixed(k, false);
			};
		});
		methods.add(new PrefixMapQueryDelegate("lastPrefixed(k, true)") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.lastPrefixed(k, true);
			};
		});
		methods.add(new PrefixMapQueryDelegate("lastPrefixed(k, false)") {
			public <K> Object query(PrefixMap<K, Integer> m, K k) {
				return m.lastPrefixed(k, false);
			};
		});
	}

	@SuppressWarnings("serial")
	public static class TestFailed extends RuntimeException {
		TestFailed(long seed, Throwable cause) {
			super("Test failed with random seed: 0x"
					+ Long.toHexString(seed).toUpperCase() + "L", cause);
		}
	}

	@SuppressWarnings("serial")
	private static final class Random extends java.util.Random {
		// same constants as Random, but must be redeclared because private
		private static final long multiplier = 0x5DEECE66DL;
		private static final long addend = 0xBL;
		private static final long mask = (1L << 48) - 1;

		/**
		 * The random seed. We can't use super.seed.
		 */
		long rnd;

		Random() {}

		public void setSeed(long seed) {
			rnd = (seed ^ multiplier) & mask;
		}

		@Override
		protected int next(int bits) {
			rnd = (rnd * multiplier + addend) & mask;
			return (int) (rnd >>> (48 - bits));
		}
	}

	static {
		if (!PatriciaTrieTest.class.desiredAssertionStatus())
			throw new RuntimeException("Assertions must be enabled.");
	}
}
