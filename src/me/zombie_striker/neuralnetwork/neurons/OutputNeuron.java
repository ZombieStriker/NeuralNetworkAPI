package me.zombie_striker.neuralnetwork.neurons;

import java.util.Map;

import me.zombie_striker.neuralnetwork.NNAI;

public class OutputNeuron extends Neuron{

	public int responceid;	
	private String name;
	
	public String getName(){
		return name;
	}
	public void setName(String n){
		name = n;
	}
	public boolean hasName(){
		return name!=null;
	}
	public OutputNeuron(NNAI ai, int responceid) {
		super(ai,ai.MAX_LAYERS-1);
		this.responceid = responceid;
	}
	public OutputNeuron(NNAI ai, int responceid, int layer) {
		super(ai,layer);
		this.responceid = responceid;
	}
	@Override
	public Neuron generateNeuron(NNAI ai) {
		OutputNeuron n = new OutputNeuron(ai,responceid);
		return n;
	}
	
	@Override
	public Neuron clone(NNAI ai) {
		OutputNeuron clone = (OutputNeuron) super.clone(ai);
		clone.responceid = responceid;
		return clone;
	}
	@Override
	public boolean isTriggered() {
		return getTriggeredStength() > 0;
	}
	public OutputNeuron(Map<String, Object> map) {
		super(map);
		this.name = (String) map.get("n");
		this.responceid = (int) map.get("rid");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = super.serialize();//new HashMap<String, Object>();
		m.put("n", this.name);
		m.put("rid", this.responceid);
		return m;
	}
}
