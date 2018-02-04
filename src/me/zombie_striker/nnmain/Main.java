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
import java.util.*;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.*;
import me.zombie_striker.neuralnetwork.senses.*;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import example.blackjack_helper.BlackJackHelper;
import example.bot_guesser.BotGuesser;
import example.logical.*;
import example.music_bot.MusicBot;
import example.number_adder.NumberAdder;
import example.prime_number_guesser.PrimeNumberBot;
import example.swearfilter.ExampleSwearListener;
import example.swearfilter.SwearBot;

public class Main extends JavaPlugin implements Listener {

	/**
	 * This class is used to make a Neural Network figure out whether a username
	 * is valid
	 */
	public void onLoad() {
		ConfigurationSerialization.registerClass(NNBaseEntity.class);
		ConfigurationSerialization.registerClass(NNAI.class);
		ConfigurationSerialization.registerClass(Layer.class);
		ConfigurationSerialization.registerClass(Senses.class);
		ConfigurationSerialization.registerClass(Controler.class);

		ConfigurationSerialization.registerClass(Senses2D.class);
		ConfigurationSerialization.registerClass(Senses3D.class);
		ConfigurationSerialization.registerClass(Sensory2D_Booleans.class);
		ConfigurationSerialization.registerClass(Sensory2D_Letters.class);
		ConfigurationSerialization.registerClass(Sensory2D_Numbers.class);
		ConfigurationSerialization.registerClass(Sensory3D_Booleans.class);
		ConfigurationSerialization.registerClass(Sensory3D_Numbers.class);

		ConfigurationSerialization.registerClass(Neuron.class);
		ConfigurationSerialization.registerClass(InputNeuron.class);
		ConfigurationSerialization.registerClass(InputBlockNeuron.class);
		ConfigurationSerialization.registerClass(InputBooleanNeuron.class);
		ConfigurationSerialization.registerClass(InputLetterNeuron.class);
		ConfigurationSerialization.registerClass(InputNumberNeuron.class);
		ConfigurationSerialization.registerClass(InputTickNeuron.class);
		ConfigurationSerialization.registerClass(OutputNeuron.class);
		ConfigurationSerialization.registerClass(BiasNeuron.class);

		ConfigurationSerialization.registerClass(LogicalAND.class);
		ConfigurationSerialization.registerClass(LogicalOR.class);
		ConfigurationSerialization.registerClass(LogicalXOR.class);
		ConfigurationSerialization.registerClass(LogicalXNOR.class);
		ConfigurationSerialization.registerClass(LogicalNAND.class);
		ConfigurationSerialization.registerClass(LogicalInverted.class);
		ConfigurationSerialization.registerClass(LogicalNOR.class);

		ConfigurationSerialization.registerClass(BlackJackHelper.class);
		ConfigurationSerialization.registerClass(NumberAdder.class);
		ConfigurationSerialization.registerClass(BotGuesser.class);
		ConfigurationSerialization.registerClass(PrimeNumberBot.class);
		ConfigurationSerialization.registerClass(MusicBot.class);
		ConfigurationSerialization.registerClass(SwearBot.class);
	}

	private FileConfiguration config;
	private File f = new File(getDataFolder(), "NNData.yml");

	/**
	 * If you are creating your own plugin using the NNAPI, do not use the
	 * default NN. You should have your own class.
	 */
	private NeuralNetwork nn;

	protected static Main plugin;

	protected boolean enableMetrics = true;

	@Override
	public void onDisable() {
		plugin = null;
		if (getNn() != null && getNn().getGrapher() != null)
			getNn().closeGrapher();
		clearAllRegisteredClasses();
	}

	@Override
	public void onEnable() {
		// TODO: Remove these values. They were only needed back when the NNs
		// did not implement ConfigurationSerializable
		registerDemoEntity(BlackJackHelper.class);
		registerDemoEntity(PrimeNumberBot.class);
		registerDemoEntity(NumberAdder.class);
		registerDemoEntity(MusicBot.class);
		registerDemoEntity(BotGuesser.class);
		registerDemoEntity(SwearBot.class);
		registerDemoEntity(LogicalInverted.class);
		registerDemoEntity(LogicalOR.class);
		registerDemoEntity(LogicalAND.class);
		registerDemoEntity(LogicalXOR.class);
		registerDemoEntity(LogicalNAND.class);
		registerDemoEntity(LogicalNOR.class);
		registerDemoEntity(LogicalXNOR.class);

		nn = new NeuralNetwork(this);
		plugin = this;

		config = YamlConfiguration.loadConfiguration(f);
		Bukkit.getPluginManager().registerEvents(
				new ExampleSwearListener(getNn()), this);

		if (!getConfig().contains("enableStats")) {
			getConfig().set("enableStats", true);
			saveConfig();
		}
		enableMetrics = getConfig().getBoolean("enableStats");

		//new Updater(this, 280241);
		GithubUpdater.autoUpdate(this, "ZombieStriker","NeuralNetworkAPI","NeuralNetworkAPI.jar");

		if (Bukkit.getPluginManager().getPlugin("PluginConstructorAPI") == null)
			// new DependencyDownloader(this, 276723);
			GithubDependDownloader.autoUpdate(this,
					new File(getDataFolder().getParentFile(), "PluginConstructorAPI.jar"), "ZombieStriker",
					"PluginConstructorAPI", "PluginConstructorAPI.jar");
		/**
		 * Everyone should want the most up to date version of the plugin, so
		 * any improvements made (either with performance or through new
		 * methods) should be welcome. Since it is rare that I will remove
		 * anything, and even if I did, I would deprecate the methods for a long
		 * period of time before I do, nothing should really break.
		 */
		if (enableMetrics) {
			/**
			 * I use bStats metrics to monitor how many servers are using my
			 * API. This does not send any personal/private information. This
			 * only sends:
			 * 
			 * the server version, Java version, 
			 * the plugin's version, 
			 * system architecture,
			 * Core count,
			 * 
			 * You can view the stats being collected at:
			 * https://bstats.org/plugin/bukkit/NeuralNetworkAPI
			 */
			Metrics metrics = new Metrics(this);
		}
	}

	public static Main getMainClass() {
		return plugin;
	}

	private void b(List<String> list, String input, String check) {
		if (check.toLowerCase().startsWith(input.toLowerCase()))
			list.add(check);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command,
			String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			b(list, args[0], "save");
			b(list, args[0], "load");
			b(list, args[0], "list");
			b(list, args[0], "startlearning");
			b(list, args[0], "stoplearning");
			b(list, args[0], "start");
			b(list, args[0], "stop");
			b(list, args[0], "createNewNN");
			b(list, args[0], "help");
			b(list, args[0], "test");
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("load")) {
				for (String s : getConfig().getConfigurationSection(
						"NeuralNetworks").getKeys(false))
					b(list, args[1], s);
			} else if (args[0].equalsIgnoreCase("createNewNN")) {
				for (Class<?> c : getRegisteredDemoEntityClasses()) {
					b(list, args[1], c.getSimpleName());
				}
			}
		}
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
			int page = 0;
			if (args.length > 1) {
				page = Integer.parseInt(args[1]) - 1;
			}
			String[] pages = HelpPages.values()[page].lines;
			for (String p : pages) {
				sender.sendMessage(p);
			}
			return true;
		}

		if (!sender.isOp()) {
			sender.sendMessage("Sorry, only OP players can access demo commands.");
			return true;
		}

		if (args[0].equalsIgnoreCase("createNewNN")
				|| args[0].equalsIgnoreCase("cnn")) {
			if (args.length < 2) {
				sender.sendMessage("You must specify which NN you want to create. Choose one of the following:");
				for (Class<?> c : getRegisteredDemoEntityClasses()) {
					sender.sendMessage("-" + c.getSimpleName());
				}
				return true;
			}
			NNBaseEntity base = null;
			for (Class<?> c : getRegisteredDemoEntityClasses()) {
				if (args[1].equalsIgnoreCase(c.getSimpleName())) {
					try {
						try {
							base = (NNBaseEntity) c.getDeclaredConstructor(
									Boolean.TYPE).newInstance(true);
						} catch (Exception e) {
							// If it does not have a parameter for boolean
							// types, use default, empty constructor.
							base = (NNBaseEntity) c.getDeclaredConstructor()
									.newInstance(true);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (base == null) {
				sender.sendMessage("You need to provide a valid bot type. Choose one of the following.");
				for (Class<?> c : getRegisteredDemoEntityClasses()) {
					sender.sendMessage("-" + c.getSimpleName());
				}
				return true;
			}
			sender.sendMessage("Set the NN to "
					+ base.getClass().getSimpleName());
			this.getNn().setCurrentNeuralNetwork(base);
			return true;
		}
		if (args[0].equalsIgnoreCase("setNeuronsPerRow")
				|| args[0].equalsIgnoreCase("snpr")) {
			try {
				this.getNn().getCurrentNeuralNetwork().getAI()
						.setNeuronsPerRow(0, Integer.parseInt(args[1]));
			} catch (Exception e) {
				sender.sendMessage("You must provide how many neurons should be displayed per row");
			}
			return true;
		}

		if (args[0].equalsIgnoreCase("startlearning")) {
			getNn().startLearningAsynchronously();
			sender.sendMessage("Starting learning");
			return true;
		}
		if (args[0].equalsIgnoreCase("stoplearning")) {
			getNn().stopLearning();
			sender.sendMessage("Stoped learning");
			return true;
		}
		if (args[0].equalsIgnoreCase("stop")) {
			getNn().stop();
			sender.sendMessage("Stopping");
			return true;
		}
		if (args[0].equalsIgnoreCase("start")) {
			getNn().start();
			sender.sendMessage("Starting");
			return true;
		}
		if (args[0].equalsIgnoreCase("triggeronce")) {
			sender.sendMessage(getNn().triggerOnce());
			return true;
		}

		if (args[0].equalsIgnoreCase("test")) {
			getNn().getCurrentNeuralNetwork().getControler()
					.setInputs(sender, args);
			sender.sendMessage(getNn().triggerOnce());
			return true;
		}
		if (args[0].equalsIgnoreCase("openGrapher")) {
			getNn().openGrapher();
			sender.sendMessage("Opeining Grapher");
			return true;
		}
		if (args[0].equalsIgnoreCase("closeGrapher")) {
			getNn().closeGrapher();
			sender.sendMessage("closing Grapher");
			return true;
		}
		if (args[0].equalsIgnoreCase("save")) {
			if (args.length > 1) {
				String id = args[1];
				config.set(id, getNn().getCurrentNeuralNetwork());
				try {
					config.save(f);
				} catch (IOException e) {
					e.printStackTrace();
				}
				sender.sendMessage("Saving the NN " + id);
			} else {
				sender.sendMessage("Provide a path for the NN");
			}
			return true;
		}
		if (args[0].equalsIgnoreCase("load")) {
			if (args.length > 1) {
				String id = args[1];
				if (!config.contains(id)) {
					sender.sendMessage("The path in the config is null.");
					return true;
				}
				NNBaseEntity b = (NNBaseEntity) config.get(id);
				if (b == null) {
					sender.sendMessage("The NN  is null.");
					return true;
				}
				getNn().setCurrentNeuralNetwork(b);
				// nn.setCurrentNeuralNetwork(Save_Config.loadnn(this, base,
				// id));
				sender.sendMessage("loading the NN " + id);
			} else {
				sender.sendMessage("Provide an id");
			}
			return true;
		}
		return false;
	}

	private NeuralNetwork getNn() {
		return nn;
	}

	private static List<Class<? extends NNBaseEntity>> registeredDemoClasses = new ArrayList<>();

	/**
	 * THIS SHOULD ONLY BE USED BY OTHER PLUGINS IF YOU WANT TO TEST IT IN THE
	 * DEMO MODE
	 */
	public static void registerDemeEntity(NNBaseEntity base) {
		registeredDemoClasses.add(base.getClass());
	}

	/**
	 * THIS SHOULD ONLY BE USED BY OTHER PLUGINS IF YOU WANT TO TEST IT IN THE
	 * DEMO MODE
	 */
	public static void registerDemoEntity(Class<? extends NNBaseEntity> base) {
		registeredDemoClasses.add(base);
	}

	/**
	 * SHOULD NOT BE USED BY OTHER PLUGINS.
	 */
	public static List<Class<? extends NNBaseEntity>> getRegisteredDemoEntityClasses() {
		return new ArrayList<>(registeredDemoClasses);
	}

	/**
	 * SHOULD NOT BE USED BY OTHER PLUGINS.
	 */
	@Deprecated
	public static void clearAllRegisteredClasses() {
		registeredDemoClasses.clear();
		registeredDemoClasses = null;
	}

}
