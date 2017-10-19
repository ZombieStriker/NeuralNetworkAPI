package me.zombie_striker.neuralnetwork;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.zombie_striker.neuralnetwork.neurons.Neuron;

public class Layer implements ConfigurationSerializable {
	public ArrayList<Neuron> neuronsInLayer = new ArrayList<>();
	private int i;

	private int neuronsperrow = -1;

	public Layer(int layer) {
		i = layer;
	}

	public int getLayer() {
		return i;
	}

	public int getNeuronsPerRow() {
		return neuronsperrow;
	}

	public void setNeuronsPerRow(int i) {
		neuronsperrow = i;
	}

	@SuppressWarnings("unchecked")
	public Layer(Map<String, Object> map) {
		this.neuronsInLayer = (ArrayList<Neuron>) map.get("n");
		this.i = (int) map.get("l");
		this.neuronsperrow = (int) map.get("npr");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("n", this.neuronsInLayer);
		m.put("l", this.i);
		m.put("npr", neuronsperrow);
		return m;
	}
}
