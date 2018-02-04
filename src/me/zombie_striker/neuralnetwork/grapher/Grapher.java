package me.zombie_striker.neuralnetwork.grapher;

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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;

import me.zombie_striker.neuralnetwork.*;
import me.zombie_striker.neuralnetwork.neurons.Neuron;
import me.zombie_striker.neuralnetwork.neurons.OutputNeuron;

public class Grapher extends JPanel implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2519755633786585583L;

	JFrame frame;
	Thread thread;

	public Collection<Neuron> allNeurons;

	NeuralNetwork nn;

	final static int WIDTH = 1000;
	final static int HEIGHT = 1000;

	int vsHeight = 50;
	int vsWidth = 10;
	int tileSize = 25;

	int onHeight = 50;
	int onWidth = 450;

	final int spacing = 20;
	final int height_spacing = 10;

	final int init_gray = 100;

	public boolean running = true;

	public void setNN(NeuralNetwork n) {
		this.nn = n;
	}

	public static Grapher initGrapher() {
		final Grapher grapher = new Grapher();
		grapher.initvars();
		return grapher;
	}

	public void reOpenGUI() {
		running = true;
		initvars();
	}

	private void initvars() {
		this.thread = new Thread(this);
		this.frame = new JFrame("Neuron Grapher v0.4");
		// grapher.frame.add(grapher);
		this.thread.start();
		this.setSize(new Dimension(WIDTH, HEIGHT));
		this.frame.setSize(new Dimension(WIDTH, HEIGHT));
		this.frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));

		this.frame.setContentPane(this);
		this.frame.setVisible(true);
		// grapher.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setResizable(true);
		this.frame.pack();
	}

	public void stopIt() {
		running = false;
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	@Override
	public void run() {
		running = true;
		while (running) {
			try {
				BufferedImage bi = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
				Graphics2D graphics = (Graphics2D) bi.getGraphics();
				graphics.fillRect(0, 0, bi.getWidth(), bi.getHeight());

				int space_layer = 20;
				int space_neuron = 10;
				if (this.nn == null || this.nn.getCurrentNeuralNetwork() == null) {
					// If there is no neural network, stop trying to find it.
					// One second delays should be fine for performance
					try {
						Graphics2D thisGraph = (Graphics2D) getGraphics();
						if (thisGraph != null)
							thisGraph.drawImage(bi, 0, 0, frame.getWidth(), frame.getHeight(), null);
						thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}

				int offset = 0;
				boolean firstRow = true;
				int colorOffset = 1;
				for (int layerId = 0; layerId < this.nn.getCurrentNeuralNetwork().getAI().maxlayers; layerId++) {
					int cc = init_gray + (8 * colorOffset);
					Layer layer = this.nn.getCurrentNeuralNetwork().getAI().getLayer(layerId);
					if (layer.getNeuronsPerRow() > 0) {
						graphics.setColor(new Color(cc, cc, cc));
						int expectedOffset = (int) (space_layer
								* (((double) this.nn.getCurrentNeuralNetwork().getAI().getNeuronsInLayer(layerId).size()
										/ layer.getNeuronsPerRow()) + 2));
						graphics.fillRect(firstRow ? 0 : offset, 0, expectedOffset, 1000);
						firstRow = false;
						offset += expectedOffset;
					} else {
						graphics.setColor(new Color(cc, cc, cc));
						graphics.fillRect(firstRow ? 0 : offset, 0, (space_layer * 2), 1000);
						firstRow = false;
						offset += space_layer * 2;
					}
					colorOffset++;
				}
				// TODO:Tempfix
				int cc = init_gray + (8 * (colorOffset + 2));
				graphics.setColor(new Color(cc, cc, cc));
				graphics.fillRect(offset, 0, 1920 - offset, 1000);

				int layeroffset = 0;

				for (Layer layer : this.nn.getCurrentNeuralNetwork().getAI().getLayers()) {
					for (int i = 0; i < layer.neuronsInLayer.size(); i++) {
						Neuron n = layer.neuronsInLayer.get(i);
						// TODO: Make it not rely on bot guesser
						if (layer.getNeuronsPerRow() > 0 && i % (layer.getNeuronsPerRow()) == 0)
							layeroffset++;
						int cs = 0;
						int cw = (int) (((n.getWeight()) * 255 / 2) + 255 / 2);
						if (cw < 0)
							cw = 0;
						if (cw > 255)
							cw = 255;

						if (n.isTraining()) {
							graphics.setColor(new Color(100, 255, 255));
							graphics.fillRect((space_layer * (layer.getLayer() + layeroffset) + 3) - 3,
									(space_neuron
											* (layer.getNeuronsPerRow() > 0 ? (i % layer.getNeuronsPerRow()) : i % 100)
											+ height_spacing) - 3,
									(6 / 2) + (6), 6 + (6));
						}

						graphics.setColor(new Color(255 - cw, cw, 0));
						graphics.fillRect(space_layer * (layer.getLayer() + layeroffset) + 3,
								space_neuron * (layer.getNeuronsPerRow() > 0 ? (i % layer.getNeuronsPerRow()) : i % 100)
										+ height_spacing,
								6 / 2, 6);
						if (n.isTriggered()) {
							if (n.getTriggeredStength() == 0) {
								graphics.setColor(new Color(80, 45, 80));
							} else {
								cs = (int) (((n.getTriggeredStength()) * 255 / 2) + 255 / 2);
								if (cs < 0)
									cs = 0;
								if (cs > 255)
									cs = 255;
								graphics.setColor(new Color(255 - cs, cs, 0));
							}
						} else {
							graphics.setColor(new Color(0, 10, 0));
						}
						graphics.fillRect(space_layer * (layer.getLayer() + layeroffset),
								space_neuron * (layer.getNeuronsPerRow() > 0 ? (i % layer.getNeuronsPerRow()) : i % 100)
										+ height_spacing,
								6 / 2, 6);

						if (layer.getLayer() == nn.getCurrentNeuralNetwork().getAI().maxlayers - 1) {
							// If they are output neurons:
							if (n instanceof OutputNeuron) {
								OutputNeuron on = (OutputNeuron) n;
								if (on.hasName()) {
									if (on.isTriggered()) {
										cs = (int) (((n.getTriggeredStength()) * 255 / 2) + 255 / 2);
										if (cs < 0)
											cs= 0;
										if (cs > 255)
											cs = 255;
										graphics.setColor(new Color(255 - cs, cs, 0));
									} else {
										graphics.setColor(new Color(230, 230, 230));
									}
									graphics.drawString(on.getName() + "  -  " + on.getTriggeredStength(),
											space_layer * (layer.getLayer() + layeroffset + 1) + 3,
											space_neuron
													* (layer.getNeuronsPerRow() > 0 ? (i % layer.getNeuronsPerRow())
															: i % 100)
													+ height_spacing + 6);
								}
							}
						}
					}
					layeroffset++;
				}

				// int finalLossGraphXOffset = space_layer *
				// (this.nn.getCurrentNeuralNetwork().getAI().maxlayers+ layeroffset+1)+3 + 200;

				// graphics.setColor(new Color(230,230,230));

				Graphics2D thisGraph = (Graphics2D) getGraphics();
				if (thisGraph != null)
					thisGraph.drawImage(bi, 0, 0, WIDTH, HEIGHT, null);

				try {
					thread.sleep(33L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		frame.setVisible(false);
		frame.dispose();
		thread.stop();
	}

	/*
	 * @Override public void run() { running=true; while (running) { try {
	 * BufferedImage bi = new BufferedImage(WIDTH, HEIGHT,
	 * BufferedImage.TYPE_INT_RGB); Graphics2D graphics = (Graphics2D)
	 * bi.getGraphics(); graphics.fillRect(0, 0, bi.getWidth(), bi.getHeight()); if
	 * (view != null) { int tiles = ((view.viewdistance * 2) + 1); int[][]
	 * tileScanTypes = new int[tiles][tiles]; int outputNeurons = 0;
	 * 
	 * if (allNeurons != null) { for (Neuron n : allNeurons) { if (n instanceof
	 * OutputMotorNeuron) { graphics.setColor(new Color(0, 0, 0));
	 * graphics.fillRect(onWidth - 1, onHeight - 1+ (((OutputMotorNeuron)
	 * n).responceid * (tileSize + spacing)), tileSize + 2, tileSize + 2);
	 * if(n.input.size()==0){ graphics.setColor(new Color(110, 110, 110)); }else if
	 * (n.isTriggered()) { graphics.setColor(new Color(240, 240, 240)); } else {
	 * graphics.setColor(new Color(160 , 160, 160)); } graphics.fillRect( onWidth,
	 * onHeight
	 * 
	 * + (((OutputMotorNeuron) n).responceid * (tileSize + spacing)), tileSize ,
	 * tileSize); } else if (n instanceof InputNeuron) { if (n instanceof
	 * InputMobNeuron) { tileScanTypes[((InputNeuron) n).xlink][((InputNeuron)
	 * n).ylink] = 1; } else if (n instanceof InputBlockNeuron) {
	 * tileScanTypes[((InputNeuron) n).xlink][((InputNeuron) n).ylink] = 2; } } } }
	 * for (int w = 0; w < view.universe.length; w++) { for (int h = 0; h <
	 * view.universe[w].length; h++) { if (view.universe[w][h] != null) { int r = 0;
	 * int g = 0; int b = 0; if (view.universe[w][h] instanceof Block) g = 160; if
	 * (view.universe[w][h] instanceof Entity) r = 160; if (tileScanTypes[w][h] ==
	 * 1) r += 80; if (tileScanTypes[w][h] == 2) g += 80;
	 * 
	 * graphics.setColor(new Color(r, g, b)); graphics.fillRect(vsWidth + (w *
	 * tileSize), vsHeight + (h * tileSize), tileSize, tileSize); } } }
	 * graphics.setColor(new Color(0, 0, 0)); for (int Lines = 0; Lines < tiles + 1;
	 * Lines++) { graphics.drawLine(vsWidth + (tileSize * Lines), vsHeight, vsWidth
	 * + (tileSize * Lines), vsHeight + (tileSize * tiles));
	 * graphics.drawLine(vsWidth, vsHeight + (tileSize * Lines), vsWidth + (tileSize
	 * * tiles), vsHeight + (tileSize * Lines)); } graphics.drawString(
	 * "Generation ="+Test.test.generation+"  || Active Geneome ="
	 * +Test.test.activePop
	 * +" || Current score ="+(Test.test.current!=null?Test.test
	 * .current.ai.score:-1),30, 10); }
	 * 
	 * if (allNeurons != null) { for (Neuron n : allNeurons) { if (n instanceof
	 * OutputMotorNeuron) { for (Neuron inputs : n.ai.getNeuronsFromId(n.input)) {
	 * int r = 0; int g = 0; int b = 0; // Make the colors darker for lines if
	 * (inputs instanceof InputBlockNeuron) g = 160 + (inputs.isTriggered() ? 80 :
	 * 0) - 30; if (inputs instanceof InputMobNeuron) r = 160 +
	 * (inputs.isTriggered() ? 80 : 0) - 30; if (inputs instanceof
	 * OutputMotorNeuron) b = 160 + (inputs.isTriggered() ? 80 : 0) - 30;
	 * 
	 * graphics.setColor(new Color(r, g, b)); if (inputs instanceof InputNeuron) {
	 * graphics.drawLine( vsWidth + (tileSize / 2) + (((InputNeuron) inputs).xlink *
	 * tileSize), vsHeight + (tileSize / 2) + (((InputNeuron) inputs).ylink *
	 * tileSize), onWidth + (tileSize / 2), onHeight + (tileSize / 2) +
	 * (((OutputMotorNeuron) n).responceid * (tileSize + spacing))); }else if
	 * (inputs instanceof OutputMotorNeuron){ int direction = (((OutputMotorNeuron)
	 * inputs).responceid-((OutputMotorNeuron) n).responceid); int
	 * directionNormalized = (direction)/(direction<0?-direction:direction);
	 * graphics.drawLine(onWidth + (tileSize / 2), onHeight + (tileSize / 2) +
	 * (((OutputMotorNeuron) inputs).responceid * (tileSize + spacing)), onWidth +
	 * (tileSize / 2) +(direction*(spacing)+(directionNormalized*tileSize)),
	 * onHeight + (tileSize / 2) + (((OutputMotorNeuron) inputs).responceid *
	 * (tileSize + spacing))); graphics.drawLine(onWidth + (tileSize / 2), onHeight
	 * + (tileSize / 2) + (((OutputMotorNeuron) n).responceid * (tileSize +
	 * spacing)), onWidth + (tileSize / 2)
	 * +((direction*(spacing)+(directionNormalized*tileSize))), onHeight + (tileSize
	 * / 2) + (((OutputMotorNeuron) n).responceid * (tileSize + spacing)));
	 * 
	 * graphics.drawLine(onWidth + (tileSize / 2)
	 * +(direction*(spacing)+(directionNormalized*tileSize)), onHeight + (tileSize /
	 * 2) + (((OutputMotorNeuron) inputs).responceid * (tileSize + spacing)),
	 * onWidth + (tileSize / 2)
	 * +(direction*(spacing)+(directionNormalized*tileSize)), onHeight + (tileSize /
	 * 2) + (((OutputMotorNeuron) n).responceid * (tileSize + spacing))); } } } } }
	 * Graphics2D thisGraph = (Graphics2D) getGraphics(); if (thisGraph != null)
	 * thisGraph.drawImage(bi, 0, 0, 1600, 800, null);
	 * 
	 * try { thread.sleep(100L); } catch (InterruptedException e) {
	 * e.printStackTrace(); }
	 * 
	 * } catch (Exception e) { e.printStackTrace(); } } frame.setVisible(false);
	 * frame.dispose(); thread.stop(); }
	 */
	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

}
