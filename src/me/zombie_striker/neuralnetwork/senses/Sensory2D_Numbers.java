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
