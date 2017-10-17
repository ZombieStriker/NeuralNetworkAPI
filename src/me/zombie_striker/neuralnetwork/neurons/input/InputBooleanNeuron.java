package me.zombie_striker.neuralnetwork.neurons.input;

import java.util.Map;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.*;

public class InputBooleanNeuron extends InputNeuron {

	public InputBooleanNeuron(NNAI ai, Sensory2D_Booleans sl) {
		super(ai);
		this.s = sl;
	}

	public InputBooleanNeuron(NNAI ai, int row, int col,
			Sensory2D_Booleans sl) {
		super(ai, row, col,sl);
		this.s = sl;
	}

	@Override
	public InputNeuron generateNeuron(NNAI ai, Senses2D word) {
		return InputBooleanNeuron.generateNeuronStatically(ai, 0, ' ',
				 (Sensory2D_Booleans) word);
	}
	
	public static InputNeuron generateNeuronStatically(NNAI ai, int row,
			int col, Sensory2D_Booleans sl) {
		InputNeuron link = new InputBooleanNeuron(ai, row, col, (Sensory2D_Booleans) sl);
		return link;
	}
	public InputBooleanNeuron(Map<String,Object> map) {
		super(map);
	}
}
