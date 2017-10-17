package me.zombie_striker.nnmain;

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
	 * If you are creating your own plugin using the NNAPI, do not use the default NN. You should have your own class.
	 */
	private NeuralNetwork nn;

	protected static Main plugin;

	@SuppressWarnings("deprecation")
	@Override
	public void onDisable() {
		plugin = null;
		if(getNn()!=null&&getNn().getGrapher()!=null)
		getNn().closeGrapher();
		NeuralNetwork.clearAllRegisteredClasses();
	}

	@Override
	public void onEnable() {
		//TODO: Remove these values. They were only needed back when the NNs did not implement ConfigurationSerializable
		NeuralNetwork.registerBaseEntity(BlackJackHelper.class);
		NeuralNetwork.registerBaseEntity(PrimeNumberBot.class);
		NeuralNetwork.registerBaseEntity(NumberAdder.class);
		NeuralNetwork.registerBaseEntity(MusicBot.class);
		NeuralNetwork.registerBaseEntity(BotGuesser.class);
		NeuralNetwork.registerBaseEntity(SwearBot.class);
		NeuralNetwork.registerBaseEntity(LogicalInverted.class);
		NeuralNetwork.registerBaseEntity(LogicalOR.class);
		NeuralNetwork.registerBaseEntity(LogicalAND.class);
		NeuralNetwork.registerBaseEntity(LogicalXOR.class);
		NeuralNetwork.registerBaseEntity(LogicalNAND.class);
		NeuralNetwork.registerBaseEntity(LogicalNOR.class);
		NeuralNetwork.registerBaseEntity(LogicalXNOR.class);
		
		nn=new NeuralNetwork(this);
		plugin = this;

		config = YamlConfiguration.loadConfiguration(f);
		Bukkit.getPluginManager().registerEvents(new ExampleSwearListener(getNn()), this);
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
				for (Class<?> c : NeuralNetwork
						.getRegisteredBaseEntityClasses()) {
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

		if (args[0].equalsIgnoreCase("createNewNN")
				|| args[0].equalsIgnoreCase("cnn")) {
			if (args.length < 2) {
				sender.sendMessage("You must specify which NN you want to create. Choose one of the following:");
				for (Class<?> c : NeuralNetwork
						.getRegisteredBaseEntityClasses()) {
					sender.sendMessage("-" + c.getSimpleName());
				}
				return true;
			}
			NNBaseEntity base = null;
			for (Class<?> c : NeuralNetwork.getRegisteredBaseEntityClasses()) {
				if (args[1].equalsIgnoreCase(c.getSimpleName())) {
					try {
						base = (NNBaseEntity) c.getDeclaredConstructor(
								Boolean.TYPE).newInstance(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (base == null) {
				sender.sendMessage("You need to provide a valid bot type. Choose one of the following.");
				for (Class<?> c : NeuralNetwork
						.getRegisteredBaseEntityClasses()) {
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
			getNn().getCurrentNeuralNetwork().getControler().setInputs(sender, args);
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

}
