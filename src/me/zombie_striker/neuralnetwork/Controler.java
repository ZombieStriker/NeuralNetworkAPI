package me.zombie_striker.neuralnetwork;

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

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface Controler extends ConfigurationSerializable {
	/**
	 * This is what will cause the AI to think. The return is a message that will be
	 * printed to console.
	 * 
	 * @return
	 */
	public String update();

	public void setBase(NNBaseEntity base);

	public void setInputs(CommandSender initiator, String[] args);

	/**
	 * This is a method designed for learning. Instead of relying on the update
	 * method to learn, this will help separate code designed for testing and for
	 * learning
	 * 
	 * The learn method will not be called in this update. However, in future
	 * updates, this is the method that will be called when learning.
	 */
	public String learn();
}
