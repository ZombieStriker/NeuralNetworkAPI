package me.zombie_striker.neuralnetwork.util;

public class SigmoidUtil {

	public static double sigmoidNumber(double input){
		return 1/(1+Math.pow(Math.E,-input));
	}
}
