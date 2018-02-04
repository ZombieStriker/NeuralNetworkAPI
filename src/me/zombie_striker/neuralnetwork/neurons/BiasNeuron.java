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

public class BiasNeuron extends Neuron{

	public BiasNeuron(NNAI ai, int layer) {
		super(ai, layer);
	}
	@Override
	public double getTriggeredStength() {
		if(droppedOut())
			return 0;
		return 1;
	}
	@Override
	public boolean isTriggered() {
		if(droppedOut())
			return false;
		if(!useThreshold())
			return true;
		return getThreshold() < 0.5;
	}
	public Neuron generateNeuron(NNAI ai) {
		return BiasNeuron.generateNeuronStatically(ai, this.layer);
	}

	public static BiasNeuron generateNeuronStatically(NNAI ai, int layer) {
		return new BiasNeuron(ai, layer);
	}
	@Override
	public double forceTriggerStengthUpdate() {
		this.tickUpdated = getAI().getCurrentTick();
		return lastResult = 1;
	}
	public BiasNeuron(Map<String,Object> map) {
		super(map);
	}
}
