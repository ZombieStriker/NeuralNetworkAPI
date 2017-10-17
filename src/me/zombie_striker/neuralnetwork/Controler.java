package me.zombie_striker.neuralnetwork;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public interface Controler extends ConfigurationSerializable{
	/**
	 * This is what will cause the AI to think. The return is a message that will be printed to console.
	 * @return
	 */
	public String update();
	public void setBase(NNBaseEntity base);
	public void setInputs(CommandSender initiator, String[] args);
}
