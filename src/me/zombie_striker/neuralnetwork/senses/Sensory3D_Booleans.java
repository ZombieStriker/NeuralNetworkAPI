package me.zombie_striker.neuralnetwork.senses;

/**
 Copyright (C) 2017  Zombie_Striker

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **/

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
