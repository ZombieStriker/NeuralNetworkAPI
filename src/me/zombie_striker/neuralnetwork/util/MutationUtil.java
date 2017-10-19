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


public class MutationUtil {
	//TODO: Commenting out, as no one should use it:
	
	/*
	private static final double MUTATION_CHANCE = 0.0;
	//TODO: Go back to 0.3. Right now, no mutations

	private  static final double NEURON_SUICIDE_CHANCE = 0.25;
	private  static final double NEURON_CREATION_CHANCE = 0.30;
	// Drop them from 0.20 each to .10 so the total for either change is 0.20
	private static final double NEURON_CONNECTION_MUTATION_CHANCE = 0.10;
	private  static final double NEURON_CONNECTION_CREATION_MUTATION_CHANCE = 0.10;
	private  static final double NEURON_CONNECTION_SUICIDE_MUTATION_CHANCE = 0.10;
	private  static final double NEURON_LOCATION_X_MUTATION_CHANCE = 0.10;
	private  static final double NEURON_LOCATION_Y_MUTATION_CHANCE = 0.10;
	private  static final double NEURON_THRESHOLD_MUTATION_CHANCE = 0.10;
	private  static final double NEURON_OUTPUT_STRENGTH_MUTATION_CHANCE = 0.10;
	private  static final double CROSSOVER_DISJOINT_MUTATION_CHANCE = 0.20;
	
	private  static boolean CREATE_INPUT_NEURONS = false;
	private  static boolean CHANGE_XY_NEURONS = false;
	private  static boolean CREATE_NEURONS = false;

	// For the roll section
	private static final double[] ARRAY_CONNECTION_CHANCE = new double[] {
			NEURON_CONNECTION_MUTATION_CHANCE,
			NEURON_CONNECTION_CREATION_MUTATION_CHANCE,
			NEURON_CONNECTION_SUICIDE_MUTATION_CHANCE };

	public static boolean rollCrossoverMutation() {
		return Math.random() > 1 - CROSSOVER_DISJOINT_MUTATION_CHANCE;
	}

	public static void rollSuicide(NNAI ai) {
		if (Math.random() > 1 - NEURON_SUICIDE_CHANCE) {
			for (int i = 0; i < 10; i++) {
				// Ten chances to kill a neuron and see if it
				// survives.
				int neuron = getObjectByChance((ai.allNeurons.size() - 1));
				if (ai.allNeurons.get(neuron) != null
						&& !(ai.allNeurons.get(neuron) instanceof OutputNeuron)) {
					Bukkit.broadcastMessage("Neuron kill mutation!");
					Neuron toRemove = ai.allNeurons.get(neuron);
					for (Neuron input : ai.getNeuronsFromId(toRemove.getInputs())) {
						input.getOutputs().remove(toRemove);
					}
					for (Neuron output : ai.getNeuronsFromId(toRemove.getOutputs())) {
						output.getInputs().remove(toRemove);
					}
					toRemove.getInputs().clear();
					toRemove.getOutputs().clear();
					ai.allNeurons.put(toRemove.getID(), null);
					ai.getNeuronsInLayer(toRemove.layer).remove(toRemove);
					break;
				}
			}
		}
	}

	private static int pickOnOf(int entires) {
		return (int) (Math.random() * entires);
	}

	public static void rollCreate(NNAI ai) {
		if (Math.random() > 1 - NEURON_CREATION_CHANCE && CREATE_NEURONS) {
			Neuron n = null;
			int pick = pickOnOf(2);
			if(!CREATE_INPUT_NEURONS){
			n = Neuron.generateNeuronStatically(ai,
					(int) (Math.random() * (ai.MAX_LAYERS - 2)) + 1);
			}
			
			//We should not need to generate input neurons. All input neurons should already be there.
			
			/*switch (pick) { // 3 = cases
			case 0:
				n = Neuron.generateNeuronStatically(ai,
						(int) (Math.random() * (Layer.MAX_LAYERS - 2)) + 1);
				break;

			case 1:
				n = InputMobNeuron.generateNeuronStatically(ai);
				break;
			case 2:
				n = InputBlockNeuron.generateNeuronStatically(ai);
				break;
			}*
			if (n != null) {
				Bukkit.broadcastMessage("Neuron creation mutation! Type= "
						+ pick + ". Neuron count=" + ai.allNeurons.size());
			}
		}
	}

	/**
	 * Mode 0 = Regular Mutation change
	 * 
	 * Mode 1 = Add connection
	 * 
	 * Mode 2 = Remove connection
	 * 
	 * @param ai
	 * @param modeToTestFor
	 *
	public static void rollInOutMutationChance(NNAI ai, int modeToTestFor) {

		boolean shouldgo = false;
		if (Math.random() > 1 - ARRAY_CONNECTION_CHANCE[modeToTestFor])
			shouldgo = true;

		if (shouldgo) {
			Neuron n = null;
			for (int tries = 0; tries < 10; tries++) {
				n = ai.allNeurons
						.get(getObjectByChance((ai.allNeurons.size() - 1)));
				if (n != null)
					break;
			}
			Bukkit.broadcastMessage("Neuron connection "
					+ (modeToTestFor == 1 ? "addtion"
							: (modeToTestFor == 2 ? "removal" : "change"))
					+ " mutation!");
			if (modeToTestFor == 0 || modeToTestFor == 2) {
				if (n.getInputs().size() > 0) {
					for (int i = 0; i < 5; i++) {
						int chance = getObjectByChance((n.getInputs().size() - 1));
						if (n.getInputs().toArray()[chance] != n
								&& n.getInputs().toArray()[chance] != null) {
							n.getInputs().remove((Neuron) ai.allNeurons.get(chance));
							((Neuron) ai.allNeurons.get(chance)).getOutputs()
									.remove(n);
							break;
						}
					}
				}
			}
			if (modeToTestFor == 0 || modeToTestFor == 1) {
				// Neuron connection = n;// set to n just for the for loop
				// for (int i = 0; connection == n && connection != null
				// && i < 100; i++) {
				// // connection = (Neuron) ai.allNeurons
				// .get(getObjectByChance((ai.allNeurons.size() - 1)));

				// Only add a connection to the layer before, or after.

				boolean addToLastLayer = Math.random() > 0.5;
				addConnections(n, addToLastLayer);

				// }
				// n.input.add(connection.id);
				// connection.output.add(n.id);
			}
		}
	}

	public static void addConnections(Neuron n, boolean addToLastLayer) {
		Layer connectionLayer = n.getAI().layers.get(n.layer
				+ (addToLastLayer ? -1 : +1));
		if(connectionLayer==null)return;
		if(connectionLayer.neuronsInLayer.size()==0)return;
		Neuron randomNeuron = connectionLayer.neuronsInLayer.get((int) (Math
				.random() * connectionLayer.neuronsInLayer.size()));
		if (addToLastLayer) {
			n.getInputs().add(randomNeuron.getID());
			randomNeuron.getOutputs().add(n.getID());
		} else {
			n.getOutputs().add(randomNeuron.getID());
			randomNeuron.getInputs().add(n.getID());
		}
	}

	public static void rollXYChange(NNAI ai, boolean isY) {
		if(!CHANGE_XY_NEURONS)return;
		boolean shouldgo = false;
		if (isY) {
			if (Math.random() > 1 - NEURON_LOCATION_Y_MUTATION_CHANCE)
				shouldgo = true;
		} else {
			if (Math.random() > 1 - NEURON_LOCATION_X_MUTATION_CHANCE)
				shouldgo = true;
		}
		if (shouldgo) {
			// Do not do these mutation just yet.
			InputNeuron inputNeuron = null;
			int tries = 0;
			while (inputNeuron == null && tries < 100) {
				tries++;
				int neuron = getObjectByChance(ai.allNeurons.size() - 1);
				Neuron temp = (Neuron) ai.allNeurons.get(neuron);
				if (temp instanceof InputNeuron) {
					inputNeuron = (InputNeuron) temp;
				}
			}
			if (inputNeuron != null) {
				for (int i = 0; i < 5; i++) {
					if (isY) {
					//TODO:	inputNeuron.ylink = getObjectByChance(((inputNeuron.entitiesVision.viewdistance * 2) + 1));
					//	if (inputNeuron.ylink < inputNeuron.entitiesVision.universe.length)
					//		break;
					} else {
					//	inputNeuron.xlink = getObjectByChance(((inputNeuron.entitiesVision.viewdistance * 2) + 1));
					//	if (inputNeuron.xlink < inputNeuron.entitiesVision.universe.length)
					//		break;
					}
				}
				Bukkit.broadcastMessage("Neuron xlink mutation!");
			}

		}
	}

	public static void rollThresholdChange(NNAI ai) {
		if (Math.random() > 1 - NEURON_THRESHOLD_MUTATION_CHANCE) {
			Neuron neuron = null;
			int tries = 0;
			while (neuron == null && tries < 100) {
				tries++;
				int neuroni = getObjectByChance(ai.allNeurons.size() - 1);
				neuron = (Neuron) ai.allNeurons.get(neuroni);
			}
			if (neuron != null) {
			//TODO:	neuron.threshold = Math.random();
				Bukkit.broadcastMessage("Neuron threshold mutation!");
			}
		}
	}

	public static void rollOutputSignalChange(NNAI ai) {
		if (Math.random() > 1 - NEURON_OUTPUT_STRENGTH_MUTATION_CHANCE) {
			Neuron neuron = null;
			int tries = 0;
			while (neuron == null && tries < 100) {
				tries++;
				int neuroni = getObjectByChance(ai.allNeurons.size() - 1);
				neuron = (Neuron) ai.allNeurons.get(neuroni);
			}
			if (neuron != null) {
				//TODO:neuron.outputStength = (Math.random() * 2) - 1;
				Bukkit.broadcastMessage("Neuron signal output mutation!");
			}
		}
	}

	public static void mutateGene(NNAI ai) {
		boolean first = true;

		while (first || Math.random() > 1 - MUTATION_CHANCE) {
			first = false;
			rollSuicide(ai);
			rollCreate(ai);
			rollOutputSignalChange(ai);
			rollThresholdChange(ai);
			for (int i = 0; i < ARRAY_CONNECTION_CHANCE.length; i++)
				rollInOutMutationChance(ai, i);
			for (int i = 0; i < 2; i++)
				rollXYChange(ai, i == 1);
		}
	}

	public static int getObjectByChance(int range) {
		return (int) ((Math.random() * range) + 0.5);
	}
*/
}
