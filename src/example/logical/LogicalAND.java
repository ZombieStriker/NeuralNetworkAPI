package example.logical;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputBooleanNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Booleans;
import me.zombie_striker.neuralnetwork.util.DeepReinformentUtil;

public class LogicalAND extends NNBaseEntity implements Controler {

	public Sensory2D_Booleans binary = new Sensory2D_Booleans(1, 2);

	public LogicalAND(boolean createAI) {
		this.controler = this;

		if (createAI) {
			this.ai = NNAI.generateAI(this, 1, 3, "output");
			for (int binaryIndex = 0; binaryIndex < 2; binaryIndex++) {
				InputBooleanNeuron.generateNeuronStatically(ai, 0, binaryIndex,
						this.binary);
			}

			for (int neurons = 0; neurons < 3; neurons++) {
				Neuron.generateNeuronStatically(ai, 1);
			}
			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);

			connectNeurons();
		}
	}

	@Override
	public String update() {
		if (shouldLearn) {
			binary.changeValueAt(0, 0,
					ThreadLocalRandom.current().nextInt(2) == 0);
			binary.changeValueAt(0, 1,
					ThreadLocalRandom.current().nextInt(2) == 0);
		}

		boolean[] thought = tickAndThink();

		if (!shouldLearn)
			return ("|" + binary.getBooleanAt(0, 0) + " + "
					+ binary.getBooleanAt(0, 1) + " ~~ " + thought[0]);

		boolean logic = (binary.getBooleanAt(0, 0) && binary.getBooleanAt(0, 1));
		boolean result = logic == thought[0];
		this.getAccuracy().addEntry(result);

		// IMPROVE IT
		HashMap<Neuron, Double> map = new HashMap<>();
		for (int i = 0; i < thought.length; i++) 
			map.put(ai.getNeuronFromId(i), logic ? 1 : -1.0);		
		if (!result)
			DeepReinformentUtil.instantaneousReinforce(this, map, 1);
		return ((result ? ChatColor.GREEN : ChatColor.RED) + "acc "
				+ getAccuracy().getAccuracyAsInt() + "|"
				+ binary.getBooleanAt(0, 0) + " + " + binary.getBooleanAt(0, 1)
				+ " ~~ " + thought[0]);

	}


	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator
					.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}
		if (args.length > 2) {
			boolean test = false;
			try {
				test = Boolean.parseBoolean(args[1]);
			} catch (Exception e) {
			}
			boolean test2 = false;
			try {
				test2 = Boolean.parseBoolean(args[2]);
			} catch (Exception e) {
			}
			binary.changeValueAt(0, 0, test);
			binary.changeValueAt(0, 1, test2);

		} else {
			initiator.sendMessage("Provide two values (True or false)");
		}
	}

	@Override
	public NNBaseEntity clone() {
		LogicalAND thi = new LogicalAND(false);
		thi.ai = this.ai;
		return thi;
	}

	@Override
	public void setBase(NNBaseEntity t) {
	}

	public LogicalAND(Map<String, Object> map) {
		super(map);
	}

}
