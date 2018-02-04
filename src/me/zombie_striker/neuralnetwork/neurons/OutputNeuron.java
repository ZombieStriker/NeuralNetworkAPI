package me.zombie_striker.neuralnetwork.neurons;

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

import java.util.Map;

import me.zombie_striker.neuralnetwork.NNAI;

public class OutputNeuron extends Neuron {

	public int responceid;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String n) {
		name = n;
	}

	public boolean hasName() {
		return name != null;
	}

	public OutputNeuron(NNAI ai, int responceid) {
		super(ai, ai.maxlayers - 1);
		this.responceid = responceid;
	}

	public OutputNeuron(NNAI ai, int responceid, int layer) {
		super(ai, layer);
		this.responceid = responceid;
	}

	@Override
	public Neuron generateNeuron(NNAI ai) {
		OutputNeuron n = new OutputNeuron(ai, responceid);
		return n;
	}

	@Override
	public Neuron clone(NNAI ai) {
		OutputNeuron clone = (OutputNeuron) generateNeuron(ai);
		clone.responceid = responceid;
		return clone;
	}

	@Override
	public boolean isTriggered() {
		if (allowNegativeValues())
			return getTriggeredStength() > 0.0;
		else
			return getTriggeredStength() > 0.5;
	}

	public OutputNeuron(Map<String, Object> map) {
		super(map);
		this.name = (String) map.get("n");
		this.responceid = (int) map.get("rid");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = super.serialize();// new HashMap<String, Object>();
		m.put("n", this.name);
		m.put("rid", this.responceid);
		return m;
	}
}
