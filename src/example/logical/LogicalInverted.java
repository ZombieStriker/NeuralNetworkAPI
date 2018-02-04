package example.logical;

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
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputBooleanNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Booleans;
import me.zombie_striker.neuralnetwork.util.DeepReinforcementUtil;

public class LogicalInverted extends NNBaseEntity implements Controler {

	public Sensory2D_Booleans binary = new Sensory2D_Booleans(1, 1);

	public LogicalInverted(boolean createAI) {
		this.controler = this;

		if (createAI) {
			/**
			 * If createAI is true, then generate the AI with 3 layers (i.e, 1 hidden
			 * layer), 1 input, 2 neurons, and 1 bias neuron. After that, connect all the
			 * neurons.
			 * 
			 * If you want to test using random inputs (what should be done, but makes
			 * replication and understanding a bit hander), add randomizeNeurons() after
			 * connecting then.
			 */
			this.ai = NNAI.generateAI(this, 1, 3, "output");
			for (int binaryIndex = 0; binaryIndex < 1; binaryIndex++) {
				InputBooleanNeuron.generateNeuronStatically(ai, 0, binaryIndex, this.binary);
			}

			for (int neurons = 0; neurons < 2; neurons++) {
				Neuron.generateNeuronStatically(ai, 1);
			}
			// For some reason, it needs a bias neuron at the bottom in order to work. Look
			// into it.
			BiasNeuron.generateNeuronStatically(ai, 0);

			connectNeurons();
		}
	}

	public String learn() {
		/**
		 * Simple explanation of these steps:
		 * 
		 * 1) If it is currently learning, change the input to either true or false.
		 * 
		 * 2) Let the NN tick and think. This will return the outputs from the
		 * OutpuitNeurons
		 * 
		 * 3) If it is not learning, just return the answer.
		 * 
		 * 4) Else, do the logic and see if the answer it gave (thought[0]) was correct.
		 * 
		 * 5) If it was not correct, use the DeepReinforcementUtil to improve it.
		 * 
		 * 6) After inprovement, return a message with if it was correct, the accuracy,
		 * the inputs, and what it thought was the output,
		 */
		binary.changeValueAt(0, 0, ThreadLocalRandom.current().nextBoolean());
		boolean[] thought = tickAndThink();
		boolean logic = !(binary.getBooleanAt(0, 0));
		boolean result = (logic == thought[0]);
		this.getAccuracy().addEntry(result);

		// IMPROVE IT
		HashMap<Neuron, Double> map = new HashMap<>();
		for (int i = 0; i < thought.length; i++)
			map.put(ai.getNeuronFromId(i), logic ? 1 : -1.0);
		if (!result)
			DeepReinforcementUtil.instantaneousReinforce(this, map, 1);
		return ((result ? ChatColor.GREEN : ChatColor.RED) + "acc " + getAccuracy().getAccuracyAsInt() + "|"
				+ binary.getBooleanAt(0, 0) + " ~~ " + thought[0]);
	}

	@Override
	public String update() {
		boolean[] thought = tickAndThink();
		return ("|" + binary.getBooleanAt(0, 0) + " ~~ " + thought[0]);
	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}
		if (args.length > 1) {
			boolean test = false;
			try {
				test = Boolean.parseBoolean(args[1]);
			} catch (Exception e) {
			}
			binary.changeValueAt(0, 0, test);

		} else {
			initiator.sendMessage("Provide one value (True or false)");
		}
	}

	@Override
	public NNBaseEntity clone() {
		LogicalInverted thi = new LogicalInverted(false);
		thi.ai = this.ai;
		return thi;
	}

	@Override
	public void setBase(NNBaseEntity t) {
	}

	public LogicalInverted(Map<String, Object> map) {
		super(map);
	}

}
