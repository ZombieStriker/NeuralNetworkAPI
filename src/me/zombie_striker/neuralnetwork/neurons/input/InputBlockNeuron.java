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
import me.zombie_striker.neuralnetwork.senses.Senses2D;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Numbers;

/**
 * DO NOT USE. Still updating!
 * 
 * @author ZombieStriker
 *
 */

public class InputBlockNeuron extends InputNeuron {

	public InputBlockNeuron(NNAI ai, int xlink, int ylink, Sensory2D_Numbers sn) {
		this(ai, xlink, ylink, -1, sn);
	}

	public InputBlockNeuron(NNAI ai, int xlink, int ylink, int blockID,
			Sensory2D_Numbers sn) {
		super(ai, xlink, ylink,blockID, sn);
	}

	public InputBlockNeuron(NNAI ai, Sensory2D_Numbers sn) {
		super(ai);
		this.s = sn;
	}

	@Override
	public InputNeuron generateNeuron(NNAI ai, Senses2D n) {
		return InputBlockNeuron.generateNeuronStatically(ai, 0, 0,
				(Sensory2D_Numbers) n);
	}

	public static InputNeuron generateNeuronStatically(NNAI ai, int x, int y,
			Sensory2D_Numbers sn) {
		InputNeuron link = new InputBlockNeuron(ai, x, y, sn);
		return link;
	}

	@Override
	public boolean isTriggered() {
		if (tickUpdated == this.getAI().getCurrentTick())
			return lastResult == 1;
		tickUpdated = this.getAI().getCurrentTick();
		if ((zlink == -1 && ((Sensory2D_Numbers) s).getNumberAt(xlink, ylink) != 0)
				|| ((Sensory2D_Numbers) s).getNumberAt(xlink, ylink) == zlink) {
			lastResult = 1;
			return true;
		}
		lastResult = 0;
		return false;
	}
}
