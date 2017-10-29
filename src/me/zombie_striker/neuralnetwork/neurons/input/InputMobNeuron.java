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
public class InputMobNeuron extends InputNeuron {
	
	public InputMobNeuron(NNAI ai,Sensory2D_Numbers sn) {
		super(ai);
		this.s = sn;
	}

	public InputMobNeuron(NNAI ai, int xlink, int ylink, Sensory2D_Numbers sn) {
		super(ai,xlink,ylink,sn);
		this.s = sn;
	}

	@Override
	public InputNeuron generateNeuron(NNAI ai,Senses2D sn) {
		return InputMobNeuron.generateNeuronStatically(ai, (Sensory2D_Numbers) sn);
	}

	public static InputNeuron generateNeuronStatically(NNAI ai,Sensory2D_Numbers sn) {
		InputNeuron link = new InputMobNeuron(ai,sn);
		return link;
	}


	@Override
	public boolean isTriggered() {
		if(tickUpdated==this.getAI().getCurrentTick()){
			return lastResult==1;
		}
		tickUpdated = this.getAI().getCurrentTick();
		if (((Sensory2D_Numbers) s).getNumberAt(xlink,ylink) !=0) {
			lastResult=1;
			return true;
		}
		lastResult=0;
		return false;
	}


}