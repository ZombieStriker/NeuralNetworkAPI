package me.zombie_striker.neuralnetwork.senses;

import java.util.HashMap;
import java.util.Map;

public class Sensory3D_Booleans implements Senses3D{

	private boolean[][][] values;
	
	@Override
	public double getPowerFor(int x, int y,int z) {
		return values[x][y][z]?1:0;
	}	

	public Sensory3D_Booleans(int x,int y,int z) {
		this.values = new boolean[x][y][z];
	}

	public boolean getBooleanAt(int x,int y ,int z) {
		return values[x][y][z];
	}

	public void changeMatrix(boolean[][][] newValues) {
		this.values = newValues;
	}
	public boolean[][][] getMatrix(){
		return this.values;
	}
	public void changeValueAt(int x, int y,int z, boolean value){
		this.values[x][y][z]=value;
	}
	public Sensory3D_Booleans(Map<String, Object> map) {
		values = new boolean[(int) map.get("x")][(int) map.get("y")][(int) map.get("z")];
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
