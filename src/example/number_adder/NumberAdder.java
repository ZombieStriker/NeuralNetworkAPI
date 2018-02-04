package example.number_adder;

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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputBooleanNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Booleans;
import me.zombie_striker.neuralnetwork.util.DeepReinforcementUtil;

public class NumberAdder extends NNBaseEntity implements Controler {

	public Sensory2D_Booleans binary = new Sensory2D_Booleans(4, 10);

	public NumberAdder base;

	private final int max_bytes = 4;

	public NumberAdder(boolean createAI) {
		base = this;

		if (createAI) {
			this.ai = NNAI.generateAI(this, max_bytes + 1, 4, "1", "2", "4", "8", "16", "32", "64", "128", "256", "512",
					"1024");

			for (int trueOrFalse = 0; trueOrFalse < 4; trueOrFalse++) {
				for (int binaryIndex = 0; binaryIndex < max_bytes; binaryIndex++) {
					InputBooleanNeuron.generateNeuronStatically(ai, trueOrFalse, binaryIndex, this.binary);
				}
			}

			for (int neurons = 0; neurons < 50; neurons++) {
				Neuron.generateNeuronStatically(ai, 1);
			}
			for (int neurons = 0; neurons < 40; neurons++) {
				Neuron.generateNeuronStatically(ai, 2);
			}
			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);

			connectNeurons();
			setNeuronsPerRow(0, max_bytes);
		}
		this.controler = this;
	}

	public String learn() {
		boolean[] bbb = numberToBinaryBooleans((int) (Math.random() * (Math.pow(2, max_bytes))));
		boolean[] bbb2 = numberToBinaryBooleans((int) (Math.random() * (Math.pow(2, max_bytes))));
		for (int i = 0; i < bbb.length; i++) {
			this.binary.changeValueAt(0, i, (bbb[i]));
			((NumberAdder) base).binary.changeValueAt(1, i, (!bbb[i]));
		}
		for (int i = 0; i < bbb2.length; i++) {
			((NumberAdder) base).binary.changeValueAt(2, i, (bbb2[i]));
			((NumberAdder) base).binary.changeValueAt(3, i, (!bbb2[i]));
		}
		boolean[] thought = tickAndThink();

		boolean[] booleanBase = new boolean[10];
		for (int i = 0; i < 10; i++) {
			booleanBase[i] = base.binary.getBooleanAt(0, i);
		}

		boolean[] booleanBase2 = new boolean[10];
		for (int i = 0; i < 10; i++) {
			booleanBase2[i] = base.binary.getBooleanAt(2, i);
		}
		int number = binaryBooleansToNumber(booleanBase);
		int number2 = binaryBooleansToNumber(booleanBase2);
		int number3 = binaryBooleansToNumber(thought);

		boolean result = number + number2 == number3;
		boolean[] correctvalues = numberToBinaryBooleans(number + number2);

		this.getAccuracy().addEntry(result);

		StringBuilder sb = new StringBuilder();
		int amountOfMistakes = 0;
		for (int i = 0; i < Math.max(correctvalues.length, thought.length); i++) {
			if (i < thought.length && thought[i]) {
				sb.append((correctvalues.length > i && correctvalues[i] ? ChatColor.DARK_GREEN : ChatColor.DARK_RED)
						+ "+" + ((int) Math.pow(2, i)));
				if (!(correctvalues.length > i && correctvalues[i]))
					amountOfMistakes++;
			} else if (i < correctvalues.length && correctvalues[i]) {
				sb.append(ChatColor.GRAY + "+" + ((int) Math.pow(2, i)));
				amountOfMistakes++;
			}
		}

		// IMPROVE IT
		HashMap<Neuron, Double> map = new HashMap<>();
		for (int i = 0; i < thought.length; i++) {
			map.put(base.ai.getNeuronFromId(i), correctvalues.length > i && correctvalues[i] ? 1 : -1.0);
		}

		// amountOfMistakes = (int) Math.pow(2,amountOfMistakes);
		if (!result)
			DeepReinforcementUtil.instantaneousReinforce(base, map, amountOfMistakes);
		return ((result ? ChatColor.GREEN : ChatColor.RED) + "acc " + getAccuracy().getAccuracyAsInt() + "|" + number
				+ " + " + number2 + " = " + number3 + "|  " + sb.toString());
	}

	@Override
	public String update() {
		boolean[] thought = tickAndThink();

		boolean[] booleanBase = new boolean[10];
		for (int i = 0; i < 10; i++) {
			booleanBase[i] = base.binary.getBooleanAt(0, i);
		}

		boolean[] booleanBase2 = new boolean[10];
		for (int i = 0; i < 10; i++) {
			booleanBase2[i] = base.binary.getBooleanAt(2, i);
		}
		int number = binaryBooleansToNumber(booleanBase);
		int number2 = binaryBooleansToNumber(booleanBase2);
		int number3 = binaryBooleansToNumber(thought);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < thought.length; i++) {
			if (thought[i]) {
				sb.append("+" + ((int) Math.pow(2, i)));
			}
		}
		Bukkit.getConsoleSender().sendMessage("Byte Values = " + sb.toString());
		return ("|" + number + " + " + number2 + " ~~ " + number3);
	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}

		if (args.length > 2) {
			int test = 0;
			try {
				test = Integer.parseInt(args[1]);
			} catch (Exception e) {
				return;
			}
			int test2 = 0;
			try {
				test2 = Integer.parseInt(args[2]);
			} catch (Exception e) {
				return;
			}
			int tempnumber = test;
			for (int power = 9; power >= 0; power--) {
				if (tempnumber - Math.pow(2, power) >= 0) {
					this.binary.changeValueAt(0, power, true);
					this.binary.changeValueAt(1, power, false);
					tempnumber -= Math.pow(2, power);
				} else {
					this.binary.changeValueAt(0, power, false);
					this.binary.changeValueAt(1, power, true);
				}
			}
			int tempnumber2 = test2;
			for (int power = 9; power >= 0; power--) {
				if (tempnumber2 - Math.pow(2, power) >= 0) {
					this.binary.changeValueAt(2, power, true);
					this.binary.changeValueAt(3, power, false);
					tempnumber2 -= Math.pow(2, power);
				} else {
					this.binary.changeValueAt(2, power, false);
					this.binary.changeValueAt(3, power, true);
				}
			}

		} else {
			initiator.sendMessage("Provide two numbers to add");
		}

	}

	@Override
	public NNBaseEntity clone() {
		NumberAdder thi = new NumberAdder(false);
		thi.ai = this.ai;
		return thi;
	}

	@Override
	public void setBase(NNBaseEntity t) {
		this.base = (NumberAdder) t;
	}

	private boolean[] numberToBinaryBooleans(int i) {
		int mathlog = 0;
		for (int k = 0; k < 20; k++) {
			if (Math.pow(2, k) > i) {
				mathlog = k;
				break;
			}
		}
		boolean[] k = new boolean[mathlog];

		int tempnumber = i;
		for (int power = mathlog - 1; power >= 0; power--) {
			if (tempnumber - Math.pow(2, power) >= 0) {
				k[power] = true;
				tempnumber -= Math.pow(2, power);

			} else {
				k[power] = false;
			}
		}
		return k;
	}

	private int binaryBooleansToNumber(boolean[] b) {
		int k = 0;
		for (int i = 0; i < b.length; i++) {
			if (b[i])
				k += Math.pow(2, i);
		}
		return k;
	}

}
