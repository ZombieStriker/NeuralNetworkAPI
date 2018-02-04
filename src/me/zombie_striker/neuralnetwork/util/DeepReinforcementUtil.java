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
import me.zombie_striker.neuralnetwork.SIOMemoryHolder;
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
	 *            Array of all neuons that should be equal to 1. The rest will try
	 *            to be decreased to -1;
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 */
	public static void instantaneousReinforce(NNBaseEntity base, Neuron[] neuronsThatShouldBeTrue, int repetitions) {
		instantaneousReinforce(base, neuronsThatShouldBeTrue, repetitions, 0);
	}

	/**
	 * Teaches the NN for an instantaneous, constant answer regarding a single
	 * response. Useful for true-false situations
	 * 
	 * @param Base
	 *            The NeuralEntity
	 * @param neuronsThatShouldBeTrue
	 *            Array of all neuons that should be equal to 1. The rest will try
	 *            to be decreased to -1;
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 * @param chanceForSkip
	 *            The chance that a neuron will be skipped. This is done to make
	 *            sure neurons are not over specialized for each time the NN is
	 *            reinforced.
	 */
	public static void instantaneousReinforce(NNBaseEntity base, Neuron[] neuronsThatShouldBeTrue, int repetitions,
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
	 *            a Hashmap of all neurons and the values they should be equal to
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 */
	public static void instantaneousReinforce(NNBaseEntity base, HashMap<Neuron, Double> correctValues,
			int repetitions) {
		instantaneousReinforce(base, correctValues, repetitions, 0);
	}

	/**
	 * Teaches the NN for an instantaneous, constant answer regarding a single
	 * response. Useful for true-false situations
	 * 
	 * @param Base
	 *            the NeuralEntity
	 * @param correctValues
	 *            a Hashmap of all neurons and the values they should be equal to
	 * @param repetitions
	 *            the amount of repetitions for the training.
	 * @param chanceForSkip
	 *            The chance that a neuron will be skipped. This is done to make
	 *            sure neurons are not over specialized for each time the NN is
	 *            reinforced.
	 * 
	 */
	public static void instantaneousReinforce(NNBaseEntity base, HashMap<Neuron, Double> correctValues, int repetitions,
			double chanceForSkip) {
		final double step = 0.01;

		for (int loops = 0; loops < repetitions; loops++) {
			HashMap<Neuron, Double> suggestedValueForNeuron = correctValues;
			// Subtract -1 a second time to not get the outputs
			// for (int layer = base.ai.MAX_LAYERS - 1 - 1; layer >= 0; layer--)
			// {
			for (int layer = 0; layer < base.ai.maxlayers - 1; layer++) {
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
							lastOutputs.put(outputs, outputs.getTriggeredStength());

						if (!(n instanceof InputNeuron) && n.getTriggeredStength() <= n.getThreshold()) {
							n.setThreshold(-2);
							n.forceTriggerUpdateTree();
							double shouldLowerVal = 0;
							for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
								double sug = suggestedValueForNeuron.get(c.getKey());
								if (c.getValue() > c.getKey().getThreshold()
										|| c.getKey().getTriggeredStength() > c.getKey().getThreshold()
										|| c.getKey().getTriggeredStength() > c.getValue())
									shouldLowerVal += (Math.abs(c.getValue() - sug)
											- Math.abs(c.getKey().getTriggeredStength() - sug));
							}
							n.setThreshold(orgThr);
							if (shouldLowerVal > 0) {
								n.setThreshold(removeExtremes(n.getThreshold() - step, n.allowNegativeValues()));
							}
						}
						continue;
					}

					for (Neuron outputs : suggestedValueForNeuron.keySet())
						lastOutputs.put(outputs, outputs.getTriggeredStength());

					int multiplier = Math.random() > 0.5 ? -1 : 1;

					n.setWeight(n.getWeight() + (step * multiplier));
					n.forceTriggerUpdateTree();
					double change2 = 0.0;
					for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
						// TODO: Multipled by 100 so the change is not a small
						// number.
						double sug = suggestedValueForNeuron.get(c.getKey());
						int wasRightDirection = Math.abs(sug - c.getKey().getTriggeredStength()) <= Math
								.abs(sug - c.getValue()) ? 1 : -1;

						if (c.getValue() > c.getKey().getThreshold()
								|| c.getKey().getTriggeredStength() > c.getKey().getThreshold()
								|| c.getKey().getTriggeredStength() > c.getValue())
							change2 += wasRightDirection * Math.abs(c.getKey().getTriggeredStength() - c.getValue());
						/*
						 * change2 += ((Math .abs((c.getKey().getTriggeredStength() * 100) -
						 * (c.getValue() * 100))) * (Math .abs((suggestedValueForNeuron.get(c.getKey())
						 * - c .getKey().getTriggeredStength())) <= Math
						 * .abs(suggestedValueForNeuron.get(c.getKey()) - c.getValue()) ? 1.0 : -1.0));
						 */

					}

					if (change2 == 0.0) {
						n.setWeight(n.getWeight() - (step * multiplier));
					} else if (change2 < 0.0) {
						n.setWeight(n.getWeight() - (step * 2 * multiplier));
					}
					n.setWeight(removeExtremes(n.getWeight(), n.allowNegativeValues()));
					n.forceTriggerUpdateTree();

					for (int outputNeuronId : n.getStrengthIDs()) {
						Neuron outputNeuronInstance = n.getAI().getNeuronFromId(outputNeuronId);

						for (Neuron outputs : suggestedValueForNeuron.keySet())
							lastOutputs.put(outputs, outputs.getTriggeredStength());

						int multiplier2 = Math.random() > 0.5 ? -1 : 1;
						n.setStrengthForNeuron(outputNeuronInstance,
								n.getOutputForNeuron(outputNeuronInstance) + (step * multiplier2));
						n.forceTriggerStengthUpdate();
						outputNeuronInstance.forceTriggerUpdateTree();
						double change = 0.0;
						for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
							double sug = suggestedValueForNeuron.get(c.getKey());
							int wasRightDirection = (Math.abs(sug - c.getKey().getTriggeredStength()) <= Math
									.abs(sug - c.getValue()) ? 1 : -1);

							if (c.getValue() > c.getKey().getThreshold()
									|| c.getKey().getTriggeredStength() > c.getKey().getThreshold()
									|| c.getKey().getTriggeredStength() > c.getValue())
								change += wasRightDirection * Math.abs(c.getKey().getTriggeredStength() - c.getValue());
							/*
							 * change += ((Math.abs((c.getKey() .getTriggeredStength() * 100) -
							 * (c.getValue() * 100))) * (Math .abs((suggestedValueForNeuron.get(c .getKey())
							 * - c.getKey() .getTriggeredStength())) <= Math
							 * .abs(suggestedValueForNeuron.get(c.getKey()) - c.getValue()) ? 1.0 : -1.0));
							 */
						}

						if (change == 0.0) {
							n.setStrengthForNeuron(outputNeuronInstance,
									n.getOutputForNeuron(outputNeuronInstance) - (step * multiplier2));
						} else if (change < 0.0) {
							n.setStrengthForNeuron(outputNeuronInstance,
									n.getOutputForNeuron(outputNeuronInstance) - (step * 2 * multiplier2));
						}
						n.setStrengthForNeuron(outputNeuronInstance,
								removeExtremes(n.getStrengthForNeuron(outputNeuronInstance), n.allowNegativeValues()));
						n.forceTriggerStengthUpdate();
						outputNeuronInstance.forceTriggerUpdateTree();
					}

					// Do check for inherit bias of neuron
					for (Neuron outputs : suggestedValueForNeuron.keySet())
						lastOutputs.put(outputs, outputs.getTriggeredStength());
					int multiplier3 = Math.random() > 0.5 ? -1 : 1;
					n.setBias(n.getBias() + (step * multiplier3));
					n.forceTriggerUpdateTree();
					double change3 = 0.0;
					for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
						// TODO: Multipled by 100 so the change is not a small
						// number.
						double sug = suggestedValueForNeuron.get(c.getKey());
						int wasRightDirection = Math.abs(sug - c.getKey().getTriggeredStength()) <= Math
								.abs(sug - c.getValue()) ? 1 : -1;

						if (c.getValue() > c.getKey().getThreshold()
								|| c.getKey().getTriggeredStength() > c.getKey().getThreshold()
								|| c.getKey().getTriggeredStength() > c.getValue())
							change2 += wasRightDirection * Math.abs(c.getKey().getTriggeredStength() - c.getValue());

					}
					// Step > 0 is already accounted for, since we already
					// deceased the step
					if (change3 == 0.0) {
						n.setBias(n.getBias() - (step * multiplier3));
					} else if (change3 < 0.0) {
						n.setBias(n.getBias() - (step * 2 * multiplier3));
					}
					n.setBias(removeExtremes(n.getBias(), 50, n.allowNegativeValues()));
					n.forceTriggerUpdateTree();

					// Do Thresh checks after everything else if is triggered
					if (!(n instanceof InputNeuron) && n.getThreshold() < n.getTriggeredStength()) {
						double orgThr = n.getThreshold();
						for (Neuron outputs : suggestedValueForNeuron.keySet())
							lastOutputs.put(outputs, outputs.getTriggeredStength());

						n.setThreshold(2);
						n.forceTriggerUpdateTree();
						double shouldIncreaseVal = 0;
						for (Entry<Neuron, Double> c : lastOutputs.entrySet()) {
							double sug = suggestedValueForNeuron.get(c.getKey());
							if (c.getValue() > c.getKey().getThreshold()
									|| c.getKey().getTriggeredStength() > c.getKey().getThreshold()
									|| c.getKey().getTriggeredStength() > c.getValue())
								shouldIncreaseVal += (Math.abs(c.getValue() - sug)
										- Math.abs(c.getKey().getTriggeredStength() - sug));
						}
						n.setThreshold(orgThr);
						if (shouldIncreaseVal > 0)
							n.setThreshold(removeExtremes(n.getThreshold() + step, n.allowNegativeValues()));
					}
				}
			}
		}
	}

	/**
	 * Teaches the NN for fluid, nonlinear responses given a set of multiple
	 * scenarios. Good for cases where you know true/false responses for certain
	 * cases, but not for all cases.
	 * 
	 * The reason this is done per-neuron is to allow for percentages to print out,
	 * or the ability to cancel the training if needed.
	 * 
	 * There will be no random checks for values. Values can sometimes get stuck in
	 * local minimums with no way out.
	 * 
	 * @param Base
	 *            the NeuralEntity
	 * @param n
	 *            The neuron that will be trained
	 * @param scenarios
	 *            The scenarios, which contain the inputs and the suggested output.
	 * 
	 */
	public static void multiScenarioReinforceNeuron(NNBaseEntity base, Neuron n, List<SIOMemoryHolder> scenarios) {
		multiScenarioReinforceNeuron(base, n, scenarios, 0.00);
	}

	/**
	 * Teaches the NN for fluid, nonlinear responses given a set of multiple
	 * scenarios. Good for cases where you know true/false responses for certain
	 * cases, but not for all cases.
	 * 
	 * The reason this is done per-neuron is to allow for percentages to print out,
	 * or the ability to cancel the training if needed.
	 * 
	 * @param Base
	 *            the NeuralEntity
	 * @param n
	 *            The neuron that will be trained
	 * @param scenarios
	 *            The scenarios, which contain the inputs and the suggested output.
	 * @param chanceForRandomValueCheck
	 *            The chance (between 0.0 and 1.0) for the values for a neuron to be
	 *            set to a random value and tested. Good for getting out of local
	 *            minimums and finding better results
	 * 
	 */
	public static void multiScenarioReinforceNeuron(NNBaseEntity base, Neuron n, List<SIOMemoryHolder> scenarios,
			double chanceForRandomValueCheck) {
		final double step = 0.01;
		// Subtract -1 a second time to not get the outputs
		// for (int layer = base.ai.MAX_LAYERS - 1 - 1; layer >= 0; layer--)
		// {
		// Since no new outputs are created, nor are any destroyed, we
		// do not need to clear it: simply adding new values will
		// override the existing vals.

		// Do thresh checks before if is not triggered

		// Do thresholds after everything else
		n.setIsTraining(true);

		// if (!n.isTriggered()) {
		// TODO: Test if correct: Thresholds will not check inputs, however, inputs
		// should not be skipped.

		if (n.useThreshold()) {
			double orgThr = n.getThreshold();
			if (!(n instanceof InputNeuron)) {
				// Temporary check: If it is a bias neuron, reduce the threshold and invert the
				// weight
				// inverting weight should mean that whatever problem is was having before will
				// now be 'fixed'
				if (n instanceof BiasNeuron && (n.getThreshold() >= 0.5)) {
					n.setWeight(-n.getWeight());
					n.setThreshold(0.3);
				}

				recordOutputs(base, n, scenarios);
				n.setThreshold(-2);
				double difference = returnDifference(base, n, scenarios);
				n.setThreshold(orgThr);
				if (difference > 0) {
					n.setThreshold(removeExtremes(n.getThreshold() - step, n.allowNegativeValues()));
				} else {
					recordOutputs(base, n, scenarios);
					n.setThreshold(2);
					double difference4 = returnDifference(base, n, scenarios);
					n.setThreshold(orgThr);
					if (difference4 > 0) {
						n.setThreshold(removeExtremes(n.getThreshold() + step, n.allowNegativeValues()));
					}
				}
			} else {
				if (!everGetsActivated(n, scenarios)) {
					n.setIsTraining(false);
					return;
				}
			}
		}
		// }
		


		if (!(n instanceof InputNeuron || n instanceof BiasNeuron)) {
			// Do check for inherit bias of neuron
			recordOutputs(base, n, scenarios);
			if (Math.random() <= chanceForRandomValueCheck) {

				double prevBoas = n.getBias();
				// Sets the bias between -50 and +50
				if (n.allowNegativeValues())
					n.setBias((Math.random() * 4) - 2);
				else
					n.setBias((Math.random() * 2));
				double difference3 = returnDifference(base, n, scenarios);
				if (difference3 <= 0.0)
					n.setBias(prevBoas);
				n.setBias(removeExtremes(n.getBias(), 2, n.allowNegativeValues()));

			} else {
				int multiplier3 = Math.random() > 0.5 ? -1 : 1;
				n.setBias(n.getBias() + (step * multiplier3));
				double difference3 = returnDifference(base, n, scenarios);
				if (difference3 == 0.0) {
					n.setBias(n.getBias() - (step * multiplier3));
				} else if (difference3 < 0.0) {
					n.setBias(n.getBias() - (step * 2 * multiplier3));
				}
				n.setBias(removeExtremes(n.getBias(), 2, n.allowNegativeValues()));
			}
		}

		recordOutputs(base, n, scenarios);

		if (Math.random() <= chanceForRandomValueCheck) {

			double prevWeight = n.getWeight();
			if (n.allowNegativeValues())
				n.setWeight((Math.random() * 2) - 1);
			else
				n.setWeight((Math.random()));
			double difference = returnDifference(base, n, scenarios);
			if (difference <= 0.0)
				n.setWeight(prevWeight);
			n.setWeight(removeExtremes(n.getWeight(), n.allowNegativeValues()));

		} else {
			int multiplier = Math.random() > 0.5 ? -1 : 1;
			n.setWeight(n.getWeight() + (step * multiplier));
			double difference = returnDifference(base, n, scenarios);
			if (difference == 0.0) {
				n.setWeight(n.getWeight() - (step * multiplier));
			} else if (difference < 0.0) {
				n.setWeight(n.getWeight() - (step * 2 * multiplier));
			}
			n.setWeight(removeExtremes(n.getWeight(), n.allowNegativeValues()));
		}

		for (int outputNeuronId : n.getStrengthIDs()) {
			Neuron outputNeuronInstance = n.getAI().getNeuronFromId(outputNeuronId);
			
			if(outputNeuronInstance.droppedOut())
				continue;

			recordOutputs(base, n, scenarios);

			if (Math.random() <= chanceForRandomValueCheck) {

				double prevStength = n.getStrengthForNeuron(outputNeuronInstance);
				if (n.allowNegativeValues())
					n.setStrengthForNeuron(outputNeuronId, (Math.random() * 2) - 1);
				else
					n.setStrengthForNeuron(outputNeuronId, (Math.random()));
				double difference2 = returnDifference(base, n, scenarios);
				if (difference2 <= 0.0)
					n.setStrengthForNeuron(outputNeuronId, prevStength);
				n.setStrengthForNeuron(outputNeuronInstance,
						removeExtremes(n.getStrengthForNeuron(outputNeuronInstance), n.allowNegativeValues()));
			} else {
				int multiplier2 = Math.random() > 0.5 ? -1 : 1;
				n.setStrengthForNeuron(outputNeuronInstance,
						n.getOutputForNeuron(outputNeuronInstance) + (step * multiplier2));
				double difference2 = returnDifference(base, n, scenarios);

				if (difference2 == 0.0) {
					n.setStrengthForNeuron(outputNeuronInstance,
							n.getOutputForNeuron(outputNeuronInstance) - (step * multiplier2));
				} else if (difference2 < 0.0) {
					n.setStrengthForNeuron(outputNeuronInstance,
							n.getOutputForNeuron(outputNeuronInstance) - (step * 2 * multiplier2));
				}
				n.setStrengthForNeuron(outputNeuronInstance,
						removeExtremes(n.getStrengthForNeuron(outputNeuronInstance), n.allowNegativeValues()));
			}
		}
		n.setIsTraining(false);
	}

	public static boolean everGetsActivated(Neuron n, List<SIOMemoryHolder> scenarios) {
		for (SIOMemoryHolder mem : scenarios) {
			if (everGetsActivated(n, mem))
				return true;
		}
		return false;
	}

	public static boolean everGetsActivated(Neuron n, SIOMemoryHolder scenario) {
		if (scenario.needsToUse())
			return true;
		if (n.getLayer() > 0)
			return true;
		if (n instanceof BiasNeuron)
			if (n.getThreshold() < 0.5)
				return true;
			else
				return false;
		if (scenario.inputValues.containsKey(n.getID())) {
			double newVal = scenario.inputValues.get(n.getID());
			if (newVal > 0)
				return true;
		}
		return false;
	}

	public static void recordOutputs(NNBaseEntity base, Neuron testFor, List<SIOMemoryHolder> scenarios) {

		for (SIOMemoryHolder mem : scenarios) {
			if (!everGetsActivated(testFor, mem))
				continue;
			for (Neuron inN : base.ai.getInputNeurons()) {
				if (mem.inputValues.containsKey(inN.getID())) {
					if (inN instanceof BiasNeuron)
						continue;
					double newVal = mem.inputValues.get(inN.getID());
					inN.forceLastResultChange(newVal);
					((InputNeuron) inN).setIsTriggeredLast(newVal > 0);
				}
			}
			base.ai.forceStengthUpdateForNeuronsInAndAbove(1);

			HashMap<Integer, Double> outputs = new HashMap<>();

			for (Neuron n2 : base.ai.getOutputNeurons()) {
				outputs.put(n2.getID(), n2.getTriggeredStength());
			}
			mem.updatePreviousOutputs(outputs);
		}
	}

	public static double returnDifference(NNBaseEntity base, Neuron testFor, List<SIOMemoryHolder> scenarios) {
		double difference = 0;
		for (SIOMemoryHolder mem : scenarios) {
			if (!everGetsActivated(testFor, mem))
				continue;
			for (Neuron inN : base.ai.getInputNeurons()) {
				if (inN instanceof BiasNeuron)
					continue;
				double newVal = mem.inputValues.get(inN.getID());
				inN.forceLastResultChange(newVal);
				((InputNeuron) inN).setIsTriggeredLast(newVal > 0);
			}
			base.ai.forceStengthUpdateForNeuronsInAndAbove(1);

			HashMap<Integer, Double> currentoutputs = new HashMap<>();
			for (Neuron n2 : base.ai.getOutputNeurons()) {
				currentoutputs.put(n2.getID(), n2.getTriggeredStength());
			}

			for (Entry<Integer, Double> suggested : mem.suggestOutputValues.entrySet()) {
				// TODO: Check if correct: By adding or subtracting 1, sqaring the number should
				// never create a smaller number
				double deltaOrg = (suggested.getValue() - mem.previousOutputValues.get(suggested.getKey()));
				if (deltaOrg < 0)
					deltaOrg--;
				else
					deltaOrg++;
				double deltaDifference = (suggested.getValue() - currentoutputs.get(suggested.getKey()));
				if (deltaDifference < 0)
					deltaDifference--;
				else
					deltaDifference++;

				difference += (deltaOrg * deltaOrg) - (deltaDifference * deltaDifference);

				// TODO: Check if correct: Switching val make it positive, but on average
				// further than needed

				// difference += (suggested.getValue() -
				// mem.previousOutputValues.get(suggested.getKey()))
				// - (suggested.getValue() - currentoutputs.get(suggested.getKey()));
			}
		}
		return difference;
	}

	private static double removeExtremes(double d, boolean allowNeg) {
		if ((!allowNeg) && d < 0)
			return 0;
		if (d > 1)
			return 1;
		if (d < -1)
			return -1;
		return d;
	}

	private static double removeExtremes(double d, double max, boolean allowNeg) {
		if ((!allowNeg) && d < 0)
			return 0;
		if (d > max)
			return max;
		if (d < -max)
			return -max;
		return d;
	}
}
