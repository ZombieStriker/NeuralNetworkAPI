package me.zombie_striker.neuralnetwork;

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
import java.util.Random;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.util.Accuracy;

public class NNBaseEntity implements ConfigurationSerializable {

	public NNAI ai;

	public Controler controler;
	public boolean shouldLearn = false;

	// TODO: Default accuracy is 500. This makes sure slight changes to the
	// amount of correct answers should not affect the total accuracy that much
	Accuracy accuracy = new Accuracy(500);

	public Accuracy getAccuracy() {
		return accuracy;
	}

	public NNBaseEntity() {
	}

	public NNBaseEntity(boolean createAI) {
		if (createAI)
			ai = new NNAI(this);
		accuracy = new Accuracy(500);
	}

	public NNBaseEntity(boolean createAI, int accuracyCheck) {
		if (createAI)
			ai = new NNAI(this);
		accuracy = new Accuracy(accuracyCheck);
	}

	public void connectNeurons() {
		for (Neuron n : ai.getAllNeurons()) {
			connectNeuron(n);
		}
	}

	public void connectNeuron(Neuron n) {
		if (ai.maxlayers > n.layer + 1) {
			// n.setWeight((Math.random() * 2) - 1);
			// n.setWeight(0.1);
			for (Neuron output : ai.getNeuronsInLayer(n.layer + 1)) {
				if (output instanceof BiasNeuron)
					continue;
				// n.getOutputs().add(output.getID());
				// output.getInputs().add(n.getID());
				// n.setStrengthForNeuron(output, (Math.random() * 2) - 1);
				n.setStrengthForNeuron(output, 0);
			}
		}
	}

	/**
	 * In case the neuron needs more neurons after it has already been trained, this
	 * will add the connection from previous layers.
	 * 
	 * @param newNeuron
	 *            - the new neuron that was added.
	 */
	public void backPropNeuronConnections(Neuron newNeuron, boolean randomize) {
		if (newNeuron.getLayer() > 0) {
			for (Neuron n : getAI().getNeuronsInLayer(newNeuron.getLayer() - 1)) {
				if (n.allowNegativeValues())
					n.setStrengthForNeuron(newNeuron, randomize ? (Math.random() * 2) - 1 : 0.1);
				else
					n.setStrengthForNeuron(newNeuron, randomize ? Math.random() : 0.1);
			}
		}
	}

	public void randomizeNeurons() {
		for (Neuron n : ai.getAllNeurons()) {
			randomizeNeuron(n);
		}
	}

	public void randomizeNeuron(Neuron n) {
		if (ai.maxlayers > n.layer + 1) {
			if (n.allowNegativeValues()) {
				n.setWeight((Math.random() * 2) - 1);
				n.setThreshold((Math.random() * 2) - 1);
			} else {
				n.setWeight((Math.random()));
				n.setThreshold((Math.random()));
			}
			for (Neuron output : ai.getNeuronsInLayer(n.layer + 1)) {
				if (n.allowNegativeValues()) 
				n.setStrengthForNeuron(output, (Math.random() * 2) - 1);
				else
					n.setStrengthForNeuron(output, (Math.random()));
			}
		}
	}

	public boolean[] tickAndThink() {
		return ai.think();
	}

	public NNBaseEntity clone() {
		return null;
	}

	public Controler getControler() {
		return controler;
	}

	public boolean shouldLearn() {
		return shouldLearn;
	}

	public void setShouldLearn(boolean b) {
		shouldLearn = b;
	}

	public void setNeuronsPerRow(int row, int amount) {
		getAI().setNeuronsPerRow(row, amount);
	}

	public int getNeuronsPerRow(int row) {
		return getAI().getNeuronsPerRow(row);
	}

	public NNAI getAI() {
		return ai;
	}

	public NNBaseEntity(Map<String, Object> map) {
		this.ai = (NNAI) map.get("ai");
		this.ai.entity = this;
		if (map.containsKey("c")) {
			this.controler = (Controler) map.get("c");
		} else if (this instanceof Controler) {
			this.controler = (Controler) this;
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		if (this.controler != this)
			m.put("c", controler);
		m.put("ai", ai);
		return m;
	}
}
