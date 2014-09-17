package com.circlet.util;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Float.floatToIntBits;

public class BitwiseComparators {

	public static final CharBits FOR_CHAR = CharBits.INSTANCE;
	public static final ByteBits FOR_BYTE = ByteBits.INSTANCE;
	public static final ShortBits FOR_SHORT = ShortBits.INSTANCE;
	public static final IntBits FOR_INT = IntBits.INSTANCE;
	public static final LongBits FOR_LONG = LongBits.INSTANCE;
	public static final FloatBits FOR_FLOAT = FloatBits.INSTANCE;
	public static final DoubleBits FOR_DOUBLE = DoubleBits.INSTANCE;

	public static final Chars FOR_CHAR_ARRAY = Chars.INSTANCE;
	public static final Bytes FOR_BYTE_ARRAY = Bytes.INSTANCE;
	public static final Shorts FOR_SHORT_ARRAY = Shorts.INSTANCE;
	public static final Ints FOR_INT_ARRAY = Ints.INSTANCE;
	public static final Longs FOR_LONG_ARRAY = Longs.INSTANCE;
	public static final Floats FOR_FLOAT_ARRAY = Floats.INSTANCE;
	public static final Doubles FOR_DOUBLE_ARRAY = Doubles.INSTANCE;

	public static final UBytes FOR_UBYTE_ARRAY = UBytes.INSTANCE;
	public static final Booleans FOR_BOOLEAN_ARRAY = Booleans.INSTANCE;
	public static final StringBits FOR_STRING = StringBits.INSTANCE;

	// Basic implementations

	private interface BaseInterface<T> extends BitwiseComparator<T>,
			java.io.Serializable {
	}

	public static final class CharBits implements BaseInterface<Character> {
		private static final long serialVersionUID = -8457058687755395859L;
		public static final CharBits INSTANCE = new CharBits();

		private CharBits() {}

		public static final int SIZE_SHIFT = 4;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(char v, int index) {
			return v & MASK_BIT >>> index;
		}

		public static int indexOfBit(int i) {
			if (i == 0) return -1;
			// @formatter:off
			int n = 1;
			if (i >>> 8 == 0)  { n += 8; i <<= 8; }
			if (i >>> 12 == 0) { n += 4; i <<= 4; }
			if (i >>> 14 == 0) { n += 2; i <<= 2; }
			n -= i >>> 15;
			// @formatter:on
			return n;
		}

		@Override
		public int lengthBits(Character o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Character o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Character o1, Character o2) {
			return indexOfBit(o1 ^ o2);
		}

		@Override
		public int compare(Character o1, Character o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Character o, Character prefix,
				boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class ByteBits implements BaseInterface<Byte> {
		private static final long serialVersionUID = 2312954554533904716L;
		public static final ByteBits INSTANCE = new ByteBits();

		private ByteBits() {}

		public static final int SIZE_SHIFT = 3;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(byte v, int index) {
			return (v ^ MASK_BIT) & MASK_BIT >>> index;
		}

		public static int indexOfBit(int i) {
			if (i == 0) return -1;
			// @formatter:off
			int n = 1;
			if (i >>> 4 == 0) { n += 4; i <<= 4; }
			if (i >>> 6 == 0) { n += 2; i <<= 2; }
			n -= (i & 0xff) >>> 7;
			// @formatter:on
			return n;
		}

		@Override
		public int lengthBits(Byte o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Byte o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Byte o1, Byte o2) {
			return indexOfBit(o1 ^ o2);
		}

		@Override
		public int compare(Byte o1, Byte o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Byte o, Byte prefix, boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class ShortBits implements BaseInterface<Short> {
		private static final long serialVersionUID = -3645352988764048623L;
		public static final ShortBits INSTANCE = new ShortBits();

		private ShortBits() {}

		public static final int SIZE_SHIFT = CharBits.SIZE_SHIFT;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(short v, int index) {
			return (v ^ MASK_BIT) & MASK_BIT >>> index;
		}

		public static int indexOfBit(int i) {
			return CharBits.indexOfBit((char) i);
		}

		@Override
		public int lengthBits(Short o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Short o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Short o1, Short o2) {
			return CharBits.indexOfBit((char) (o1 ^ o2));
		}

		@Override
		public int compare(Short o1, Short o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Short o, Short prefix, boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class IntBits implements BaseInterface<Integer> {
		private static final long serialVersionUID = -8597496351962025337L;
		public static final IntBits INSTANCE = new IntBits();

		private IntBits() {}

		public static final int SIZE_SHIFT = 5;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(int v, int index) {
			return (v ^ MASK_BIT) & MASK_BIT >>> index;
		}

		public static int indexOfBit(int i) {
			if (i == 0) return -1;
			return Integer.numberOfLeadingZeros(i);
		}

		@Override
		public int lengthBits(Integer o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Integer o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Integer o1, Integer o2) {
			return indexOfBit(o1 ^ o2);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Integer o, Integer prefix,
				boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class LongBits implements BaseInterface<Long> {
		private static final long serialVersionUID = 8475874472776142489L;
		public static final LongBits INSTANCE = new LongBits();

		private LongBits() {}

		public static final int SIZE_SHIFT = 6;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final long MASK_BIT = 1L << MASK_INDEX;

		public static long bit(long v, int index) {
			return (v ^ MASK_BIT) & MASK_BIT >>> index;
		}

		public static int indexOfBit(long i) {
			if (i == 0) return -1;
			return Long.numberOfLeadingZeros(i);
		}

		@Override
		public int lengthBits(Long o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Long o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Long o1, Long o2) {
			return indexOfBit(o1 ^ o2);
		}

		@Override
		public int compare(Long o1, Long o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Long o, Long prefix, boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class FloatBits implements BaseInterface<Float> {
		private static final long serialVersionUID = -3583879160053872310L;
		public static final FloatBits INSTANCE = new FloatBits();

		private FloatBits() {}

		public static final int SIZE_SHIFT = 5;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(float v, int index) {
			return (floatToIntBits(v) ^ MASK_BIT) & MASK_BIT >>> index;
		}

		public static int indexOfXOR(float x, float y) {
			return IntBits.indexOfBit(floatToIntBits(x) ^ floatToIntBits(y));
		}

		@Override
		public int lengthBits(Float o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Float o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Float o1, Float o2) {
			return indexOfXOR(o1, o2);
		}

		@Override
		public int compare(Float o1, Float o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Float o, Float prefix, boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class DoubleBits implements BaseInterface<Double> {
		private static final long serialVersionUID = -1486226681979429470L;
		public static final DoubleBits INSTANCE = new DoubleBits();

		private DoubleBits() {}

		public static final int SIZE_SHIFT = 6;
		public static final int SIZE_IN_BITS = 1 << SIZE_SHIFT;
		public static final int MASK_INDEX = SIZE_IN_BITS - 1;
		public static final long MASK_BIT = 1L << MASK_INDEX;

		public static long bit(double v, int index) {
			return (doubleToLongBits(v) ^ MASK_BIT) & MASK_BIT >>> index;
		}

		public static int indexOfXOR(double x, double y) {
			return LongBits.indexOfBit(doubleToLongBits(x)
					^ doubleToLongBits(y));
		}

		@Override
		public int lengthBits(Double o) {
			return SIZE_IN_BITS;
		}

		@Override
		public boolean isBitSet(Double o, int index) {
			return bit(o, index) != 0;
		}

		@Override
		public int contrast(Double o1, Double o2) {
			return indexOfXOR(o1, o2);
		}

		@Override
		public int compare(Double o1, Double o2) {
			return o1.compareTo(o2);
		}

		@Override
		public boolean checkPrefixed(Double o, Double prefix, boolean inclusive) {
			return inclusive && o.equals(prefix);
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	// Array counterparts

	public static class Chars implements BaseInterface<char[]> {
		private static final long serialVersionUID = 3067778829126799764L;
		public static final Chars INSTANCE = new Chars();

		private Chars() {}

		public static final int SIZE_SHIFT = CharBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(char[] a, int index) {
			try {
				return a[index >> SIZE_SHIFT]
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(char[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(char[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(char[] a1, char[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				char b1 = a1[i];
				char b2 = a2[i];
				if (b1 != b2) {
					return (i << SIZE_SHIFT) + CharBits.indexOfBit(b1 ^ b2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(char[] a1, char[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				char b1 = a1[i];
				char b2 = a2[i];
				if (b1 != b2) {
					return b1 - b2;
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(char[] a, char[] prefix, boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				char b1 = a[i];
				char b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static class Bytes implements BaseInterface<byte[]> {
		private static final long serialVersionUID = -4241952380244353573L;
		public static final Bytes INSTANCE = new Bytes();

		private Bytes() {}

		public static final int SIZE_SHIFT = ByteBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(byte[] a, int index) {
			try {
				return (a[index >> SIZE_SHIFT] ^ MASK_BIT)
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(byte[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(byte[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(byte[] a1, byte[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				byte b1 = a1[i];
				byte b2 = a2[i];
				if (b1 != b2) {
					return (i << SIZE_SHIFT) + ByteBits.indexOfBit(b1 ^ b2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(byte[] a1, byte[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				byte b1 = a1[i];
				byte b2 = a2[i];
				if (b1 != b2) {
					return b1 - b2;
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(byte[] a, byte[] prefix, boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				byte b1 = a[i];
				byte b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static class Shorts implements BaseInterface<short[]> {
		private static final long serialVersionUID = -3220651844930229258L;
		public static final Shorts INSTANCE = new Shorts();

		private Shorts() {}

		public static final int SIZE_SHIFT = ShortBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(short[] a, int index) {
			try {
				return (a[index >> SIZE_SHIFT] ^ MASK_BIT)
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(short[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(short[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(short[] a1, short[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				int b1 = a1[i];
				int b2 = a2[i];
				if (b1 != b2) {
					return (i << SIZE_SHIFT) + ShortBits.indexOfBit(b1 ^ b2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(short[] a1, short[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				short b1 = a1[i];
				short b2 = a2[i];
				if (b1 != b2) {
					return b1 - b2;
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(short[] a, short[] prefix,
				boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				int b1 = a[i];
				int b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static class Ints implements BaseInterface<int[]> {
		private static final long serialVersionUID = -4161355469666934749L;
		public static final Ints INSTANCE = new Ints();

		private Ints() {}

		public static final int SIZE_SHIFT = IntBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(int[] a, int index) {
			try {
				return (a[index >> SIZE_SHIFT] ^ MASK_BIT)
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(int[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(int[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(int[] a1, int[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				int b1 = a1[i];
				int b2 = a2[i];
				if (b1 != b2) {
					return (i << SIZE_SHIFT) + IntBits.indexOfBit(b1 ^ b2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(int[] a1, int[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				int b1 = a1[i];
				int b2 = a2[i];
				if (b1 != b2) {
					return Integer.compare(b1, b2);
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(int[] a, int[] prefix, boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				int b1 = a[i];
				int b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static class Longs implements BaseInterface<long[]> {
		private static final long serialVersionUID = 7530883873551249293L;
		public static final Longs INSTANCE = new Longs();

		private Longs() {}

		public static final int SIZE_SHIFT = LongBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final long MASK_BIT = 1L << MASK_INDEX;

		public static long bit(long[] a, int index) {
			try {
				return (a[index >> SIZE_SHIFT] ^ MASK_BIT)
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(long[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(long[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(long[] a1, long[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				long b1 = a1[i];
				long b2 = a2[i];
				if (b1 != b2) {
					return (i << SIZE_SHIFT) + LongBits.indexOfBit(b1 ^ b2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(long[] a1, long[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				long b1 = a1[i];
				long b2 = a2[i];
				if (b1 != b2) {
					return Long.compare(b1, b2);
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(long[] a, long[] prefix, boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				long b1 = a[i];
				long b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static class Floats implements BaseInterface<float[]> {
		private static final long serialVersionUID = -301803560586437084L;
		public static final Floats INSTANCE = new Floats();

		private Floats() {}

		public static final int SIZE_SHIFT = FloatBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final long MASK_BIT = 1L << MASK_INDEX;

		public static long bit(float[] a, int index) {
			try {
				return FloatBits
						.bit(a[index >> SIZE_SHIFT], index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(float[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(float[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(float[] a1, float[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				float b1 = a1[i];
				float b2 = a2[i];
				if (b1 == 0 || b1 != b2) {
					int x = floatToIntBits(b1);
					int y = floatToIntBits(b2);
					if (x == y) continue;
					return (i << SIZE_SHIFT) + IntBits.indexOfBit(x ^ y);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(float[] a1, float[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				float b1 = a1[i];
				float b2 = a2[i];
				int r = Float.compare(b1, b2);
				if (r != 0) {
					return r;
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(float[] a, float[] prefix,
				boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				float b1 = a[i];
				float b2 = prefix[i];
				if (Float.compare(b1, b2) != 0) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static class Doubles implements BaseInterface<double[]> {
		private static final long serialVersionUID = 5355581870898494914L;
		public static final Doubles INSTANCE = new Doubles();

		private Doubles() {}

		public static final int SIZE_SHIFT = LongBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final long MASK_BIT = 1L << MASK_INDEX;

		public static long bit(double[] a, int index) {
			try {
				return DoubleBits.bit(a[index >> SIZE_SHIFT], index
						& MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(double[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(double[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(double[] a1, double[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				double b1 = a1[i];
				double b2 = a2[i];
				if (b1 == 0 || b1 != b2) {
					long x = doubleToLongBits(b1);
					long y = doubleToLongBits(b2);
					if (x == y) continue;
					return (i << SIZE_SHIFT) + LongBits.indexOfBit(x ^ y);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(double[] a1, double[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				double b1 = a1[i];
				double b2 = a2[i];
				int r = Double.compare(b1, b2);
				if (r != 0) {
					return r;
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(double[] a, double[] prefix,
				boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				double b1 = a[i];
				double b2 = prefix[i];
				if (Double.compare(b1, b2) != 0) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	// Special implementations

	public static final class UBytes implements BaseInterface<byte[]> {
		private static final long serialVersionUID = 3437926655035587782L;
		public static final UBytes INSTANCE = new UBytes();

		private UBytes() {}

		public static final int SIZE_SHIFT = ByteBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(byte[] a, int index) {
			try {
				return a[index >> SIZE_SHIFT]
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(byte[] a) {
			return a.length << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(byte[] a, int index) {
			return bit(a, index) != 0;
		}

		@Override
		public int contrast(byte[] a1, byte[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				byte b1 = a1[i];
				byte b2 = a2[i];
				if (b1 != b2) {
					return (i << SIZE_SHIFT) + ByteBits.indexOfBit(b1 ^ b2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(byte[] a1, byte[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				byte b1 = a1[i];
				byte b2 = a2[i];
				if (b1 != b2) {
					return (b1 & 0xFF) - (b2 & 0xFF);
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(byte[] a, byte[] prefix, boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				byte b1 = a[i];
				byte b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class Booleans implements BaseInterface<boolean[]> {
		private static final long serialVersionUID = 4397982975514525683L;
		public static final Booleans INSTANCE = new Booleans();

		private Booleans() {}

		@Override
		public int lengthBits(boolean[] a) {
			return a.length;
		}

		@Override
		public boolean isBitSet(boolean[] a, int index) {
			try {
				return a[index];
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int contrast(boolean[] a1, boolean[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				boolean b1 = a1[i];
				boolean b2 = a2[i];
				if (b1 != b2) {
					return i;
				}
			}

			if (len1 == len2) return -1;
			return lim;
		}

		@Override
		public int compare(boolean[] a1, boolean[] a2) {
			int len1 = a1.length;
			int len2 = a2.length;
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				boolean b1 = a1[i];
				boolean b2 = a2[i];
				if (b1 != b2) {
					return b1 ? 1 : -1;
				}
			}
			return len1 - len2;
		}

		@Override
		public boolean checkPrefixed(boolean[] a, boolean[] prefix,
				boolean inclusive) {
			int lim = prefix.length;
			if (inclusive) {
				if (lim > a.length) return false;
			} else if (lim >= a.length) return false;

			for (int i = 0; i < lim; i++) {
				boolean b1 = a[i];
				boolean b2 = prefix[i];
				if (b1 != b2) {
					return false;
				}
			}
			return true;
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}

	public static final class StringBits implements BaseInterface<String> {
		private static final long serialVersionUID = 1819036664749613728L;
		public static final StringBits INSTANCE = new StringBits();

		private StringBits() {}

		public static final int SIZE_SHIFT = CharBits.SIZE_SHIFT;
		public static final int MASK_INDEX = (1 << SIZE_SHIFT) - 1;
		public static final int MASK_BIT = 1 << MASK_INDEX;

		public static int bit(String s, int index) {
			try {
				return s.charAt(index >> SIZE_SHIFT)
						& MASK_BIT >>> (index & MASK_INDEX);
			} catch (IndexOutOfBoundsException e) {
				throw new BitIndexOutOfBoundsException(index);
			}
		}

		@Override
		public int lengthBits(String s) {
			return s.length() << SIZE_SHIFT;
		}

		@Override
		public boolean isBitSet(String s, int index) {
			return bit(s, index) != 0;
		}

		@Override
		public int contrast(String s1, String s2) {
			int len1 = s1.length();
			int len2 = s2.length();
			int lim = Math.min(len1, len2);

			for (int i = 0; i < lim; i++) {
				char c1 = s1.charAt(i);
				char c2 = s2.charAt(i);
				if (c1 != c2) {
					return (i << SIZE_SHIFT) + CharBits.indexOfBit(c1 ^ c2);
				}
			}

			if (len1 == len2) return -1;
			return lim << SIZE_SHIFT;
		}

		@Override
		public int compare(String s1, String s2) {
			return s1.compareTo(s2);
		}

		@Override
		public boolean checkPrefixed(String s, String prefix, boolean inclusive) {
			return s.startsWith(prefix)
					&& (inclusive || s.length() != prefix.length());
		}

		private Object readResolve() {
			return INSTANCE;
		}
	}
}
