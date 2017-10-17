package me.zombie_striker.neuralnetwork.senses;

import java.util.HashMap;
import java.util.Map;

public class Sensory3D_Numbers implements Senses3D{

	private double[][][] values;
	
	@Override
	public double getPowerFor(int x, int y, int z) {
		return values[x][y][z];
	}

	public Sensory3D_Numbers(int x, int y, int z) {
		this.values = new double[x][y][z];
	}

	public double getNumberAt(int x,int y, int z) {
		return values[x][y][z];
	}

	public void changeMatrix(double[][][] newValues) {
		this.values = newValues;
	}
	public double[][][] getMatrix(){
		return this.values;
	}
	public void changeNumberAt(int x, int y,int z, double value){
		this.values[x][y][z]=value;
	}
	public Sensory3D_Numbers(Map<String, Object> map) {
		values = new double[(int) map.get("x")][(int) map.get("y")][(int) map.get("z")];
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> v = new HashMap<String, Object>();
		v.put("x", values.length);
		v.put("y", values[0].length);
		v.put("z", values[0][0].length);
		return v;
	}
}
