package me.zombie_striker.neuralnetwork.senses;

import java.util.HashMap;
import java.util.Map;

public class Sensory2D_Booleans implements Senses2D{

	@Override
	public double getPowerFor(int x, int y) {
		return values[x][y]?1:0;
	}
	
	private boolean[][] values;

	public Sensory2D_Booleans(int rows, int col) {
		this.values = new boolean[rows][col];
	}

	public boolean getBooleanAt(int row, int col) {
		return values[row][col];
	}

	public void changeMatrix(boolean[][] newValues) {
		this.values = newValues;
	}
	public boolean[][] getMatrix(){
		return this.values;
	}
	public void changeValueAt(int x, int y, boolean value){
		this.values[x][y]=value;
	}
	
	public Sensory2D_Booleans(Map<String, Object> map) {
		values = new boolean[(int) map.get("x")][(int) map.get("y")];
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> v = new HashMap<String, Object>();
		v.put("x", values.length);
		v.put("y", values[0].length);
		return v;
	}

}
