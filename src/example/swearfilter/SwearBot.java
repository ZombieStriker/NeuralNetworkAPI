package example.swearfilter;

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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.InputLetterNeuron;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Letters;
import me.zombie_striker.neuralnetwork.util.DeepReinforcementUtil;

public class SwearBot extends NNBaseEntity implements Controler {

	public static char[] letters = InputLetterNeuron.letters;
	//Returns capital letters A-Z and 0-9

	public Sensory2D_Letters word = new Sensory2D_Letters("none");
	public boolean wasCorrect = true;

	public List<String> cleanWords = new ArrayList<String>();
	public List<String> swearWords = new ArrayList<String>();

	public String filterType = "null";
	//Makes it easier to print out what each NN is testing for.

	public SwearBot(boolean createAI) {
		// Only used for my Demo system. You do not actually need this constructor for your NNs.
	}

	public SwearBot(boolean createAI, String sweartype) {
		super(false, 2000);
		/**
		 * The second value for the super is how many points are stored for
		 * accuracy. This means that the accuracy printed
		 */
		// initValidNames();

		if (createAI) {
			// Generates an ai with ONE output, which is equal to whether it is
			// a player
			this.ai = NNAI.generateAI(this, 1, 4, "Is a swear word");

			for (int index = 0; index < 16; index++) {
				for (int character = 0; character < letters.length; character++) {
					// 1st one is what index, the next is the actual character
					InputLetterNeuron.generateNeuronStatically(ai, index,
							character, this.word);
				}
			}
			/**
			 * Two layers are used for this one because there are a lot of
			 * nuances in language. Adding more hidden layers will increase the
			 * amount of variance and specificity that it will look for.
			 */
			// Creates the neurons for layer 1.
			for (int neurons = 0; neurons < 50; neurons++) 
				Neuron.generateNeuronStatically(ai, 1);
			// Creates the neurons for layer 2
			for (int neurons = 0; neurons < 15; neurons++) 
				Neuron.generateNeuronStatically(ai, 2);
			
			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);

			connectNeurons();
		}
		this.controler = this;

		this.setNeuronsPerRow(0, letters.length);

		/**
		 * Since there is no universal quality in the arrangement of all 5 swear words, each NN has to be trained for each specific swear word.
		 */
		filterType = sweartype;
		if (sweartype.equals("fuck")) {
			initValidNames(0);
		} else if (sweartype.equals("shit")) {
			initValidNames(1);
		} else if (sweartype.equals("bitch")) {
			initValidNames(2);
		} else if (sweartype.equals("cunt")) {
			initValidNames(3);
		} else if (sweartype.equals("fag")) {
			initValidNames(4);
		} else {
			initValidNames(0);
		}
	}

	public String learn() {

		boolean useSwear = ThreadLocalRandom.current().nextBoolean()
				&& ThreadLocalRandom.current().nextBoolean();
		//The booleans are doubled to make sure swear words happen 1/4 the time, as it is more important to make sure clean words are not flagged than to go over swear words.
		if (useSwear) {
			word.changeWord((String) swearWords.toArray()[(int) ((swearWords
					.size() - 1) * Math.random())]);
		} else {
			word.changeWord((String) cleanWords.toArray()[(int) ((cleanWords
					.size() - 1) * Math.random())]);
		}
		boolean result = tickAndThink()[0];
		
		boolean isswear = swearWords.contains(word.getWord());
		wasCorrect = result == isswear;
		//Returns if it was a swear word;
		
		this.getAccuracy().addEntry(wasCorrect);
		float accuracy = (float) this.getAccuracy().getAccuracy();

		// IMPROVE IT
		Neuron[] array = new Neuron[1];
		if (isswear)
			array[0] = ai.getNeuronFromId(0);
		//Instead of hashmaps, since there is only 1 output neuron, is is easier to do this instead of defining the suggested value.
		//Adding it to the array means the suggested value is +1. Not having it means it is -1.
		
		//only learn when it was not correct.
		if (!wasCorrect) {
			DeepReinforcementUtil.instantaneousReinforce(this, array, 1);
		}
		return ((wasCorrect ? ChatColor.GREEN : ChatColor.RED) + "acc "
				+ ((int) (100 * accuracy)) + "|=" + word.getWord() + "|  "
				+ result + "." + isswear + "|Swear-Score " + ((int) (100 * (ai
				.getNeuronFromId(0).getTriggeredStength()))));
	}
	
	@Override
	public String update() {
		boolean result = tickAndThink()[0];
			return "" + result;
			
	}

	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		if (this.shouldLearn) {
			initiator
					.sendMessage("Stop the learning before testing. use /nn stoplearning");
			return;
		}
		if (args.length > 1) {
			String username = "  " + args[1].toUpperCase();
			this.word.changeWord(username);
			return;
		} else {
			initiator.sendMessage("Provide an id");
		}
	}

	private void initValidNames(int i) {

		// swears
		/**
		 * Because there is not some universal element that exists for all swear
		 * words in existance, you have to narrow the search to just one swear
		 * word and its varients. For each swear word you want to filter out,
		 * you need to create and train a new NN
		 */
		if (i == 0)
			a(true, "  fuck", "  fuk", "  fuc", "  fck", "  phuc", "  phuk",
					"  fuck", "  fucccckkking", "  fuuuuck", "  fookkkkk",
					"  fuuuk", "  fuka", "  fuck", "  fuck", "  fook",
					"  foock", "  fooooking", "  fuukin", "  fuc", "  fuuukk",
					"  ffuuuuck");
		if (i == 1)
			a(true, "  shit", "  ssshhit", "  shhhiiiite", "  shite",
					"  shiiit", "  shhhhhhhiiiiiiit", "  shitty", "  shat",
					"  shaaaat", "  shart", "  shaaaaart", "  shitt",
					"  shiiit");
		if (i == 2)
			a(true, "  bitch", "  bich", "  bitch", "  btch", "  biiiitch",
					"  biiiiich", "  biatch", "  biiiaaatch", "  biiiatch",
					"  bitttttttttch", "  biiiiiiich", "  bicccch", "  bitch");
		if (i == 3)
			a(true, "  cunt", "  cuuuuuunt", "  kunt", "  cuuuuuuuunnnt",
					"  cuuunt", "  kuuuunntttt", "  kunnnnt");
		if (i == 4)
			a(true, "  fag", "  faggot", "  fagget", "  feggit", "  figgit",
					"  faaaaag", "  phagot", "  phaggot", "  phag",
					"  phaaaaag", "  phaaaget", "  faaaaagggot", "  phegot",
					"  pheggot");
		
		// Leave two spaces for beginnings of sentences. Two spaces are done so things like "Mass" does not get flagged as "Ass" simply because it does not see the letter before.

		// clean
		a(false, " mass", "mass", "  mass", "the", "world", "some", "so",
				"such", "mynameisjeff", "woah", "text", "it", "does", "not",
				"very", "  very", "clean", "  clean", "come", "  come",
				"carrot", " klingon", "  captain", " kirk", " discord",
				"funky", "  funky", "matter", "what", "i", "type", "  hello",
				"  world", "  some", "  example", "  test", " messages",
				"  of", "of", "normal", "sneak", "  sneak", "shifting",
				"  shifting", "  are", "  you", "  is", "jerky", "snowglobe",
				"canoue", "  vote", "  meat", " meet", "  idk", "  taxi",
				" booya", " bomb", "dictionairy", "coolio", " normal",
				"continue", "shihtzu", "zen", "zone", "  shihtzu", "zoo",
				"zoom", "picklerick", "  words", "  it", " can", " be",
				"anything", "greatgamegg", "anything", " anything",
				"  aything", " that", " the", " reader", " would", " read",
				" cool", " heywantto", "coming", "following", "climbing",
				"flagging", "masking", "creating", "flinging", "shining",
				"  shining", "gliding", "swimming", "swarming", "shunning",
				"grappling", "sappling", "tradeforsome", "diamonds", "gold",
				"emerald", "do", " do", "  do", "  iron", "  mass", "mass",
				" mass", " geoff", "somerandom", "lengthofstring",
				"forsomething", "op", "opis", "anythingelseyou", "wouldliketo",
				"add", "subtract", "wordassociation", "mispelword", "kik",
				"kek", "lol", "  lol", " lol", "goodjob", "Accordingtoall",
				"knownlawsofaviation", "thereisnowayabee", "shouldbeable",
				"toflyits", "wingsaretoo", "smalltogetits", "fatlittlebodyoff",
				"thegroundThe", "beeofcourse", "fliesanyway",
				"becausebeesdont", "can", "  can", "  who", "  bythepower",
				" ofgrayskull", " zombie", " mummy", " mommy", "  daddy",
				"  kiddo", "  kid", "thatsright", "atright", "hatsright",
				"right", "sright", "meetatspawn", "somebody", "oncetoldme",
				"theworldis", "bacon", "  bacon", "biscut", "  biscut",
				" biscut", "totalbiscut", "abiscut", "country", "county",
				"community", "poppop", "nevergonna", "giveyouup", "nevergonna",
				"letyoudown", "nevergonnarunaround", "anddesertyou",
				"takeabow", "carewhathumansthink", "isimpossibleYellow",
				"blackYellowblack", "YellowblackYellow", "blackOoh",
				"blackandyellow", "letsshakeitupalittle", "ifweeverwanted",
				"toachieveinterestaller", "travelsomething",
				"somethingorother", "justsomeothertext", "moretextandstuff",
				"duck", "ducking", "fudge", "fudging", "  ducking", " ducking",
				" shipping", "shipping", "  shipping", " sharp", "cup",
				"  cup", "chip", "  chip", "caesar", "thangs",
				"maybetheNNisoff", "weneedmoretesting", "morestuff", "zebealo",
				"weeboasf", "forefsase", "dsfawdsddg", "dsafGASGDSAG",
				"stuifas", "testxrtwats", "period", "  wirds", "wereecsf",
				"zzzzdgfwasf", "mooooos", "thecowgoesmpoo", "dogswoof",
				"catsmoew", "cwomoo", "moretext", "swearsstuff", "hullo",
				"halo", "wynncraft", "mineplex", "spigot", "bukkit",
				"theshotbownetwork", "network", "hivemc",
				"thisisanexampletext", "sampletext", "gateeem", "lolwat",
				"cooliokid", "Zombie_Striker", "Zombie", "Skeleton", "creeper",
				"cow", "chicken", "pig", "squid", "quack", "parrot",
				"silverfish", "fish", "redfish", "bluefish", "onefishtwofish",
				"enderman", "enderdragon", "dragon", "moretextthatisnotbad",
				"goodtext", "mrwsomethingcool", "happens", "fallout",
				"callofduty", "blackops", "liamneelson", "mattdamon",
				"hisnameisrobert", "paulson", "hisnameis", "robertpaulson",
				"fightclub", "incaseyouwerewondering", "plastic", "aluminium",
				"iron", "rock", "granite", "andersite", "obsidian", "queue",
				"joke", "batman", "superman", "aquaman", "wonderwoman",
				"spiderman", "ironman", "hulk", "ironfist", "lukecage", "thor",
				"hhhhhug", "firstlook", "quick", "quit", "  quick",
				"thisisallgoodtext", "somethingthatwouldbe",
				"seenoneveryserver", "EVERYSERVER", "nonofitis", "justrabling",
				"ofadeveloper", "losingsleep", "becauseastupid",
				"neuralnetworkiis", "refusingtowork", "withasmallsamplesize",
				"WHYWONTYOU", "FILTEROUTSWEARWORDS", "thereisnotasingle",
				"swearwordinthisblock", "oftext", "noneofthese", "sometimes",
				"sometimesyou", "  youjusthave", "  sometimes", "letters",
				"tters", "ttters", "certainwords", "whycanyou",
				"canyoujustnot", "  justnot", "whyareyoudoing", "  thistome",
				"  whyareyoudoingthis", "flipping", "  flipping", " flunky",
				"flunky", "   flunky", "  flying", "flying", "  flyying",
				"false", "  false", "  full", "  filling", " flame", "  fling",
				" flung", " flop", "flop", "  flop", "little", "lame",
				"  lame", "assassin", " assassin", "  assassin",
				"  assasinscreed", "anassassin", "gas", "  gas", "deka",
				" daca", " waterboard", "lava", "  lava", "  villager",
				"witch", "potions", " potions", "keepthisinmind",
				"learnfromthis", "justdoit", "  justdoit", "makeyourdeams",
				"  makeyourdreams", "cometrue", "  cometrue", "true", "  true",
				"helpme", "  help", "howtoraisemaxhealth", "maxhealth",
				"thechancesaresmall", "butIhaveagood", "feelingthaththis",
				"isenough", "paulwalker");

		a(false, "abcdefghijklmnopqrstuvwxyz".split(""));
		a(false,
				"  a.  b.  c.  d.  e.  f.  g.  h.  i.  j.  k.  l.  m.  n.  o.  p.  q.  r.  s.  t.  u.  v.  w.  v.  x.  y.  z"
						.split("."));
		a(false, "1234567890".split(""));

	}

	@Override
	public NNBaseEntity clone() {
		SwearBot thi = new SwearBot(false, filterType);
		thi.ai = this.ai;
		return thi;
	}

	public void setBase(NNBaseEntity t) {
		// this.base = (SwearBot) t;
	}

	public SwearBot(Map<String, Object> map) {
		super(map);
	}

	/**
	 * Adds string B to the hashmap with value true
	 * 
	 * @param b
	 */
	public void a(boolean d, String... b) {
		if (d) {
			for (String c : b)
				swearWords.add(c.toUpperCase());
		} else {
			for (String c : b)
				cleanWords.add(c.toUpperCase());
			// isSwearWord.put(c.toUpperCase(), d);
		}
	}

	public void a(boolean d, String full) {
		full = full.toUpperCase().replaceAll(" ", "").replaceAll("!", "")
				.replaceAll("?", "").replaceAll(".", "").replaceAll(",", "")
				.replaceAll("'", "").replaceAll("\"", "").replaceAll("-", "");
		int skip = 0;
		for (; skip > full.length();) {
			int size = (int) ((Math.random() * 13) + 2);
			String c = full.substring(skip, size);
			skip += size;
			if (d) {
				swearWords.add(c);
			} else {
				cleanWords.add(c);
			}
			// isSwearWord.put(c, d);
		}
	}
}
