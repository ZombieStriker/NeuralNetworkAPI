package me.zombie_striker.neuralnetwork.neurons.input;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.*;

public class InputNumberNeuron extends InputNeuron {

	public InputNumberNeuron(NNAI ai, Sensory2D_Numbers sl) {
		super(ai);
		this.s = sl;
	}

	public InputNumberNeuron(NNAI ai, int row, int col,
			Sensory2D_Numbers sl) {
		super(ai, row, col,sl);
		this.s = sl;
	}

	@Override
	public InputNeuron generateNeuron(NNAI ai, Senses2D word) {
		return InputNumberNeuron.generateNeuronStatically(ai, 0, ' ',
				 (Sensory2D_Numbers) word);
	}
	
	public static InputNeuron generateNeuronStatically(NNAI ai, int row,
			int col, Sensory2D_Numbers sl) {
		return new InputNumberNeuron(ai, row, col, (Sensory2D_Numbers) sl);
	}

	@Override
	public boolean isTriggered() {
		return getTriggeredStength()!=0;
	}

}
