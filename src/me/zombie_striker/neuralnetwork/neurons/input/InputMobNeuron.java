package me.zombie_striker.neuralnetwork.neurons.input;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.*;

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