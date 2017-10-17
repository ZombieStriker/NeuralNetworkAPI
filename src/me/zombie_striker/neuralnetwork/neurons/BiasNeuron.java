package me.zombie_striker.neuralnetwork.neurons;

import java.util.Map;

import me.zombie_striker.neuralnetwork.NNAI;

public class BiasNeuron extends Neuron{

	public BiasNeuron(NNAI ai, int layer) {
		super(ai, layer);
	}
	@Override
	public double getTriggeredStength() {
		return 1;
	}
	@Override
	public boolean isTriggered() {
		return 0.5  > getThreshold();
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
