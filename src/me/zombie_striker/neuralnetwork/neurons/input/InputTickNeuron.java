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

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.*;

/**
 * DO NOT USE. Still updating!
 * @author ZombieStriker
 *
 */
public class InputTickNeuron extends InputNeuron {

	int maxTick=0;
	
	public InputTickNeuron(NNAI ai,int maxtick) {
		super(ai);
		this.maxTick = maxtick;
	}

	public InputTickNeuron(NNAI ai, int row, int col,
			Sensory2D_Numbers sl) {
		super(ai, row, col,sl);
	}

	@Override
	public InputNeuron generateNeuron(NNAI ai, Senses2D word) {
		return InputTickNeuron.generateNeuronStatically(ai, 0);
	}
	
	public static InputNeuron generateNeuronStatically(NNAI ai, int maxTick) {
		InputNeuron link = new InputTickNeuron(ai, maxTick);
		return link;
	}

	@Override
	public double getTriggeredStength() {
		return ((/*(double)*/getAI().getCurrentTick()%maxTick)/*/maxTick*/)==0?1:0;
	}

	@Override
	public boolean isTriggered() {
		return getTriggeredStength()==1;
	}

}
