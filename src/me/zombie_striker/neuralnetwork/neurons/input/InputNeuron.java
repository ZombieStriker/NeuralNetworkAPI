package me.zombie_striker.neuralnetwork.neurons.input;

import java.util.Map;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.neurons.Neuron;
import me.zombie_striker.neuralnetwork.senses.*;

public class InputNeuron extends Neuron {

	public int xlink;
	public int ylink;
	public int zlink;
	
	protected Senses s;
	
	
	///public Sensory_Vision entitiesVision;

	public InputNeuron(NNAI ai, int xlink, int ylink,int zlink, Senses s) {
		super(ai,0);
		init(ai,xlink,ylink,zlink,s);
	}
	public InputNeuron(NNAI ai, int xlink, int ylink, Senses s) {
		super(ai,0);
		init(ai,xlink,ylink,0,s);
	}
	public InputNeuron(NNAI ai) {
		super(ai,0);
	}

	
	private void init(NNAI ai, int xlink, int ylink,int zlink, Senses s){
		this.xlink = xlink;
		this.ylink = ylink;
		this.zlink = zlink;
		this.s = s;
	}


	@Override
	public Neuron clone(NNAI ai) {
		InputNeuron n = (InputNeuron) super.clone(ai);
		n.xlink = xlink;
		n.ylink = ylink;
		return null;
	}

	public InputNeuron generateNeuron(NNAI ai, Senses2D sensory) {
		return null;
	}
	
	@Override
	public double forceTriggerStengthUpdate() {
		this.tickUpdated = getAI().getCurrentTick();
		if(s instanceof Senses2D)
		return lastResult = ((Senses2D) s).getPowerFor(xlink, ylink);
		if(s instanceof Senses3D)
		return lastResult = ((Senses3D) s).getPowerFor(xlink, ylink,zlink);
		return 0;
	}
	@Override
	public double getTriggeredStength() {
		if(s instanceof Senses2D)
		return ((Senses2D) s).getPowerFor(xlink, ylink);
		if(s instanceof Senses3D)
		return ((Senses3D) s).getPowerFor(xlink, ylink,zlink);
		return 0;
	}
	
	public Senses getSenses(){
		return s;
	}
	public void stSenses(Senses s){
		this.s = s;
	}

	public InputNeuron(Map<String, Object> map) {
		super(map);
		this.xlink = (int) map.get("xl");
		this.ylink = (int) map.get("yl");
		this.zlink = (int) map.get("zl");
		this.s = (Senses) map.get("s");
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> m = super.serialize();//new HashMap<String, Object>();
		m.put("xl", this.xlink);
		m.put("yl", this.ylink);
		m.put("zl", this.zlink);
		m.put("s", this.s);
		return m;
	}
}
