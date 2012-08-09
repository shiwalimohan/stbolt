package com.soartech.bolt.evaluation;

import java.util.Random;

public class BoardLocation {
	private int row;
	private int column;
	private static final Random rnd = new Random(System.currentTimeMillis());
	
	public BoardLocation(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public static BoardLocation getRandomLocation() {
		return new BoardLocation(rnd.nextInt(5), rnd.nextInt(5));
	}
	
	public int getRow() {
		return row;
	}
	
	public void setRow(int row) {
		this.row = row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public void setColumn(int column) {
		this.column = column;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof BoardLocation) {
			BoardLocation loc = (BoardLocation)o;
			if(this.row == loc.row && this.column == loc.column)
				return true;
		}
		return false;
	}
	
	@Override 
	public int hashCode() {
		return row+5*column;
	}
	
	public String getDescription() {
		return "row "+row+" column "+column;
	}
}