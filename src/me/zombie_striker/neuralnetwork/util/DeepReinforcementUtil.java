package me.zombie_striker.neuralnetwork.util;

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

import java.util.*;
import java.util.Map.Entry;

import me.zombie_striker.neuralnetwork.NNBaseEntity;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputNeuron;

public class DeepReinforcementUtil {

	/**
	 * Teaches the NN for an instantaneous, constant answer regarding a single
	 * response. Useful for true-false situations
	 * 
	 * @param Base
	 *            The NeuralEntity
	 * @param neuronsThatShouldBeTrue
	 *            Array of all neuons that should be equal to 1. The rest will
	 *            try to be decreased to -1;
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 */
	public static void instantaneousReinforce(NNBaseEntity base,
			Neuron[] neuronsThatShouldBeTrue, int repetitions) {
		instantaneousReinforce(base, neuronsThatShouldBeTrue, repetitions, 0);
	}

	/**
	 * Teaches the NN for an instantaneous, constant answer regarding a single
	 * response. Useful for true-false situations
	 * 
	 * @param Base
	 *            The NeuralEntity
	 * @param neuronsThatShouldBeTrue
	 *            Array of all neuons that should be equal to 1. The rest will
	 *            try to be decreased to -1;
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 * @param chanceForSkip
	 *            The chance that a neuron will be skipped. This is done to make
	 *            sure neurons are not over specialized for each time the NN is
	 *            reinforced.
	 */
	public static void instantaneousReinforce(NNBaseEntity base,
			Neuron[] neuronsThatShouldBeTrue, int repetitions,
			double chanceForSkip) {
		HashMap<Neuron, Double> h = new HashMap<>();
		for (Neuron n : base.ai.getOutputNeurons()) {
			boolean isRightNeuron = false;
			for (Neuron rn : neuronsThatShouldBeTrue) {
				if (n == rn) {
					isRightNeuron = true;
					break;
				}
			}
			h.put(n, isRightNeuron ? 1.0 : -1.0);
		}
		instantaneousReinforce(base, h, repetitions, chanceForSkip);
	}

	/**
	 * Teaches the NN for an instantaneous, constant answer regarding a single
	 * response. Useful for true-false situations
	 * 
	 * @param Base
	 *            the NeuralEntity
	 * @param correctValues
	 *            a Hashmap of all neurons and the values they should be equal
	 *            to
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 */
	public static void instantaneousReinforce(NNBaseEntity base,
			HashMap<Neuron, Double> correctValues, int repetitions) {
		instantaneousReinforce(base, correctValues, repetitions, 0);
	}

	/**
	 * Teaches the NN for an instantaneous, constant answer regarding a single
	 * response. Useful for true-false situations
	 * 
	 * @param Base
	 *            the NeuralEntity
	 * @param correctValues
	 *            a Hashmap of all neurons and the values they should be equal
	 *            to
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 * @param chanceForSkip
	 *            The chance that a neuron will be skipped. This is done to make
	 *            sure neurons are not over specialized for each time the NN is
	 *            reinforced.
	 * 
	 */
	public static void instantaneousReinforce(NNBaseEntity base,
			HashMap<Neuron, Double> correctValues, int repetitions,
			double chanceForSkip) {
		final double step = 0.01;

		for (int loops = 0; loops < repetitions; loops++) {
			HashMap<Neuron, Double> suggestedValueForNeuron = correctValues;
			// Subtract -1 a second time to not get the outputs
			// for (int layer = base.ai.MAX_LAYERS - 1 - 1; layer >= 0; layer--)
			// {
			for (int layer = 0; layer < base.ai.MAX_LAYERS - 1; layer++) {
				HashMap<Neuron, Double> lastOutputs = new HashMap<Neuron, Double>();
				// Since no new outputs are created, nor are any destroyed, we
				// do not need to clear it: simply adding new values will
				// override the existing vals.
				for (Neuron n : base.ai.getNeuronsInLayer(layer)) {
					if (Math.random() < chanceForSkip)
						continue;

					// Do thresh checks before if is not triggered

					// Do thresholds after everything else
					if (!n.isTriggered()) {
						double orgThr = n.getThreshold();
						for (Neuron outputs : suggestedValueForNeuron.keySet())
							lastOutputs.put(outputs,
									outputs.getTriggeredStength());

						if (!(n instanceof InputNeuron)
								&& n.getTriggeredStength() <= n.getThreshold()) {
							n.setThreshold(-2);
							n.forceTriggerUpdateTree();
							double shouldLowerVal = 0;
							for (Entry<Neuron, Double> c : lastOutputs
									.entrySet()) {
								double sug = suggestedValueForNeuron.get(c
										.getKey());
								shouldLowerVal += (Math.abs(c.getValue() - sug) - Math
										.abs(c.getKey().getTriggeredStength()
												- sug));
							}
							n.setThreshold(orgThr);
							if (shouldLowerVal > 0) {
								n.setThreshold(removeExtremes(n.getThreshold()
										- step));
							}
						}
						continue;
					}

					for (Neuron outputs : suggestedValueForNeuron.keySet())
						lastOutputs.put(outputs, outputs.getTriggeredStength());

					n.setWeight(n.getWeight() - step);
					n.forceTriggerUpdateTree();
					double change2 = 0.0;
					for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
						// TODO: Multipled by 100 so the change is not a small
						// number.
						double sug = suggestedValueForNeuron.get(c.getKey());
						int wasRightDirection = Math.abs(sug
								- c.getKey().getTriggeredStength()) <= Math
								.abs(sug - c.getValue()) ? 1 : -1;

						change2 += wasRightDirection
								* Math.abs(c.getKey().getTriggeredStength()
										- c.getValue());
						/*
						 * change2 += ((Math
						 * .abs((c.getKey().getTriggeredStength() * 100) -
						 * (c.getValue() * 100))) * (Math
						 * .abs((suggestedValueForNeuron.get(c.getKey()) - c
						 * .getKey().getTriggeredStength())) <= Math
						 * .abs(suggestedValueForNeuron.get(c.getKey()) -
						 * c.getValue()) ? 1.0 : -1.0));
						 */

					}

					if (change2 == 0.0) {
						n.setWeight(n.getWeight() + step);
					} else if (change2 < 0.0) {
						n.setWeight(n.getWeight() + (step * 2));
					}
					n.setWeight(removeExtremes(n.getWeight()));
					n.forceTriggerUpdateTree();

					for (int outputNeuronId : n.getStrengthIDs()) {
						Neuron outputNeuronInstance = n.getAI()
								.getNeuronFromId(outputNeuronId);

						for (Neuron outputs : suggestedValueForNeuron.keySet())
							lastOutputs.put(outputs,
									outputs.getTriggeredStength());

						n.setStrengthForNeuron(outputNeuronInstance,
								n.getOutputForNeuron(outputNeuronInstance)
										- step);
						n.forceTriggerStengthUpdate();
						outputNeuronInstance.forceTriggerUpdateTree();
						double change = 0.0;
						for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
							double sug = suggestedValueForNeuron
									.get(c.getKey());
							int wasRightDirection = (Math.abs(sug
									- c.getKey().getTriggeredStength()) <= Math
									.abs(sug - c.getValue()) ? 1 : -1);

							change += wasRightDirection
									* Math.abs(c.getKey().getTriggeredStength()
											- c.getValue());
							/*
							 * change += ((Math.abs((c.getKey()
							 * .getTriggeredStength() * 100) - (c.getValue() *
							 * 100))) * (Math
							 * .abs((suggestedValueForNeuron.get(c .getKey()) -
							 * c.getKey() .getTriggeredStength())) <= Math
							 * .abs(suggestedValueForNeuron.get(c.getKey()) -
							 * c.getValue()) ? 1.0 : -1.0));
							 */
						}

						if (change == 0.0) {
							n.setStrengthForNeuron(outputNeuronInstance,
									n.getOutputForNeuron(outputNeuronInstance)
											+ (step));
						} else if (change < 0.0) {
							n.setStrengthForNeuron(outputNeuronInstance,
									n.getOutputForNeuron(outputNeuronInstance)
											+ (step * 2));
						}
						n.setStrengthForNeuron(
								outputNeuronInstance,
								removeExtremes(n
										.getStrengthForNeuron(outputNeuronInstance)));
						n.forceTriggerStengthUpdate();
						outputNeuronInstance.forceTriggerUpdateTree();
					}

					// Do check for inherit bias of neuron
					for (Neuron outputs : suggestedValueForNeuron.keySet())
						lastOutputs.put(outputs, outputs.getTriggeredStength());
					n.setBias(n.getBias() - step);
					n.forceTriggerUpdateTree();
					double change3 = 0.0;
					for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
						// TODO: Multipled by 100 so the change is not a small
						// number.
						double sug = suggestedValueForNeuron.get(c.getKey());
						int wasRightDirection = Math.abs(sug
								- c.getKey().getTriggeredStength()) <= Math
								.abs(sug - c.getValue()) ? 1 : -1;

						change2 += wasRightDirection
								* Math.abs(c.getKey().getTriggeredStength()
										- c.getValue());

					}
					// Step > 0 is already accounted for, since we already
					// deceased the step
					if (change3 == 0.0) {
						n.setBias(n.getBias() + step);
					} else if (change3 < 0.0) {
						n.setBias(n.getBias() + (step * 2));
					}
					n.setBias(removeExtremes(n.getBias(), 50));
					n.forceTriggerUpdateTree();

					// Do Thresh checks after everything else if is triggered
					if (!(n instanceof InputNeuron)
							&& n.getThreshold() < n.getTriggeredStength()) {
						double orgThr = n.getThreshold();
						for (Neuron outputs : suggestedValueForNeuron.keySet())
							lastOutputs.put(outputs,
									outputs.getTriggeredStength());

						n.setThreshold(2);
						n.forceTriggerUpdateTree();
						double shouldIncreaseVal = 0;
						for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
							double sug = suggestedValueForNeuron
									.get(c.getKey());
							shouldIncreaseVal += (Math.abs(c.getValue() - sug) - Math
									.abs(c.getKey().getTriggeredStength() - sug));
						}
						n.setThreshold(orgThr);
						if (shouldIncreaseVal > 0)
							n.setThreshold(removeExtremes(n.getThreshold()
									+ step));
					}
				}
			}
		}
	}

	private static double removeExtremes(double d) {
		if (d > 1)
			return 1;
		if (d < -1)
			return -1;
		return d;
	}

	private static double removeExtremes(double d, double max) {
		if (d > max)
			return max;
		if (d < -max)
			return -max;
		return d;
	}
}
