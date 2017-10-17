package me.zombie_striker.neuralnetwork.util;

import java.util.*;
import java.util.Map.Entry;

import me.zombie_striker.neuralnetwork.NNBaseEntity;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputNeuron;

public class DeepReinformentUtil {

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
		instantaneousReinforce(base, h, repetitions);
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
		final double step = 0.01;

		for (int loops = 0; loops < repetitions; loops++) {
			HashMap<Neuron, Double> suggestedValueForNeuron = correctValues;
			// Subtract -1 a second time to not get the outputs
			for (int layer = base.ai.MAX_LAYERS - 1 - 1; layer >= 0; layer--) {
				for (Neuron n : base.ai.getNeuronsInLayer(layer)) {
					HashMap<Neuron, Double> currentValsThre = new HashMap<>();
					for (Neuron outputs : suggestedValueForNeuron.keySet()) {
						currentValsThre
								.put(outputs, outputs.getTriggeredStength());
					}
					double orgThr = n.getThreshold();
					if (!n.isTriggered()) {
						if (!(n instanceof InputNeuron))
							if(n.getTriggeredStength() <= n.getThreshold()){
								n.setThreshold(-2);
								n.forceTriggerUpdateTree();
								double shouldLowerVal =0;
								for (Entry<Neuron, Double> c : currentValsThre.entrySet()) {
									double sug = suggestedValueForNeuron.get(c.getKey());
									shouldLowerVal += (Math.abs(c.getValue()-sug)-Math.abs(c.getKey().getTriggeredStength()-sug));
								}
								n.setThreshold(orgThr);
								if(shouldLowerVal>0){
									n.setThreshold(n.getThreshold()-step);
								}
							}
							continue;
					}else{
						if (!(n instanceof InputNeuron))
						if(n.getTriggeredStength() > n.getThreshold()){
							n.setThreshold(2);
							n.forceTriggerUpdateTree();
							double shouldIncreaseVal =0;
							for (Entry<Neuron, Double> c : currentValsThre.entrySet()) {
								double sug = suggestedValueForNeuron.get(c.getKey());
								shouldIncreaseVal += (Math.abs(c.getValue()-sug)-Math.abs(c.getKey().getTriggeredStength()-sug));
							}
							n.setThreshold(orgThr);
							if(shouldIncreaseVal>0){
								n.setThreshold(n.getThreshold()+step);
							}
						}
					}

					HashMap<Neuron, Double> currentVals2 = new HashMap<>();
					for (Neuron outputs : suggestedValueForNeuron.keySet()) {
						currentVals2
								.put(outputs, outputs.getTriggeredStength());
					}
					n.setWeight(n.getWeight() - (step));
					n.forceTriggerUpdateTree();
					double change2 = 0.0;
					for (Entry<Neuron, Double> c : currentVals2.entrySet()) {
						// TODO: Multipled by 100 so the change is not a small
						// number.
						int wasRightDirection = (Math
								.abs((suggestedValueForNeuron.get(c.getKey()) - c
										.getKey().getTriggeredStength())) <= Math
								.abs(suggestedValueForNeuron.get(c.getKey())
										- c.getValue()) ? 1 : -1);

						change2 += wasRightDirection
								* (Math.abs((c.getKey().getTriggeredStength() * 100)
										- (c.getValue() * 100)));
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
						n.setWeight(n.getWeight() + (step));
					} else if (change2 < 0.0) {
						n.setWeight(n.getWeight() + (step * 2));
					}
					n.setWeight(removeExtremes(n.getWeight()));
					n.forceTriggerUpdateTree();

					for (int outputNeuronId : n.getStrengthIDs()) {
						Neuron outputNeuronInstance = n.getAI()
								.getNeuronFromId(outputNeuronId);

						HashMap<Neuron, Double> currentVals = new HashMap<>();
						for (Neuron outputs : suggestedValueForNeuron.keySet()) {
							currentVals.put(outputs,
									outputs.getTriggeredStength());
						}
						n.setStrengthForNeuron(outputNeuronInstance,
								n.getOutputForNeuron(outputNeuronInstance)
										- (step));
						n.forceTriggerStengthUpdate();
						outputNeuronInstance.forceTriggerUpdateTree();
						double change = 0.0;
						for (Entry<Neuron, Double> c : currentVals.entrySet()) {
							int wasRightDirection = (Math
									.abs((suggestedValueForNeuron.get(c
											.getKey()) - c.getKey()
											.getTriggeredStength())) <= Math
									.abs(suggestedValueForNeuron.get(c.getKey())
											- c.getValue()) ? 1 : -1);

							change += wasRightDirection
									* (Math.abs((c.getKey()
											.getTriggeredStength() * 100)
											- (c.getValue() * 100)));
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
}
