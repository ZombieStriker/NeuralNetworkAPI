package me.zombie_striker.neuralnetwork.util;

public class LossHolder {

	int maxLossStored = 1;

	int lastIndex = -1;

	double[] lossTrack;

	int sizeOfStoredLosses = -1;

	public LossHolder(int entriesStored) {
		this.sizeOfStoredLosses = entriesStored;
		this.lossTrack = new double[entriesStored];
	}

	public void storeNewLoss(double loss) {
		lastIndex=lastIndex++%sizeOfStoredLosses;
		lossTrack[lastIndex] = loss;
	}
	public double getLossAt(int index) {
		return this.lossTrack[index];
	}
	public int getStartingIndex() {
		return (lastIndex+1)%sizeOfStoredLosses;
	}
	
	public void setNewLossHolder(int newSize) {
		this.sizeOfStoredLosses = newSize;
		this.lossTrack = new double[newSize];
	}

}
