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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.neurons.input.InputBlockNeuron;
import me.zombie_striker.neuralnetwork.neurons.input.InputMobNeuron;
import me.zombie_striker.neuralnetwork.util.SigmoidUtil;

public class Neuron implements ConfigurationSerializable {

	// private Set<Integer> input = new HashSet<Integer>();
	// private Set<Integer> output = new HashSet<Integer>();

	private NNAI ai;
	private int id;
	public int layer;

	private HashMap<Integer, Double> outputStength = new HashMap<>();
	private double weight = 0.5;
	private double threshold = -0.5;
	
	private double inheritBias = 0;

	/**
	 * This is a shortcut for myself. Instead of having to loop through all the
	 * neurons available FOR EACH NEURON in order to figure out if a neuron
	 * should trigger, this will do the calculations once per tick. If a neuron
	 * already was triggered by another neuron, just use the strength provided
	 * by that neuron.
	 */
	protected int tickUpdated = -1;
	protected double lastResult = -1;

	public Neuron(NNAI ai, int layer) {
		this.ai = ai;
		this.layer = layer;
		id = ai.generateNeuronId();
		ai.addNeuron(this);
		ai.getNeuronsInLayer(layer).add(this);
	}

	public boolean isTriggered() {
		return getTriggeredStength() > threshold;
	}

	/**
	 * Returns the output for a neuron, factoring the weight, output strength,
	 * and trigger strength.
	 * 
	 * Will return 0 if the provided neuron is not an output for this neuron
	 * 
	 * @param The
	 *            output neuron to be tested
	 * @return the total output strength, or 0 if it is not an output.
	 */
	public double getOutputForNeuron(Neuron n) {
		if (!this.outputStength.containsKey(n.id))
			return 0;
		return weight * outputStength.get(n.id) * getTriggeredStength();
	}

	public double getTriggeredStength() {
		if (tickUpdated == ai.getCurrentTick()) {
			return lastResult;
		}
		return forceTriggerStengthUpdate();
	}

	/**
	 * DO NOT USE. This should only be used by the DR class.
	 * 
	 * @param value
	 */
	public void forceOutputValue(double value) {
		this.lastResult = value;
	}

	public double forceTriggerStengthUpdate() {
		double signal = 0;

		for (Neuron i : ai.getNeuronsInLayer(layer - 1)/*
														 * .getNeuronsFromId(input
														 * )
														 */) {
			if (i != null) {
				if (i.isTriggered()) {
					signal += (i.getOutputForNeuron(this));
				}
			}
		}
		tickUpdated = ai.getCurrentTick();
		return lastResult = (SigmoidUtil.sigmoidNumber(signal+inheritBias) * 2) - 1;
	}

	public void forceTriggerUpdateTree() {
		forceTriggerStengthUpdate();
		for (int layers = this.layer + 1; layers < this.ai.MAX_LAYERS; layers++) {
			for (Neuron n : this.ai.getNeuronsInLayer(layers)) {
				n.forceTriggerStengthUpdate();
			}
		}
	}

	public Neuron clone(NNAI ai) {
		Neuron newNeuron = null;
		if (this instanceof InputMobNeuron)
			newNeuron = ((InputMobNeuron) this).generateNeuron(ai);
		if (this instanceof InputBlockNeuron)
			newNeuron = ((InputBlockNeuron) this).generateNeuron(ai);
		if (this instanceof OutputNeuron)
			newNeuron = ((OutputNeuron) this).generateNeuron(ai);
		// newNeuron.input = new HashSet<>(this.input);
		// newNeuron.output = new HashSet<>(this.output);
		return newNeuron;
	}

	public Neuron generateNeuron(NNAI ai) {
		return Neuron.generateNeuronStatically(ai, this.layer);
	}

	public static Neuron generateNeuronStatically(NNAI ai, int layer) {
		Neuron link = new Neuron(ai, layer);
		return link;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double w) {
		weight = w;
	}

	public double getThreshold() {
		return threshold;
	}
	public double getBias(){
		return inheritBias;
	}
	public void setBias(double bias){
		this.inheritBias = bias;
	}

	public void setThreshold(double t) {
		threshold = t;
	}

	public Set<Integer> getStrengthIDs() {
		return outputStength.keySet();
	}

	public double getStrengthForNeuron(Neuron n) {
		return outputStength.get(n.id);
	}

	public double getStrengthForNeuron(int id) {
		return outputStength.get(id);
	}

	public void setStrengthForNeuron(Neuron n, double v) {
		outputStength.put(n.id, v);
	}

	public void setStrengthForNeuron(int n, double v) {
		outputStength.put(n, v);
	}

	/*
	 * public Set<Integer> getInputs(){ return input; } public Set<Integer>
	 * getOutputs(){ return output; }
	 */
	public NNAI getAI() {
		return ai;
	}

	public int getID() {
		return id;
	}

	public void setAI(NNAI ai) {
		this.ai = ai;
	}

	@SuppressWarnings("unchecked")
	public Neuron(Map<String, Object> map) {
		this.weight = (double) map.get("w");
		this.threshold = (double) map.get("t");
		this.outputStength = (HashMap<Integer, Double>) map.get("os");
		this.id = (int) map.get("id");
		this.layer = (int) map.get("l");
		//ai.getNeuronsInLayer(layer).add(this);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("w", this.weight);
		m.put("t", this.threshold);
		m.put("os", outputStength);
		m.put("id", this.id);
		m.put("l", this.layer);
		return m;
	}
}
