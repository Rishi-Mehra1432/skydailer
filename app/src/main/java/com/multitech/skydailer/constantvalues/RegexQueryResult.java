package com.multitech.skydailer.constantvalues;

public class RegexQueryResult implements Comparable<RegexQueryResult> {
	public final int position;
	public final int start;
	public final int end;
	public int numberStart;
	public int numberEnd;
	
	public RegexQueryResult(int position, int start, int end) {
		this.position = position;
		this.start = start;
		this.end = end;
	}
	
	public void setNumberPlace(int numberStart, int numberEnd) {
		this.numberStart = numberStart;
		this.numberEnd = numberEnd;
	}
	
	@Override
	public int compareTo(RegexQueryResult obj) throws NullPointerException, ClassCastException {
		if (null == obj) {
			throw new NullPointerException();
		}
		if (!(obj instanceof RegexQueryResult)) {
			throw new ClassCastException();
		}
		if (this.start > obj.start) {
			return 1;
		} else if (this.start == obj.start) {
			if (this.position == obj.position) {
				return 0;
			} else if (this.position > obj.position) {
				return 1;
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}
}
