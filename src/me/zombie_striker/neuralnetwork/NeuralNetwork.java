package me.zombie_striker.neuralnetwork;

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

import java.util.ArrayList;
import java.util.List;

import me.zombie_striker.neuralnetwork.grapher.Grapher;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class NeuralNetwork {

	private Grapher grapher;
	private NNBaseEntity base;
	private BukkitTask runnable;
	private Plugin mainClass;

	private List<String> messages = new ArrayList<>();

	private boolean Asyncrunning = false;

	private boolean broadcastMessage = true;

	public NeuralNetwork(final Plugin mainClass) {
		this.mainClass = mainClass;
		new BukkitRunnable() {
			public void run() {
				if (broadcastMessage) {
					if (messages.size() > 0) {
						List<String> s = new ArrayList<>(messages);
						messages.clear();
						if (s.size() > 5) {
							Bukkit.getConsoleSender().sendMessage(s.get(0));
							Bukkit.getConsoleSender().sendMessage(s.get(1));
							Bukkit.getConsoleSender().sendMessage(s.get(2));
							Bukkit.getConsoleSender().sendMessage(s.size() + " more...");
						} else {
							for (String ss : s) {
								if (ss != null)
									Bukkit.getConsoleSender().sendMessage(ss);
							}
						}
					}
				} else {
					messages.clear();
				}
			}
		}.runTaskTimer(mainClass, 1, 20);
	}

	/**
	 * Returns the current NeurnalNetwork entity
	 * 
	 * @param base
	 */
	public void setCurrentNeuralNetwork(NNBaseEntity base) {
		this.base = base;
	}

	/**
	 * Returns the current Neural network entity.
	 */
	public NNBaseEntity getCurrentNeuralNetwork() {
		return base;
	}

	/**
	 * Triggered controler#update once.
	 */
	public String triggerOnce() {
		String a = base.controler.update();
		messages.add(a);
		return a;
	}

	/**
	 * Triggered controler#update once.
	 */
	public String learn() {
		String a = base.controler.learn();
		messages.add(a);
		return a;
	}

	/**
	 * Starts a BukkitRunnable that will update the control every tick. Should be
	 * used if the NN requires data from bukkit
	 */
	public void start() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				triggerOnce();
			}
		}.runTaskTimer(this.mainClass, 0, 1);
	}

	/**
	 * Starts training the neural network. Note that this NN should not make any
	 * calls to Bukkit while training.
	 */
	public void startLearningAsynchronously() {
		getCurrentNeuralNetwork().setShouldLearn(true);
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
		Asyncrunning = true;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				while (getCurrentNeuralNetwork().shouldLearn()) {
					learn();
					System.out.println("Running again");
				}
			}
		}.runTaskAsynchronously(this.mainClass);
	}

	/**
	 * Starts training the neural network. Note that this NN should not make any
	 * calls to Bukkit while training.
	 */
	public void startLearningAsynchronouslyONCE() {
		getCurrentNeuralNetwork().setShouldLearn(true);
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
		Asyncrunning = true;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				learn();
				stopLearning();
				this.cancel();
			}
		}.runTaskAsynchronously(this.mainClass);
	}

	/**
	 * Starts training the neural network. Note that this this is slower than Async
	 * learning and may cause some lag on the server.
	 */
	public void startLearningSynchronously() {
		getCurrentNeuralNetwork().setShouldLearn(true);
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				learn();
			}
		}.runTaskTimer(this.mainClass, 0, 1);
	}

	/**
	 * Starts training the neural network. Note that this this is slower than Async
	 * learning and may cause some lag on the server.
	 */
	public void startLearningSynchronouslyONCE() {
		getCurrentNeuralNetwork().setShouldLearn(true);
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				learn();
			}
		}.runTask(this.mainClass);
	}

	/**
	 * Starts training the neural network. Note that this NN should not make any
	 * calls to Bukkit while training.
	 */
	public void stopLearning() {
		stop();
		getCurrentNeuralNetwork().setShouldLearn(false);
	}

	/**
	 * Starts an *Asynchronously* BukkitRunnable that will update the control every
	 * tick. Should be used for training the NN with data not a part of bukkit.
	 */
	public void startAsynchronously() {
		if (this.runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
		Asyncrunning = true;
		this.runnable = new BukkitRunnable() {
			@Override
			public void run() {
				while (Asyncrunning) {
					triggerOnce();
				}
			}
		}.runTaskLaterAsynchronously(this.mainClass, 1);
	}

	/**
	 * Stops the BukkitRunnable.
	 */
	public void stop() {
		Asyncrunning = false;
		if (runnable != null) {
			this.runnable.cancel();
			this.runnable = null;
		}
	}

	/**
	 * Opens the NeuralGrapher. Use this to visualize all the neurons.
	 */
	public void openGrapher() {
		if (grapher == null) {
			grapher = Grapher.initGrapher();
		} else {
			grapher.reOpenGUI();
		}
		grapher.setNN(this);
	}

	/**
	 * Closes the NeuralGrapher window.
	 */
	public void closeGrapher() {
		if (grapher != null)
			grapher.stopIt();
		// this.grapher = null;
	}

	public Grapher getGrapher() {
		return grapher;
	}

	public boolean isBeingBroadcasted() {
		return broadcastMessage;
	}

	public void setBroadcasting(boolean b) {
		broadcastMessage = b;
	}
}
