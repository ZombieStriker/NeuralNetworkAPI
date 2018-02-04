package me.zombie_striker.neuralnetwork;

import java.util.HashMap;

public class SIOMemoryHolder {

	public HashMap<Integer, Double> inputValues;
	public HashMap<Integer, Double> previousOutputValues;
	public HashMap<Integer, Double> suggestOutputValues;
	
	private boolean requireAllNeuronsTrainWithThis = false;

	public SIOMemoryHolder(HashMap<Integer, Double> in, HashMap<Integer, Double> out,
			HashMap<Integer, Double> suggested) {
		this.inputValues = in;
		this.previousOutputValues = out;
		this.suggestOutputValues = suggested;
	}

	public SIOMemoryHolder(HashMap<Integer, Double> in,	HashMap<Integer, Double> suggested) {
		this.inputValues = in;
		this.suggestOutputValues = suggested;
	}
	
	public void updatePreviousOutputs(HashMap<Integer,Double> newOutput) {
		this.previousOutputValues = newOutput;
	}
	public boolean needsToUse(){
		return requireAllNeuronsTrainWithThis;
	}
	public void setNeedToUse(boolean b) {
		this.requireAllNeuronsTrainWithThis = b;
	}

}
