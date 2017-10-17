package me.zombie_striker.neuralnetwork;

import java.util.HashMap;
import java.util.Map;

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

	public NNBaseEntity() {}

	public NNBaseEntity(boolean createAI) {
		if (createAI)
			ai = new NNAI(this);
	}

	public void connectNeurons() {
		for (Neuron n : ai.getAllNeurons()) {
			if (ai.MAX_LAYERS > n.layer + 1) {
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
	}
	
	public boolean[] tickAndThink(){
		ai.tick();
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
		} else if(this instanceof Controler) {
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
