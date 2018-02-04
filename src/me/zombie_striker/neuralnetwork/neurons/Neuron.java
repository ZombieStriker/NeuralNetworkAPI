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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.zombie_striker.neuralnetwork.NNAI;
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

	private boolean trainingThisNeuron = false;

	private boolean haveThreshold = true;

	private boolean allowNegativeValues = true;

	private boolean dropout = false;

	/**
	 * For some neurons, it may be better to not allow negative values. To fix this,
	 * allowNegitiveValues will force deeplearningutil and the neuronrandomizer to
	 * keep the neurons between 0 and 1. This should help in certain cases where
	 * neurons should not interfere with the values of others, but instead focus on
	 * Individualizing traits
	 * 
	 * @param b
	 *            If the neuron should allow negative values
	 * @return The neuron instance, so you can chain these modifications.
	 */
	public Neuron setAllowNegativeValues(boolean b) {
		this.allowNegativeValues = b;
		return this;
	}

	public boolean allowNegativeValues() {
		return allowNegativeValues;
	}

	/**
	 * 
	 * Dropping-out is a method used to cause the NN to generalize. Dropped-out
	 * neurons will not be activate, and because of that, will not contribute to the
	 * output value, causing other neurons when training to contribute more.
	 * 
	 * Manually "turns on" the drop-out
	 * 
	 * @param b
	 */
	public void setTemperaryDropout(boolean b) {
		this.dropout = b;
	}

	/**
	 * Dropping-out is a method used to cause the NN to generalize. Dropped-out
	 * neurons will not be activate, and because of that, will not contribute to the
	 * output value, causing other neurons when training to contribute more.
	 * 
	 * @return
	 */
	public boolean droppedOut() {
		return dropout;
	}

	/**
	 * For some cases, having a threshold for a neuron can be bad, as sometimes
	 * neurons may never fire or never want to fire, losing data. To fix this,
	 * useThreshold determines if thresholds should be considered when determining
	 * if a neuron isTriggered.
	 * 
	 * If you need logic gates, use thresholds. If you need to see similarities, and
	 * not have a pre-defined cut-off for what is and is not something, disable
	 * thresholds.
	 * 
	 * @param should
	 *            use thresholds
	 * @return The neuron instance, so you can chain multiple methods if needed.
	 */
	public Neuron setUseThreshold(boolean b) {
		this.haveThreshold = b;
		return this;
	}

	/**
	 * For some cases, having a threshold for a neuron can be bad, as sometimes
	 * neurons may never fire or never want to fire, losing data. To fix this,
	 * useThreshold determines if thresholds should be considered when determining
	 * if a neuron isTriggered.
	 * 
	 * If you need logic gates, use thresholds. If you need to see similarities, and
	 * not have a pre-defined cut-off for what is and is not something, disable
	 * thresholds.
	 * 
	 */
	public boolean useThreshold() {
		return haveThreshold;
	}

	/**
	 * This is a shortcut for myself. Instead of having to loop through all the
	 * neurons available FOR EACH NEURON in order to figure out if a neuron should
	 * trigger, this will do the calculations once per tick. If a neuron already was
	 * triggered by another neuron, just use the strength provided by that neuron.
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
		if(dropout)
			return false;
		if (!haveThreshold)
			return true;
		return getThreshold() < getTriggeredStength();
	}

	/**
	 * Returns the output for a neuron, factoring the weight, output strength, and
	 * trigger strength.
	 * 
	 * Will return 0 if the provided neuron is not an output for this neuron
	 * 
	 * @param The
	 *            output neuron to be tested
	 * @return the total output strength, or 0 if it is not an output.
	 */
	public double getOutputForNeuron(Neuron n) {
		if (!outputStength.containsKey(n.id))
			return 0;
		return weight * outputStength.get(n.id) * getTriggeredStength();
	}

	public double getTriggeredStength() {
		if (tickUpdated == ai.getCurrentTick()) {
			return lastResult;
		}
		if(dropout)
			return 0;
		return forceTriggerStengthUpdate();
	}

	public int getLayer() {
		return layer;
	}

	/**
	 * DO NOT USE. This should only be used by the DR class.
	 * 
	 * @param value
	 */
	public void forceLastResultChange(double value) {
		this.tickUpdated = ai.getCurrentTick();
		this.lastResult = value;
	}

	public double getLastResult() {
		return lastResult;
	}

	public boolean hasOutputTo(Neuron n) {
		return outputStength.containsKey(n.getID());
	}

	public double forceTriggerStengthUpdate() {
		double signal = 0;
		for (Neuron i : ai.getNeuronsInLayer(layer - 1))
			if (i.isTriggered())
				signal += (i.getOutputForNeuron(this));
		tickUpdated = ai.getCurrentTick();
		return lastResult = SigmoidUtil.sigmoidNumberPosAndNeg(signal + inheritBias);
	}

	public void forceTriggerUpdateTree() {
		this.forceTriggerStengthUpdate();
		for (int layers = this.layer + 1; layers < this.ai.maxlayers; layers++)
			for (Neuron n : this.ai.getNeuronsInLayer(layers))
				n.forceTriggerStengthUpdate();
	}

	public Neuron clone(NNAI ai) {
		return generateNeuron(ai);
	}

	public Neuron generateNeuron(NNAI ai) {
		return Neuron.generateNeuronStatically(ai, this.layer);
	}

	public static Neuron generateNeuronStatically(NNAI ai, int layer) {
		Neuron link = new Neuron(ai, layer);
		return link;
	}

	public boolean isTraining() {
		return trainingThisNeuron;
	}

	public void setIsTraining(boolean b) {
		this.trainingThisNeuron = b;
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

	public double getBias() {
		return inheritBias;
	}

	public void setBias(double bias) {
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
		this.weight = map.containsKey("w") ? (double) map.get("w") : 0;
		this.threshold = map.containsKey("t") ? (double) map.get("t") : -0.5;
		this.inheritBias = map.containsKey("bias") ? (double) map.get("bias") : 0.0;

		this.setUseThreshold(map.containsKey("uth"));
		this.setAllowNegativeValues((map.containsKey("anv") && (map.get("anv").equals(1))));
		if (map.containsKey("osC")) {
			this.outputStength = new HashMap<Integer, Double>();

			HashMap<Integer, List<Integer>> storedValues = (HashMap<Integer, List<Integer>>) map.get("osC");
			for (Entry<Integer, List<Integer>> e : storedValues.entrySet()) {
				for (Integer parse : e.getValue()) {
					outputStength.put(parse, (((double) e.getKey()) / 10000));
				}
			}

		} else if (map.containsKey("osB")) {
			this.outputStength = new HashMap<Integer, Double>();

			HashMap<Integer, List<String>> storedValues = (HashMap<Integer, List<String>>) map.get("osB");
			for (Entry<Integer, List<String>> e : storedValues.entrySet()) {
				for (String parse : e.getValue()) {
					if (parse.contains(",")) {
						int startingVal = Integer.parseInt(parse.split(",")[0]);
						int amountInSeries = Integer.parseInt(parse.split(",")[1]);
						for (int i = 0; i < amountInSeries; i++)
							outputStength.put(startingVal + i, (((double) e.getKey()) / 10000));
					} else {
						outputStength.put(Integer.parseInt(parse), (((double) e.getKey()) / 10000));
					}
				}
			}
		} else if (map.containsKey("osA")) {
			this.outputStength = new HashMap<Integer, Double>();

			HashMap<Double, List<String>> storedValues = (HashMap<Double, List<String>>) map.get("osA");
			for (Entry<Double, List<String>> e : storedValues.entrySet()) {
				for (String parse : e.getValue()) {
					if (parse.contains(",")) {
						int startingVal = Integer.parseInt(parse.split(",")[0]);
						int amountInSeries = Integer.parseInt(parse.split(",")[1]);
						for (int i = 0; i < amountInSeries; i++)
							outputStength.put(startingVal + i, e.getKey());
					} else {
						outputStength.put(Integer.parseInt(parse), e.getKey());
					}
				}
			}
		} else if (map.containsKey("os")) {
			// Legacy values:
			this.outputStength = (HashMap<Integer, Double>) map.get("os");
		}
		this.id = (int) map.get("id");
		this.layer = (int) map.get("l");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		if (this.weight != 0)
			m.put("w", this.weight);
		if (this.weight != -0.5)
			m.put("t", this.threshold);
		if (this.useThreshold())
			m.put("uth", 1);
		if (this.allowNegativeValues)
			m.put("anv", 1);
		if (this.inheritBias != 0)
			m.put("bias", this.inheritBias);

		// m.put("os", outputStength);
		// DO not use OS. Instead, create a table for outputs.

		HashMap<Double, List<Integer>> verseStrengths = new HashMap<Double, List<Integer>>();
		for (Entry<Integer, Double> e : outputStength.entrySet()) {
			List<Integer> currentList = verseStrengths.containsKey(e.getValue()) ? verseStrengths.get(e.getValue())
					: new ArrayList<Integer>();
			currentList.add(e.getKey());
			verseStrengths.put(e.getValue(), currentList);
		}

		List<Integer> verify = new ArrayList<>();

		HashMap<Integer, List<Integer>> savedOutputs = new HashMap<Integer, List<Integer>>();
		for (Double val : verseStrengths.keySet()) {
			// int start = -1;
			// int lastVal = -1;
			List<Integer> vals = new ArrayList<Integer>();
			// boolean first = true;
			for (int i : verseStrengths.get(val)) {
				/*
				 * if (start == -1) { start = i; lastVal = i - 1; } if (lastVal != i - 1) { //
				 * If there was a skip in pacing, save the last series of // numbers. int add =
				 * first ? 1 : 0; first = false; String savedString = start + ((lastVal + add -
				 * start==1) ? "" : ("," + (lastVal + add - start))); vals.add(savedString);
				 * start = i; } lastVal = i;
				 */

				verify.add(i);
				vals.add(i);
			}
			int j = (int) (val * 10000);
			if (savedOutputs.containsKey(j)) {
				List<Integer> temp = vals;
				vals = savedOutputs.get(j);
				vals.addAll(temp);
			}
			savedOutputs.put(j, vals);
		}
		// Somehow, neuron connections where not being saved. This should fix that
		if (ai.maxlayers != layer + 1) {
			for (Neuron n : ai.getNeuronsInLayer(layer + 1)) {
				if ((!verify.contains(n.getID())) && this.hasOutputTo(n)) {
					int j = (int) (this.getOutputForNeuron(n) * 10000);
					List<Integer> vals = savedOutputs.containsKey(j) ? savedOutputs.get(j) : new ArrayList<Integer>();
					vals.add(n.getID());
					savedOutputs.put(j, vals);
				}
			}
		}
		m.put("osC", savedOutputs);

		/*
		 * HashMap<Double, List<Integer>> verseStrengths = new HashMap<Double,
		 * List<Integer>>(); for (Entry<Integer, Double> e : outputStength.entrySet()) {
		 * List<Integer> currentList = verseStrengths .containsKey(e.getValue()) ?
		 * verseStrengths.get(e .getValue()) : new ArrayList<Integer>();
		 * currentList.add(e.getKey()); verseStrengths.put(e.getValue(), currentList); }
		 * 
		 * HashMap<Double, List<String>> savedOutputs = new HashMap<Double,
		 * List<String>>(); for (Double val : verseStrengths.keySet()) { int start = -1;
		 * int lastVal = -1; List<String> vals = new ArrayList<String>(); boolean first
		 * = true; for (int i : verseStrengths.get(val)) { if (start == -1) { start = i;
		 * lastVal = i - 1; } if (lastVal != i - 1) { // If there was a skip in pacing,
		 * save the last series of // numbers. int add = first ? 1 : 0; first = false;
		 * String savedString = start + ((lastVal + add == start) ? "" : ("," + (lastVal
		 * + add - start))); vals.add(savedString); start = i; } lastVal = i; } int add
		 * = first ? 1 : 0; String savedString = start + ((lastVal + add == start) ? ""
		 * : ("," + (lastVal + add - start))); vals.add(savedString);
		 * savedOutputs.put(val, vals); } m.put("osA", savedOutputs);
		 */

		m.put("id", this.id);
		m.put("l", this.layer);
		return m;
	}
}
