package me.zombie_striker.neuralnetwork.util;

public class Accuracy {

	boolean[] accuracy;
	int currentIndex = 0;
	double currentPrecent = 0;

	public Accuracy(int max_entries) {
		accuracy = new boolean[max_entries];
	}

	public double getAccuracy() {
		return currentPrecent;
	}
	public int getAccuracyAsInt() {
		return (int)(getAccuracy()*100);
	}
	public void addEntry(boolean wasAccurate){
		boolean oldone = accuracy[currentIndex];
		accuracy[currentIndex]=wasAccurate;
		if(oldone!=wasAccurate){
			currentPrecent+=(wasAccurate?1.0:-1.0)/accuracy.length;
		}
		currentIndex++;
		if(currentIndex==accuracy.length)currentIndex=0;
	}
}
