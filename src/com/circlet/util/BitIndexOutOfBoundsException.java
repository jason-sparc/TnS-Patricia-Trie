package com.circlet.util;

public class BitIndexOutOfBoundsException extends IndexOutOfBoundsException {
	private static final long serialVersionUID = -1896296096884528131L;

	public BitIndexOutOfBoundsException() {
	}

	public BitIndexOutOfBoundsException(int index) {
		super("Bit index out of range: " + index);
	}

	public BitIndexOutOfBoundsException(String s) {
		super(s);
	}
}
