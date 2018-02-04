package me.zombie_striker.neuralnetwork.neurons.input;

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
import me.zombie_striker.neuralnetwork.neurons.Neuron;
import me.zombie_striker.neuralnetwork.senses.*;

public class InputNeuron extends Neuron {

	public int xlink;
	public int ylink;
	public int zlink;

	public Senses s;

	public boolean isTriggeredLast = false;

	/// public Sensory_Vision entitiesVision;

	public InputNeuron(NNAI ai, int xlink, int ylink, int zlink, Senses s) {
		super(ai, 0);
		init(ai, xlink, ylink, zlink, s);
	}

	public InputNeuron(NNAI ai, int xlink, int ylink, Senses s) {
		super(ai, 0);
		init(ai, xlink, ylink, 0, s);
	}

	public InputNeuron(NNAI ai) {
		super(ai, 0);
	}

	private void init(NNAI ai, int xlink, int ylink, int zlink, Senses s) {
		this.xlink = xlink;
		this.ylink = ylink;
		this.zlink = zlink;
		this.s = s;
	}

	public void setIsTriggeredLast(boolean b) {
		this.isTriggeredLast = b;
	}

	public boolean getIsTriggeredLast() {
		return this.isTriggeredLast;
	}

	@Override
	public Neuron clone(NNAI ai) {
		InputNeuron n = (InputNeuron) super.clone(ai);
		n.xlink = xlink;
		n.ylink = ylink;
		return null;
	}

	public InputNeuron generateNeuron(NNAI ai, Senses2D sensory) {
		return null;
	}

	@Override
	public double forceTriggerStengthUpdate() {
		this.tickUpdated = getAI().getCurrentTick();
		
		double i = 0;
		
		if (s instanceof Senses2D)
			i= lastResult = ((Senses2D) s).getPowerFor(xlink, ylink);
		if (s instanceof Senses3D)
			i= lastResult = ((Senses3D) s).getPowerFor(xlink, ylink, zlink);
			isTriggeredLast = i > 0;
		return i;
	}

	@Override
	public double getTriggeredStength() {
		if (this.tickUpdated == this.getAI().getCurrentTick()) 
			return lastResult;
		if (s instanceof Senses2D)
			return lastResult = ((Senses2D) s).getPowerFor(xlink, ylink);
		if (s instanceof Senses3D)
			return lastResult = ((Senses3D) s).getPowerFor(xlink, ylink, zlink);
		return 0;
	}

	public Senses getSenses() {
		return s;
	}

	public void stSenses(Senses s) {
		this.s = s;
	}

	public InputNeuron(Map<String, Object> map) {
		super(map);
		this.xlink = map.containsKey("xl") ? (int) map.get("xl") : 0;
		this.ylink = map.containsKey("yl") ? (int) map.get("yl") : 0;
		this.zlink = map.containsKey("zl") ? (int) map.get("zl") : 0;
		this.s = (Senses) map.get("s");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = super.serialize();// new HashMap<String, Object>();
		if (this.xlink != 0)
			m.put("xl", this.xlink);
		if (this.ylink != 0)
			m.put("yl", this.ylink);
		if (this.zlink != 0)
			m.put("zl", this.zlink);
		m.put("s", this.s);
		return m;
	}
}
