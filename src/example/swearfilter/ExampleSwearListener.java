package example.swearfilter;

import me.zombie_striker.neuralnetwork.NeuralNetwork;
import me.zombie_striker.nnmain.Main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ExampleSwearListener implements Listener {

	private NeuralNetwork currentNN;
	/**
	 *  This does not actually do anything. All this is meant for is to check if the main 
	 *  NN for the demo is set to SwearFilter, and if so, create 5 more swearbots that will actually 
	 *  train to listen for swear words.
	 */

	private NeuralNetwork[] swearbots = new NeuralNetwork[5];

	public ExampleSwearListener(NeuralNetwork n) {
		currentNN = n;
		new BukkitRunnable() {

			@Override
			public void run() {
				if (!(currentNN.getCurrentNeuralNetwork() instanceof SwearBot))
					return;
				Bukkit.broadcastMessage(ChatColor.GOLD
						+ "Training the swear bots. Please wait");
				for (int i = 0; i < swearbots.length; i++) {
					swearbots[i] = new NeuralNetwork(Main.getMainClass());
					swearbots[i].setBroadcasting(false);
					// Cleans up the command prompt
				}
				swearbots[0]
						.setCurrentNeuralNetwork(new SwearBot(true, "fuck"));
				swearbots[1]
						.setCurrentNeuralNetwork(new SwearBot(true, "shit"));
				swearbots[2]
						.setCurrentNeuralNetwork(new SwearBot(true, "bitch"));
				swearbots[3]
						.setCurrentNeuralNetwork(new SwearBot(true, "cunt"));
				swearbots[4].setCurrentNeuralNetwork(new SwearBot(true, "fag"));

				swearbots[0].startLearningAsynchronously();
				System.out.println("Stating to train NNs");
				for (int i = 1; i < swearbots.length; i++) {
					final int k = i;
					new BukkitRunnable() {

						@Override
						public void run() {
							System.out.println("Finished training NN "
									+ (k - 1) + "/" + swearbots.length);
							swearbots[k - 1].stopLearning();
							swearbots[k].startLearningAsynchronously();
						}
					}.runTaskLater(Main.getMainClass(), 20 * 15 * k);
					// Train for 15 seconds
				}
				new BukkitRunnable() {

					@Override
					public void run() {
						swearbots[4].stopLearning();
						Bukkit.broadcastMessage(ChatColor.GOLD + "Done!");
					}
				}.runTaskLater(Main.getMainClass(),
						20 * 15 * (swearbots.length));
				this.cancel();
			}
		}.runTaskTimer(Main.getMainClass(), 20 * 4, 20);
		// Every second, check if swearbot has been set.
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (currentNN != null)
			if (!(currentNN.getCurrentNeuralNetwork() instanceof SwearBot))
				return;
		StringBuilder chat = new StringBuilder();
		chat.append("  ");
		for (char c : e.getMessage().toUpperCase().toCharArray()) {
			if (c != ' ' && c != '?' && c != '.' && c != ',' && c != '!')
				chat.append(c);
		}
		for (int i = 0; i < chat.toString().length(); i++) {
			String testingString = chat.toString().substring(i);
			for (NeuralNetwork k : swearbots) {
				((SwearBot) k.getCurrentNeuralNetwork()).word
						.changeWord(testingString);
				boolean detectsSwearWord = ((SwearBot) k
						.getCurrentNeuralNetwork()).tickAndThink()[0];
				if (detectsSwearWord) {
					// The bot says the word is a swear word
					e.setCancelled(true);
					e.getPlayer()
							.sendMessage(
									"[SwearBot] Do not swear. Found result \""
											+ ((SwearBot) k
													.getCurrentNeuralNetwork()).filterType
											+ "\" in \"" + testingString + "\"");
					return;
				}
			}
		}
	}
}
