package me.zombie_striker.neuralnetwork.senses;

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

import java.util.HashMap;
import java.util.Map;

import me.zombie_striker.neuralnetwork.neurons.input.InputLetterNeuron;

public class Sensory2D_Letters implements Senses2D{


	private char[] letters= InputLetterNeuron.letters;
	private String word;
	private String fullWord;
	public static final int MAX_LETTERS = 19;
	
	@Override
	public double getPowerFor(int x, int y) {
		//if(word.length()>x&&letters.length>y)
		return (fullWord.length()>x&&fullWord.charAt(x)==letters[y])?1:0;
		//return -1;
	}
	

	public Sensory2D_Letters(String word) {
		this.word = word;
		StringBuilder sb = new StringBuilder();
		sb.append(word);
		for(int i = word.length(); i < MAX_LETTERS;i++){
			sb.append(" ");
		}
		this.fullWord = sb.toString();
	}

	public char getCharacterAt(int index) {
		return fullWord.charAt(index);
	}
	public String getWord(){
		return word;
	}
	public void changeWord(String word) {
		this.word = word;
		StringBuilder sb = new StringBuilder();
		sb.append(word);
		for(int i = word.length(); i < MAX_LETTERS;i++){
			sb.append(" ");
		}
		fullWord = sb.toString();
	}
	public Sensory2D_Letters(Map<String, Object> map) {
		word = null;
		fullWord=null;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> v = new HashMap<String, Object>();
		return v;
	}

}
