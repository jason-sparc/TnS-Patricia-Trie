package com.circlet.util;

import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

public class PatriciaTrieTestRunner {

	public static void main(String[] args) throws InterruptedException {
		PatriciaTrieTestRunner.class.getClassLoader()
				.setDefaultAssertionStatus(true);
		final boolean DEBUG = !true;
		if (DEBUG) {
			new BinaryStringTester().runTestLoop();
			return;
		}

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 10; j++) {
				new TestRunnerThread(new BinaryStringTester()).start();
			}
			ThreadLocalRandom rn = ThreadLocalRandom.current();
			Thread.sleep(100 + rn.nextInt(900));
		}
		for (int prev = -1;;) {
			int prog = ProgressStatus.CURRENT.getLeastProgress();
			if (prog != prev) {
				System.out.append("Least progressing test count: ").println(
						prev = prog);
			}
		}
	}

	static class ProgressStatus {
		static final ProgressStatus CURRENT = new ProgressStatus();

		static final Thread[] threads = new Thread[256];

		private ProgressStatus() {}

		final int getLeastProgress() {
			Thread.currentThread().getThreadGroup().enumerate(threads);
			int min = -1;
			for (Thread t : threads) {
				if (t instanceof TestRunnerThread) {
					int testCount = ((TestRunnerThread) t).test.testCount;
					if (testCount > min)
						min = testCount;
				}
			}
			return min;
		}

		@Override
		public String toString() {
			return "Least progressing test count: " + getLeastProgress();
		}
	}

	static final class TestRunnerThread extends Thread {
		final PatriciaTrieTest<?> test;

		TestRunnerThread(PatriciaTrieTest<?> test) {
			this.test = test;
		}

		public void run() {
			try {
				for (;;) {
					test.test();
					Thread.yield();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}

	static final class BinaryStringTester extends PatriciaTrieTest<String> {
		static final TreeMap<String, Integer> CONTENT;

		static {
			TreeMap<String, Integer> map = new TreeMap<>();
			final int spawn = 100;
			final int zeroPad = 5;

			map.put("", 0);
			for (int i = 0; i < spawn; i++) {
				StringBuilder sb = new StringBuilder(Integer.toBinaryString(i))
						.reverse();
				map.put(sb.toString(), 0);
				for (int j = 0; j < zeroPad; j++)
					map.put(sb.append('0').toString(), 0);
			}
			CONTENT = map;
		}

		public BinaryStringTester() {
			super(BinaryStringBitsComparator.INSTANCE);
		}

		@Override
		protected void fill(MockPrefixMap<String, Integer> map) {
			map.putAll(CONTENT);
		}
	}

	static final class ByteBitsTester extends PatriciaTrieTest<Byte> {
		static final TreeMap<Byte, Integer> CONTENT;

		static {
			TreeMap<Byte, Integer> map = new TreeMap<>();
			final int spawn = 256;

			for (int i = 0; i < spawn; i++) {
				map.put((byte) i, 0);
			}
			CONTENT = map;
		}

		public ByteBitsTester() {
			super(BitwiseComparators.FOR_BYTE);
		}

		@Override
		protected void fill(MockPrefixMap<Byte, Integer> map) {
			map.putAll(CONTENT);
		}
	}
}
