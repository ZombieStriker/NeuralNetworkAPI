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

	private boolean training = false;
	
	public ExampleSwearListener(NeuralNetwork n) {
		currentNN = n;
		
		//Creating runnable so it only starts training the NN when the main NN is set to a swearbot
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!(currentNN.getCurrentNeuralNetwork() instanceof SwearBot))
					return;
				// Every second, check if swearbot has been set. If it has not, return. At the very end, cancel the task so this only runs once.
				
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
				training=true;
				for (int i = 1; i < swearbots.length; i++) {
					final int k = i;
					new BukkitRunnable() {

						@Override
						public void run() {
							Bukkit.broadcastMessage("Finished training \""+((SwearBot)swearbots[k-1].getCurrentNeuralNetwork()).filterType.substring(0,2)+"\" NN "
									+ (k) + "/" + swearbots.length+" Accuracy:"+swearbots[k-1].getCurrentNeuralNetwork().getAccuracy().getAccuracyAsInt());
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
						Bukkit.broadcastMessage("Finished training \""+((SwearBot)swearbots[4].getCurrentNeuralNetwork()).filterType.substring(0,2)+"\" NN "
								+ (5) + "/" + swearbots.length+" Accuracy:"+swearbots[4].getCurrentNeuralNetwork().getAccuracy().getAccuracyAsInt());
						Bukkit.broadcastMessage(ChatColor.GOLD + "Done!");
						training=false;
					}
				}.runTaskLater(Main.getMainClass(),
						20 * 15 * (swearbots.length));
				this.cancel();
			}
		}.runTaskTimer(Main.getMainClass(), 20 * 4, 20);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		if (currentNN != null)
			if (!(currentNN.getCurrentNeuralNetwork() instanceof SwearBot))
				return;
		if(training)
			return;
		
		//If it is not training, and if the demo is set to swearbot, then do the following:
		
		StringBuilder chat = new StringBuilder();
		chat.append("  ");
		for (char c : e.getMessage().toUpperCase().toCharArray()) {
			if (c != ' ' && c != '?' && c != '.' && c != ',' && c != '!')
				chat.append(c);
		}
		//Add two spaces before the chat message, remove all spaces and punctuation marks so 's?h.i t' is treated as 'shit'
		
		for (int i = 0; i < chat.toString().length(); i++) {
			String testingString = chat.toString().substring(i);
			//We are using a scanner approach. This will offset the string by 1 char until it is at the last letter.
			for (NeuralNetwork k : swearbots) {
				((SwearBot) k.getCurrentNeuralNetwork()).word
						.changeWord(testingString);
				//Loop through all the swear types. Testt it for each NN.
				
				boolean detectsSwearWord = ((SwearBot) k
						.getCurrentNeuralNetwork()).tickAndThink()[0];
				if (detectsSwearWord) {
					// The bot detects a similarity to a swear word. May be a swear.
					e.setCancelled(true);
					e.getPlayer()
							.sendMessage(
									"[SwearBot] Do not swear. Found similarities of \""
											+ ((SwearBot) k
													.getCurrentNeuralNetwork()).filterType
											+ "\" in \"" + testingString + "\"");
					return;
				}
			}
		}
	}
}
