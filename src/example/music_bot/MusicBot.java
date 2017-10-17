package example.music_bot;

import java.util.*;

import org.bukkit.*;
import org.bukkit.block.NoteBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.*;
import me.zombie_striker.neuralnetwork.neurons.input.*;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Numbers;
import me.zombie_striker.neuralnetwork.util.DeepReinformentUtil;
import me.zombie_striker.nnmain.Main;

public class MusicBot extends NNBaseEntity implements Controler{

	public Sensory2D_Numbers numbers = new Sensory2D_Numbers(10, 90);

	public static final int PITCHES = 24;
	
	// public boolean wasCorrect = true;
	//Different than should learn. This erases the inputs so the trained outputs are not what it is starting off of
	public boolean wasLearning = false;

	public double[][] trainingValues;
	int training_step = 0;

	public MusicBot base;

	public MusicBot(boolean createAI) {
		super(false);
		addMusicTrainingData(this);
		if (createAI) {
			//Creates a new AI with (Pitches) amount of output neurons and with three layers (which includes the input and output layers).
			this.ai = NNAI.generateAI(this, PITCHES, 3);
			numbers.changeMatrix(new double[PITCHES][90]);

			//Creates input number neurons for the Pitches (rows) and the memory (columns)
			for (int rows = 0; rows < numbers.getMatrix().length; rows++) {
				for (int col1 = 0; col1 < numbers.getMatrix()[0].length; col1++) {
					InputNumberNeuron.generateNeuronStatically(ai, rows, col1,
							numbers);
				}
			}

			//Generates neurons  for layer 1.
			for (int neurons = 0; neurons < 38; neurons++) {
				Neuron.generateNeuronStatically(ai, 1);
			}
			
			//Generate 10 tick rates, hopefully one of them will help with the rhythm.
			for(int tickrate = 1; tickrate <= 10; tickrate++)
			InputTickNeuron.generateNeuronStatically(ai,tickrate);
			
			//Generate a bia neuron for each layer.
			BiasNeuron.generateNeuronStatically(ai, 0);
			BiasNeuron.generateNeuronStatically(ai, 1);

			connectNeurons();
		}
		this.controler = this;
	}


	// TODO: This is used for denoting pitch. All conversions from decimal to
	// whole number should use this value.

	@Override
	public String update() {
		if (shouldLearn) {
			// This takes the training data (the music sheet), increments the
			// training step tick, and updates the sensors to the notes.
			int highestlength = 0;
			for (int i = 0; i < trainingValues.length; i++) {
				if (highestlength < trainingValues[i].length) {
					highestlength = trainingValues[i].length;
				}
			}
			training_step = (training_step + 1);
			if (training_step >= highestlength) {
				base.ai.setCurrentTick(0);
				training_step = 0;
			}
			double[][] values = new double[base.numbers.getMatrix().length][base.numbers.getMatrix()[0].length];
			for (int row = 0; row < values.length; row++) { // Pitchstamp
				for (int col = 0; col < values[row].length; col++) { // Timestamp
					for (int channels = 0; channels < trainingValues.length; channels++)
						if ((training_step - values[row].length + col >= 0)
								&& (trainingValues[channels].length > training_step
										- values[row].length + col && (training_step
										- values[row].length + col >= 0 && (trainingValues[channels][training_step
										- values[row].length + col] * PITCHES) == row))) {
							values[row][col] = 1;
						}
				}
			}
			base.numbers.changeMatrix(values);
		}
		base.ai.tick();
		boolean[] actions = base.ai.think();

		if (!shouldLearn) {
			if (wasLearning) {
				base.ai.setCurrentTick(0);
				wasLearning = false;
			}
			// Clears all the sensors if it was training the previous tick

			// Moves all the notes back
			// TODO: Change code so that it moves the notes back by 1 in the ROW
			// direction, not the COL direction
			double[][] previousNotes = base.numbers.getMatrix();
			for (int row = 0; row < previousNotes.length; row++) {
				for (int col = 1; col < previousNotes[row].length; col++) {
					previousNotes[row][col - 1] = previousNotes[row][col];
				}
			}

			// If the output neuron was triggered, add the neuron to the matrix.
			for (Neuron n : base.ai.getOutputNeurons()) {
				previousNotes[n.getID()][previousNotes[n.getID()].length - 1] = n
						.isTriggered() ? n.getTriggeredStength() : 0;
			}
			base.numbers.changeMatrix(previousNotes);

		} else {
			wasLearning = true;

			// Loops through all the outputs. If the output was correct, set the
			// desired value to 1. If not, set it to -1.
			HashMap<Neuron, Double> desiredTriggerStrengths = new HashMap<>();
			for (int i = 0; i < actions.length; i++) {
				boolean wasCorrect = false;
				for (int channel = 0; channel < trainingValues.length; channel++) {
					if (training_step < trainingValues[channel].length)
						if (wasCorrect = (i == (int) (trainingValues[channel][training_step] * PITCHES)))
							break;
				}
				desiredTriggerStrengths.put(base.ai.getNeuronFromId(i),
						(wasCorrect ? 1 : -1.0));
			}

			//This determines if the output is equal to the training data.
			boolean playedRightNotes = true;
			for (int i = 0; i < actions.length; i++) {
				boolean wasToldToBeTrue = false;

				for (int channel = 0; channel < trainingValues.length; channel++) {
					if (training_step < trainingValues[channel].length) {
						if ((int) (trainingValues[channel][training_step] * PITCHES) == i) {
							wasToldToBeTrue = true;
							break;
						}
					}
				}
				if (wasToldToBeTrue != actions[i]) {
					playedRightNotes = false;
					break;
				}
			}

			//Adds weather the notes were played correctly to the accuracy list.
			this.getAccuracy().addEntry(playedRightNotes);
			int total_accuracy = this.getAccuracy().getAccuracyAsInt();

			//Make corrections so outputs are what they should be. If it made a mistake, do this three times.
			DeepReinformentUtil.instantaneousReinforce(base,
					desiredTriggerStrengths, ((playedRightNotes) ? 1 : 3));
			
			//Logger: Print out all the triggered neurons. If the output was correct, the message will be green.
			StringBuilder activeNeurons = new StringBuilder();
			for(Neuron omn : base.ai.getOutputNeurons()){
				if(omn.isTriggered())
					activeNeurons.append(omn.getID()+", ");
			}
		return (
					((playedRightNotes) ? ChatColor.GREEN : ChatColor.RED)
							+ ""
							+ total_accuracy
							+ "% : Active neurons = "+activeNeurons.toString());
		}
		return null;
	}


	@Override
	public void setInputs(CommandSender initiator, String[] args) {
		// 
		final Player player = (Player) initiator;
		final int ticksMax = (args.length > 1) ? Integer.parseInt(args[1])
				: 20;

		final Location base = player.getLocation().clone();

		final int channels = this.trainingValues.length;

		for (int i = 0; i < channels; i++) {
			base.clone().add(i, 0, 0).getBlock()
					.setType(Material.NOTE_BLOCK);
		}
		new BukkitRunnable() {
			int tick = 0;

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				tick++;
				int row = 0;
				for (Neuron n : ai.getOutputNeurons()) {
					if (n.isTriggered()) {
						((NoteBlock) base.clone().add(row, 0, 0).getBlock()
								.getState()).setRawNote((byte) n.getID());
						((NoteBlock) base.clone().add(row, 0, 0).getBlock()
								.getState()).play();
						row++;

					}
				}

				if (tick > ticksMax)
					cancel();
			}
		}.runTaskTimer(Main.getMainClass(), 30, 9);
		
	}

	public double[] convertToDoubles(String pitches) {
		List<Double> values = new ArrayList<>();
		// int offset =0;
		for (int index = 0; index < pitches.length(); index++) {
			char c = pitches.charAt(index);
			if (c == '.') {
				// offset++;
				continue;
			}
			double d = c == ' ' ? -1
					: c == 'a' ? 10
							: c == 'b' ? 11
									: c == 'c' ? 12
											: c == 'd' ? 13
													: c == 'e' ? 14
															: c == 'f' ? 15
																	: c == 'g' ? 16
																			: c == 'h' ? 17
																					: c == 'i' ? 18
																							: c == 'j' ? 19
																									: c == 'k' ? 20
																											: c == 'l' ? 21
																													: c == 'm' ? 22
																															: c == 'n' ? 23
																																	: c == '0' ? 24
																																			: Double.parseDouble(""
																																					+ c);
			values.add(d / PITCHES);
		}
		double[] array = new double[values.size()];
		for (int i = 0; i < array.length; i++)
			array[i] = values.get(i);
		return array;
	}

	private void addMusicTrainingData(NNBaseEntity base) {

		// TODO: Move this into the NNMusicBot class.

		/**
		 * Each section, the two values are on the same line. Each one is a 1/2
		 * higher than the last. 0 1 2 3 4 - In the top line of the bottom row 5
		 * 6 6 7-- Now on the top section, not touching the bottom line 8 9 10 -
		 * In the bottom line of the top row 11 12 13 14 15 16 17 18 19 20 21 22
		 * 23 24 -- IN the top line of the top row.
		 */

		/**
		 * Every 8 characters last for 3 seconds. That means
		 */
		// TODO: Moonlight Sonata
		// https://www.8notes.com/scores/1754.asp
		double[][] song = {
				convertToDoubles("68a68a68a68a.68a68a68a68a.68a68a68b68b.68b68a689688.11568a68ad  d.68b68b68b68bd.68a68a68b68b.68b68b68a68a.68a68a68a68a."),
				convertToDoubles("1           .1           .1           .1           .1        68a .1        d   .1     1     .1     1  h  .a           ."),
				convertToDoubles("1           .1           .1           .1           .1            .f            .f     f     .f     f     .0           .") };

		this
				.setTrainingSong(song);
	}
	

	@Override
	public NNBaseEntity clone() {
		MusicBot thi = new MusicBot(false);
		thi.ai = this.ai;
		return thi;
	}


	
	public void setBase(NNBaseEntity t) {
		this.base = (MusicBot) t;
	}

	public void setTrainingSong(double[][] data) {
		this.trainingValues = data;
	}
}
