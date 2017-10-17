package me.zombie_striker.neuralnetwork.senses;

import java.util.HashMap;
import java.util.Map;

public class Sensory2D_Numbers implements Senses2D{

	@Override
	public double getPowerFor(int x, int y) {
		return values[x][y];
	}

	private double[][] values;

	public Sensory2D_Numbers(int rows, int col) {
		this.values = new double[rows][col];
	}

	public double getNumberAt(int row, int col) {
		return values[row][col];
	}

	public void changeMatrix(double[][] newValues) {
		this.values = newValues;
	}
	public double[][] getMatrix(){
		return this.values;
	}
	public void changeNumberAt(int x, int y, double value){
		this.values[x][y]=value;
	}
	public Sensory2D_Numbers(Map<String, Object> map) {
		values = new double[(int) map.get("x")][(int) map.get("y")];
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> v = new HashMap<String, Object>();
		v.put("x", values.length);
		v.put("y", values[0].length);
		return v;
	}
}
