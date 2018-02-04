package example.bot_guesser;

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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputLetterNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Letters;
import me.zombie_striker.neuralnetwork.util.DeepReinforcementUtil;

public class BotGuesser extends NNBaseEntity implements Controler {

	/**
	 * This bot checks to see if a user name is a "real account" based on its
	 * username. Gibberish or random usernames will return false.
	 */

	public static char[] letters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };

	public Sensory2D_Letters word = new Sensory2D_Letters("none");

	public BotGuesser base;

	public boolean wasCorrect = true;

	public HashMap<String, Boolean> ifNameIsValid = new HashMap<>();

	public BotGuesser(boolean createAI) {
		super(false);
		this.base = this;
		initValidNames();

		if (createAI) {
			// Generates an ai with ONE output, which is equal to whether it is
			// a player
			this.ai = NNAI.generateAI(this, 1, 4, "Is a real player");

			for (int index = 0; index < 16; index++) {
				for (int character = 0; character < letters.length; character++) {
					// 1st one is what index, the next is the actual character
					InputLetterNeuron.generateNeuronStatically(ai, index, letters[character], this.word);
				}
			}
			// Creates the neurons for layer 1.
			for (int neurons = 0; neurons < 30; neurons++)
				Neuron.generateNeuronStatically(ai, 1);
			// Create neurons for layer 2
			for (int neurons = 0; neurons < 20; neurons++)
				Neuron.generateNeuronStatically(ai, 2);

			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);

			connectNeurons();
		}
		this.controler = this;

		this.setNeuronsPerRow(0, letters.length);
	}

	public String learn() {
		this.word.changeWord(
				(String) ifNameIsValid.keySet().toArray()[(int) ((ifNameIsValid.keySet().size() - 1) * Math.random())]);
		boolean result = tickAndThink()[0];
		boolean ishuman = ifNameIsValid.get(base.word.getWord());
		this.getAccuracy().addEntry(result == ishuman);
		float accuracy = (float) this.getAccuracy().getAccuracy();
		wasCorrect = result == ishuman;

		// IMPROVE IT
		Neuron[] array = new Neuron[1];
		if (ishuman)
			array[0] = base.ai.getNeuronFromId(0);
		DeepReinforcementUtil.instantaneousReinforce(base, array, (wasCorrect ? 1 : 3));
		return ((result == ishuman ? ChatColor.GREEN : ChatColor.RED) + "acc " + ((int) (100 * accuracy)) + "|="
				+ base.word.getWord() + "|  " + result + "|Human-Score "
				+ ((int) (100 * (base.ai.getNeuronFromId(0).getTriggeredStength()))));
	}

	@Override
	public String update() {
		/**
		 * 1) If it should learn, select a random entry from the map. Based on the way
		 * we propogate the map, it has a 50% chance of being a valid name.
		 * 
		 * 2) Tick and think.
		 * 
		 * 3) If it is not learning, return if it was correct, the word used, and its
		 * "score".
		 * 
		 * 4) else, check if the name was a real name, and compare if the NN gave the
		 * same answer
		 * 
		 * 5) if it did not, improve it.
		 */
		boolean result = tickAndThink()[0];

		return ((result ? ChatColor.DARK_GREEN : ChatColor.DARK_RED) + "|=" + base.word.getWord() + "|  " + result
				+ "|Human-Score " + ((int) (100 * (base.ai.getNeuronFromId(0).getTriggeredStength()))));

	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}
		if (args.length > 1) {
			String username = args[1];

			this.word.changeWord(username);
			return;
		} else {
			initiator.sendMessage("Provide an id");
		}
	}

	private void initValidNames() {

		// This is just a small sample. The more valid names you give it, the
		// more accurate it will be.

		// Player names
		a("Zombie_Striker", "kermit_23", "Notch", "xephos", "lividcoffee", "dinnerbone", "timtowers", "bfwalshy",
				"nvider", "kittengirl", "Cooldude", "Meowgirl", "blabeblade", "coolio3000", "aintnobodygottime",
				"cablebox", "Iamhopingyou", "terminator", "gizmo", "snake", "mario", "theotherbrother", "theyellowone",
				"peach", "yoshi", "otheryoshi", "sparticus", "neo", "gooby", "loopy", "hewhoshallnot", "benamed",
				"harrypotter", "ronweeezl", "hermione", "true", "false", "almond", "putin", "the_donald", "donut",
				"lab_guy100", "Killer", "healer", "p90x", "L3375p33k", "up_arrow", "down_arrow", "up", "down", "left",
				"right", "foward", "back", "move", "look", "booster", "batman", "joker", "arandomname",
				"isthisavailable", "itseemsitis", "iamgod", "thelegend27", "iamtheone", "headphone",
				"idontwantittothink", "thatesareagood", "thing", "thang", "ThangsNStuff", "Zombie_killer", "Weezard",
				"towny", "worldedit", "lobbyapi", "vault", "vaultboy", "chestprotect", "anticheatplus",
				"anticheataulta", "multiworld", "protcollib", "waitIhavetoLetThisthing", "Runforallthese", "options",
				"mickey_mouse", "goofy", "pluto", "zues", "WhyDoInotlike_", "_xxSlayerxx_", "Cringelord",
				"whatamIDoing", "Ineedtostop", "someonesendhelp", "ihavenotbeendoingthis", "forthatlong", "portal",
				"glados", "shell", "chell", "space_core", "cake_core", "fact_core", "imanerd", "nerd", "cave_johnson",
				"causeimapotatoe", "lemons", "damnyoulemons", "burninglemons", "wheat", "seeds", "corn", "plow",
				"someshortword", "somelongword", "something", "idrk", "itsjustsomething", "aretheserealnames",
				"whywouldtheypickthis", "whyareYOUreadingthis", "youcanjuststophere", "thereisnothingelse",
				"thatwillbeinteresting", "afterthispoint", "itsnotlikeim", "tired", "omg", "stopit", "noreally",
				"cashmeoutside", "howaboutdat", "allhailhypnotoad", "hypnotoad", "thinkthisistoolong",
				"ishouldstophere", "ornot", "notlikeanyonewillsee", "this", "wink_Wink", "goodjob", "youmadeit",
				"to_the_end", "hereissomecake", "the_cake_is_alie");

		// Bot names (the chance that one of them will be an actual name is too
		// slim to take into account)
		// Generates a name from 3 to 16 characters.
		for (int i = 0; i < ifNameIsValid.size(); i++) {
			StringBuilder sb = new StringBuilder();
			int size = (int) (3 + (13 * Math.random()));
			for (int letters = 0; letters < size; letters++) {
				sb.append(BotGuesser.letters[(int) (BotGuesser.letters.length * Math.random())]);
			}
			ifNameIsValid.put(sb.toString(), false);
		}
	}

	@Override
	public NNBaseEntity clone() {
		BotGuesser clone = new BotGuesser(false);
		clone.ai = this.ai.clone(clone);
		return clone;
	}

	// Not needed, since the controler is in the same class as the base.
	public void setBase(NNBaseEntity t) {
		this.base = (BotGuesser) t;
	}

	/**
	 * Adds string B to the hashmap with value true
	 * 
	 * @param b
	 */
	public void a(String... b) {
		for (String c : b)
			ifNameIsValid.put(c, true);

	}
}
