package me.zombie_striker.neuralnetwork.neurons.input;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.Senses2D;
import me.zombie_striker.neuralnetwork.senses.Sensory2D_Numbers;


public class InputBlockNeuron extends InputNeuron {
	
	public InputBlockNeuron(NNAI ai, int xlink, int ylink, Sensory2D_Numbers sn) {
		this(ai,xlink,ylink,-1,sn);
	}
	public InputBlockNeuron(NNAI ai, int xlink, int ylink, int blockID, Sensory2D_Numbers sn) {
		super(ai,xlink,ylink,sn);
		this.s = sn;
		this.zlink = blockID;
	}
	public InputBlockNeuron(NNAI ai,Sensory2D_Numbers sn) {
		super(ai);
		this.s = sn;
	}
@Override
	public InputNeuron generateNeuron
	 (NNAI ai, Senses2D n) {
		return InputBlockNeuron.generateNeuronStatically(ai,0,0,(Sensory2D_Numbers) n);
	}
	public static InputNeuron generateNeuronStatically(NNAI ai,int x,int y,Sensory2D_Numbers sn){
		InputNeuron link = new InputBlockNeuron(ai,x,y,sn);
		return link;
	}
	

	@Override
	public boolean isTriggered() {
		if(tickUpdated==this.getAI().getCurrentTick())
			return lastResult==1;
		tickUpdated = this.getAI().getCurrentTick();
		if ((zlink==-1&&((Sensory2D_Numbers) s).getNumberAt(xlink,ylink) != 0) || ((Sensory2D_Numbers) s).getNumberAt(xlink, ylink)==zlink) {
			lastResult=1;
			return true;
		}
		lastResult=0;
		return false;
	}
}
