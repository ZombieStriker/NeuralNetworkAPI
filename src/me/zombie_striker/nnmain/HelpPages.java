package me.zombie_striker.nnmain;

import org.bukkit.ChatColor;

public enum HelpPages {
	one(ChatColor.GOLD+"--==Description: Page 1/3 ==--",
		"NeuralNetworkAPI is a plugin aimed on adding NeuralNetworks to minecraft.",
		"Pages:",
		"-Desctipion (Current)",
		"-How to use",
		"-Commands"
		),
	two(ChatColor.GOLD+"--==How to use: Page 2/3==--",
			"To start, select a neural network you wish to use by using \"/nn cnn <type>\". To see all NN-Types, leave <type> empty.",
			"Once you have selected a type, you need to train the neural network. Use \"/nn startlearning\" to start training.",
			"Once you feel the NN is good enough (you can check the console to see its accuracy), use \"/nn stoplearning\" to stop.",
			"To test the NN, use \"/nn test <inputs>\" to test it."
			),			
	three(ChatColor.GOLD+"--==Commands: Page 3/3==--",
			"/nn help - Displays help pages",
			"/nn cnn (or createNewNN) - creates a new NN",
			"/nn openGrapher - Opens the neuron grapher gui (only useful for servers running on a person computer)",
			"/nn closeGrapher- closes grapher",
			"/nn startLearning - Starts training the NN",
			"/nn stoplearning - Stops training the nn",
			"/nn test <Inputs...> - tests the NN givin the input",
			"/nn save <name> - Saves the current to the config",
			"/nn load <name> - Loads the NN from the config",
			"/nn list - Lists all saved NN from the config",
			"/nn triggeronce- Triggers the NN once, (does not change inputs)"
			);
	
	String[] lines;
	private HelpPages(String... text) {
		this.lines = text;
	}
}
