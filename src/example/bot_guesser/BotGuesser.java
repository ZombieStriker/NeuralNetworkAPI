package example.bot_guesser;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputLetterNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Letters;
import me.zombie_striker.neuralnetwork.util.DeepReinformentUtil;


public class BotGuesser extends NNBaseEntity implements Controler{


	public static char[] letters = {'A','B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '_' };

	public Sensory2D_Letters word = new Sensory2D_Letters("none");

	public BotGuesser base;

	public boolean wasCorrect = true;

	public HashMap<String, Boolean> ifNameIsValid = new HashMap<>();

	
	public BotGuesser(boolean createAI) {
		super(false);
		this.base = this;
		initValidNames();

		//this.senses.add(word);
		
		if (createAI) {
			//Generates an ai with ONE output, which is equal to whether it is a player
			this.ai = NNAI.generateAI(this, 1,3,"Is a real player");

			for (int index = 0; index < 16; index++) {
				for (int character = 0; character < letters.length; character++) {
					// 1st one is what index, the next is the actual character
					InputLetterNeuron.generateNeuronStatically(ai, index,
							letters[character],this.word);
				}
			}
			//Creates the neurons for layer 1.
			for (int neurons = 0; neurons < 28; neurons++) {
				Neuron.generateNeuronStatically(ai, 1);
			}
			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);

			connectNeurons();
		}
		this.controler = this;
		
		this.setNeuronsPerRow(0,letters.length);
	}

	@Override
	public String update() {
		if (shouldLearn) {
			this.word
					.changeWord((String) ifNameIsValid.keySet().toArray()[(int) ((ifNameIsValid
							.keySet().size() - 1) * Math.random())]);
		}
		base.ai.tick();
		boolean result = base.ai.think()[0];

		boolean ishuman = false;
		float accuracy = 0;
		if (shouldLearn) {
			ishuman = ifNameIsValid.get(base.word.getWord());
			this.getAccuracy().addEntry(result == ishuman);
			accuracy = (float) this.getAccuracy().getAccuracy();
		}
		if (!shouldLearn) {
			return (
					(result ? ChatColor.DARK_GREEN : ChatColor.DARK_RED)
							+ "|="
							+ base.word.getWord()
							+ "|  "
							+ result
							+ "|Human-Score "
							+ ((int) (100 * (base.ai.getNeuronFromId(0)
									.getTriggeredStength()))));

		} else {
			wasCorrect = result == ishuman;

			// IMPROVE IT
			Neuron[] array = new Neuron[1];
			if (ishuman)
				array[0] = base.ai.getNeuronFromId(0);
			DeepReinformentUtil.instantaneousReinforce(base, array,
					(wasCorrect ? 1 : 3));
			return(
					(result == ishuman ? ChatColor.GREEN : ChatColor.RED)
							+ "acc "
							+ ((int) (100 * accuracy))
							+ "|="
							+ base.word.getWord()
							+ "|  "
							+ result
							+ "|Human-Score "
							+ ((int) (100 * (base.ai.getNeuronFromId(0)
									.getTriggeredStength()))));
		}
	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}
		if (args.length > 1) {
			String username = args[1];

			this.word
					.changeWord(username);
			return;
		} else {
			initiator.sendMessage("Provide an id");
		}
	}

	private void initValidNames() {

		// Player names
		a("Zombie_Striker", "kermit_23", "Notch", "xephos", "lividcoffee",
				"dinnerbone", "timtowers", "bfwalshy", "nvider", "kittengirl",
				"Cooldude", "Meowgirl", "blabeblade", "coolio3000",
				"aintnobodygottime", "cablebox", "Iamhopingyou", "terminator",
				"gizmo", "snake", "mario", "theotherbrother", "theyellowone",
				"peach", "yoshi", "otheryoshi", "sparticus", "neo", "gooby",
				"loopy", "hewhoshallnot", "benamed", "harrypotter",
				"ronweeezl", "hermione", "true", "false", "almond", "putin",
				"the_donald", "donut", "lab_guy100", "Killer", "healer",
				"p90x", "L3375p33k", "up_arrow", "down_arrow", "up", "down",
				"left", "right", "foward", "back", "move", "look", "booster",
				"batman", "joker", "arandomname", "isthisavailable",
				"itseemsitis", "iamgod", "thelegend27", "iamtheone",
				"headphone", "idontwantittothink", "thatesareagood", "thing",
				"thang", "ThangsNStuff", "Zombie_killer", "Weezard", "towny",
				"worldedit", "lobbyapi", "vault", "vaultboy", "chestprotect",
				"anticheatplus", "anticheataulta", "multiworld", "protcollib",
				"waitIhavetoLetThisthing", "Runforallthese", "options",
				"mickey_mouse", "goofy", "pluto", "zues", "WhyDoInotlike_",
				"_xxSlayerxx_", "Cringelord", "whatamIDoing", "Ineedtostop",
				"someonesendhelp", "ihavenotbeendoingthis", "forthatlong",
				"portal", "glados", "shell", "chell", "space_core",
				"cake_core", "fact_core", "imanerd", "nerd", "cave_johnson",
				"causeimapotatoe", "lemons", "damnyoulemons", "burninglemons",
				"wheat", "seeds", "corn", "plow", "someshortword",
				"somelongword", "something", "idrk", "itsjustsomething",
				"aretheserealnames", "whywouldtheypickthis",
				"whyareYOUreadingthis", "youcanjuststophere",
				"thereisnothingelse", "thatwillbeinteresting",
				"afterthispoint", "itsnotlikeim", "tired", "omg", "stopit",
				"noreally", "cashmeoutside", "howaboutdat", "allhailhypnotoad",
				"hypnotoad", "thinkthisistoolong", "ishouldstophere", "ornot",
				"notlikeanyonewillsee", "this", "wink_Wink", "goodjob",
				"youmadeit", "to_the_end", "hereissomecake", "the_cake_is_alie");

		// Bot names (the chance that one of them will be an actual name is too
		// slim to take into account)
		for (int i = 0; i < ifNameIsValid.size(); i++) {
			StringBuilder sb = new StringBuilder();
			int size = (int) (3 + (13 * Math.random()));
			for (int letters = 0; letters < size; letters++) {
				sb.append(BotGuesser.letters[(int) (BotGuesser.letters.length * Math
						.random())]);
			}
			ifNameIsValid.put(sb.toString(), false);
		}
	}

	@Override
	public NNBaseEntity clone() {
		BotGuesser thi = new BotGuesser(false);
		thi.ai = this.ai;
		return thi;
	}

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
