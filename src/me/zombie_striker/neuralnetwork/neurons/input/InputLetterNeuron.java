package me.zombie_striker.neuralnetwork.neurons.input;

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

import java.util.Map;

import me.zombie_striker.neuralnetwork.NNAI;
import me.zombie_striker.neuralnetwork.senses.*;

public class InputLetterNeuron extends InputNeuron {

	public static char[] letters = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',
			'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', '_' };
	
	public char letter;

	public InputLetterNeuron(NNAI ai, Sensory2D_Letters sl) {
		super(ai);
		this.s = sl;
	}

	public InputLetterNeuron(NNAI ai, int index, int charr,
			Sensory2D_Letters sl) {
		super(ai, index, charr, sl);
		this.letter = letters[charr];
		this.s = sl;
	}

	@Override
	public InputNeuron generateNeuron(NNAI ai, Senses2D word) {
		return InputLetterNeuron.generateNeuronStatically(ai, 0, ' ',
				(Sensory2D_Letters) word);
	}

	public static InputNeuron generateNeuronStatically(NNAI ai, int index,
			char letter, Sensory2D_Letters sl) {
		int index2 = 0;
		for(;index2<letters.length;index2++)
			if(letters[index2]==letter)
				break;	
		InputNeuron link = new InputLetterNeuron(ai, index, index2, sl);
		return link;
	}
	public static InputNeuron generateNeuronStatically(NNAI ai, int index,
			int letter, Sensory2D_Letters sl) {
		InputNeuron link = new InputLetterNeuron(ai, index, letter, sl);
		return link;
	}

	@Override
	public boolean isTriggered() {
		if(this.tickUpdated == this.getAI().getCurrentTick())
			return isTriggeredLast;
			if (((Sensory2D_Letters) s).getCharacterAt(xlink) == letter)
				return isTriggeredLast=true;
		return isTriggeredLast=false;
	}

	public InputLetterNeuron(Map<String, Object> map) {
		super(map);
		this.letter= (map.get("char")+"").toCharArray()[0];
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String,Object> map = super.serialize();
		map.put("char", letter);
		return map;
	}

}
