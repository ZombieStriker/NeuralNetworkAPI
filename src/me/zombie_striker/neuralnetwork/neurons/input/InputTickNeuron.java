package me.zombie_striker.neuralnetwork.neurons.input;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.*;

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
