package me.zombie_striker.nnmain;

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


import java.io.File;
import java.io.IOException;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.*;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
@Deprecated
public class Save_Config{

	public static NNBaseEntity loadn312n(Plugin main, NNBaseEntity base, String id) {
		ConfigurationSection nids = main.getConfig().getConfigurationSection(
				"NeuralNetworks." + id + ".neurons");
		NNBaseEntity ent = base;

		int cId = main.getConfig().getInt("NeuralNetworks." + id + ".id");
		int layers = main.getConfig().getInt("NeuralNetworks." + id + ".layers");
		
		String senses = main.getConfig().getString("NeuralNetworks." + id + ".senses");
		int i = 0;
		for(String s : senses.split("\\|")){
			if(s.startsWith("vision")){
		//		ent.senses.add(new Sensory2D_Numbers(Integer.parseInt(s.split(",")[1]),i));
				i++;
			}else if (s.startsWith("letters")){
		//		ent.senses.add(new Sensory2D_Letters("config",i));
				i++;
			}
		}
		

		NNAI ai = new NNAI(ent, cId, false, layers);
		ent.ai = ai;
		for (String nID : nids.getKeys(false)) {
			String data = main.getConfig().getString(
					"NeuralNetworks." + id + ".neurons" + /* "." + aIID + */"."
							+ nID);
			String[] bits = data.split(",");
			Neuron n = null;
			//TODO: Make this configurable
			
			
			if (bits[0].startsWith("omn")) {
				String[] splits = bits[0].split("\\|");
				int l = Integer.parseInt(splits[1]);
				n = new OutputNeuron(ai, Integer.parseInt(splits[2]), l);
			} else if (bits[0].startsWith("imn")) {
				String[] splits = bits[0].split("\\|");
			//	int l = Integer.parseInt(splits[1]);
				int x = Integer.parseInt(splits[2]);
				int y = Integer.parseInt(splits[3]);
		//		int sens_id = Integer.parseInt(splits[4]);
	//			n = new InputMobNeuron(ai, x, y,(Sensory2D_Numbers) ent.senses.get(sens_id));
			} else if (bits[0].startsWith("ibn")) {
				String[] splits = bits[0].split("\\|");
			//	int l = Integer.parseInt(splits[1]);
				int x = Integer.parseInt(splits[2]);
				int y = Integer.parseInt(splits[3]);
				int sens_id = Integer.parseInt(splits[4]);
		//		n = new InputBlockNeuron(ai, x, y,(Sensory2D_Numbers) ent.senses.get(sens_id));
			} else if (bits[0].startsWith("iln")) {
				String[] splits = bits[0].split("\\|");
			//	int l = Integer.parseInt(splits[1]);
				int x = Integer.parseInt(splits[2]);
				char y = splits[3].charAt(0);
				int sens_id = Integer.parseInt(splits[4]);
		//		n = new InputLetterNeuron(ai, x, y,(Sensory2D_Letters) ent.senses.get(sens_id));
			} else if (bits[0].startsWith("bn")) {
				String[] splits = bits[0].split("\\|");
				int l = Integer.parseInt(splits[1]);
				n = new BiasNeuron(ai, l);
			} else if (bits[0].startsWith("n")) {
				String[] splits = bits[0].split("\\|");
				int l = Integer.parseInt(splits[1]);
				n = new Neuron(ai, l);
			} else
				continue;
			
			if (bits.length >= 2) {
				for (String input : bits[1].split("\\|")) {
				//	if (input.length() > 0)
		//		/		n.getInputs().add(Integer.parseInt(input));
				}
			}
			if (bits.length >= 3) {
				for (String output : bits[2].split("\\|")) {
				//	if (output.length() > 0)
		//				n.getOutputs().add(Integer.parseInt(output));
				}
			}

			if (bits.length >= 4) {
				if (bits[3].length() > 0)
					n.setWeight(Double.parseDouble(bits[3]));
			}
			if (bits.length >= 5) {
				String[] entries = bits[4].split("\\|");
				for (String entries2 : entries) {
					if (entries2.length() > 0)
						n.setStrengthForNeuron(Integer.parseInt(entries2.split("M")[0]),
								Double.parseDouble(entries2.split("M")[1]));
				}
			}

		}
		return ent;
	}

	public static void savennokld(Plugin main,NNBaseEntity e, String id) {
		main.getConfig().set("NeuralNetworks." + id + ".layers", e.ai.maxlayers);
		
		StringBuilder sb_sens = new StringBuilder();
		
		main.getConfig().set("NeuralNetworks." + id + ".senses", sb_sens.toString());

		for (Neuron n : e.getAI().getAllNeurons()) {
			StringBuilder sb = new StringBuilder();
			if (n == null) {
				continue;
			} else if (n instanceof OutputNeuron) {
				sb.append("omn|" + n.layer + "|"
						+ ((OutputNeuron) n).responceid + ",");
			} else if (n instanceof InputMobNeuron) {
				sb.append("imn|" + n.layer + "|"
						+ ((InputNeuron) n).xlink + "|"
						+ ((InputNeuron) n).ylink + ",");
			} else if (n instanceof InputLetterNeuron) {
				sb.append("iln|" + n.layer + "|"
						+ ((InputNeuron) n).xlink + "|"
						+ ((InputLetterNeuron) n).letter + ",");
			} else if (n instanceof InputBlockNeuron) {
				sb.append("ibn|" + n.layer + "|"
						+ ((InputNeuron) n).xlink + "|"
						+ ((InputBlockNeuron) n).ylink +",");
			} else if (n instanceof BiasNeuron) {
				sb.append("bn|" + n.layer + ",");
			} else if (n instanceof Neuron) {
				sb.append("n|" + n.layer + ",");
			}
		//	for (Integer i : n.getInputs()) {
		//		sb.append(i + "|");
		//	}
			sb.append(",");
		//	for (Integer i : n.getOutputs()) {
		//		sb.append(i + "|");
		//	}
			// Add strength, and OS
			sb.append(",");
			sb.append(n.getWeight());
			sb.append(",");
			for (Integer ee : n.getStrengthIDs()) {
				sb.append(ee + "M" + n.getStrengthForNeuron(ee) + "|");
			}

			main.getConfig().set("NeuralNetworks." + id + ".neurons." + n.getID(),
					sb.toString());
		}
		main.saveConfig();
	}
}