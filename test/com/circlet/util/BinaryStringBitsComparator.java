package com.circlet.util;

import com.circlet.util.BitwiseComparator;

public class BinaryStringBitsComparator implements BitwiseComparator<String>,
		java.io.Serializable {
	private static final long serialVersionUID = -6798584204646507518L;

	public static final BinaryStringBitsComparator INSTANCE = new BinaryStringBitsComparator();

	public BinaryStringBitsComparator() {
	}

	@Override
	public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	}

	@Override
	public int lengthBits(String o) {
		return o.length();
	}

	@Override
	public boolean isBitSet(String o, int index) {
		return o.charAt(index) == '1';
	}

	@Override
	public int contrast(String o1, String o2) {
		int len1 = o1.length();
		int len2 = o2.length();
		int lim = Math.min(len1, len2);

		for (int i = 0; i < lim; i++) {
			char b1 = o1.charAt(i);
			char b2 = o2.charAt(i);
			if (b1 != b2) {
				return i;
			}
		}

		if (len1 == len2) {
			return -1;
		}
		return lim;
	}

	@Override
	public boolean checkPrefixed(String o, String prefix, boolean inclusive) {
		return o.startsWith(prefix)
				&& (inclusive || o.length() != prefix.length());
	}
}
