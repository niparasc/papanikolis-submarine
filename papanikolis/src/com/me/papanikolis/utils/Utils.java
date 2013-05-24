package com.me.papanikolis.utils;

import java.util.Random;

public class Utils {

	private Random random;

	public Utils() {
		random = new Random();
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	/* Utility Methods */

	/*
	 * Generates random integer in range [min, max].
	 */
	public int random(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}

	/*
	 * Generates random float in range [min, max].
	 */
	public float random(float min, float max) {
		return random.nextFloat() * (max - min) + min;
	}

}
