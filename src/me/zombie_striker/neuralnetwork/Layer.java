package me.zombie_striker.neuralnetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.zombie_striker.neuralnetwork.neurons.Neuron;

public class Layer implements ConfigurationSerializable {
	public ArrayList<Neuron> neuronsInLayer = new ArrayList<>();
	private int i;

	private int neuronsperrow = -1;

	public Layer(int layer) {
		i = layer;
	}

	public int getLayer() {
		return i;
	}

	public int getNeuronsPerRow() {
		return neuronsperrow;
	}

	public void setNeuronsPerRow(int i) {
		neuronsperrow = i;
	}

	@SuppressWarnings("unchecked")
	public Layer(Map<String, Object> map) {
		this.neuronsInLayer = (ArrayList<Neuron>) map.get("n");
		this.i = (int) map.get("l");
		this.neuronsperrow = (int) map.get("npr");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("n", this.neuronsInLayer);
		m.put("l", this.i);
		m.put("npr", neuronsperrow);
		return m;
	}
}
