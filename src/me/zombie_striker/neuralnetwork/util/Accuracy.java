package me.zombie_striker.neuralnetwork.util;

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
		if(currentIndex==accuracy.length){
			currentIndex=0;
			for(boolean b : accuracy){
				if(b)
					currentPrecent+=1/accuracy.length;
		}
		}
	}
}
