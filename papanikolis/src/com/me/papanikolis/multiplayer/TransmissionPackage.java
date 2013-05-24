package com.me.papanikolis.multiplayer;

import java.io.Serializable;
import com.badlogic.gdx.utils.Pool.Poolable;

public class TransmissionPackage implements Serializable, Poolable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1790798304239358707L;

	public static final String LOG = TransmissionPackage.class.getSimpleName();

	/* Submarine Data */
	// private float submarineX = -1;
	private float submarineY = -1;
	private float rotation = -1;
	private boolean crashed = false;

	/* Tunnel Data */
	private int[] randomPointXDistance = null;
	private int[] randomPointYDistance = null;
	private int[] randomYObstacleDistance = null;

	public TransmissionPackage() {
	}

	// public float getSubmarineX() {
	// return submarineX;
	// }
	//
	// public void setSubmarineX(float submarineX) {
	// this.submarineX = submarineX;
	// }

	public float getSubmarineY() {
		return submarineY;
	}

	public void setSubmarineY(float submarineY) {
		this.submarineY = submarineY;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public boolean isCrashed() {
		return crashed;
	}

	public void setCrashed(boolean crashed) {
		this.crashed = crashed;
	}

	public int[] getRandomPointXDistance() {
		return randomPointXDistance;
	}

	public void setRandomPointXDistance(int[] randomPointXDistance) {
		this.randomPointXDistance = randomPointXDistance;
	}

	public int[] getRandomPointYDistance() {
		return randomPointYDistance;
	}

	public void setRandomPointYDistance(int[] randomPointYDistance) {
		this.randomPointYDistance = randomPointYDistance;
	}

	public int[] getRandomYObstacleDistance() {
		return randomYObstacleDistance;
	}

	public void setRandomYObstacleDistance(int[] randomYObstacleDistance) {
		this.randomYObstacleDistance = randomYObstacleDistance;
	}

	@Override
	public void reset() {
		// submarineX = -1;
		submarineY = -1;
		rotation = -1;
		crashed = false;
		randomPointXDistance = null;
		randomPointYDistance = null;
		randomYObstacleDistance = null;
	}

	public String toString() {
		String retValue;

		retValue =
		// "submarineX = " + submarineX + "\n"
		"submarineY = " + submarineY + "\n" + "rotation = " + rotation + "\n"
				+ "crashed = " + crashed + "\n" + "randomPointXDistance = "
				+ printRandomPointXDistance() + "\n"
				+ "randomPointYDistance = " + printRandomPointYDistance()
				+ "\n" + "randomYObstacleDistance = "
				+ printRandomYObstacleDistance();

		return retValue;

	}

	public String printRandomPointXDistance() {
		String retValue = "";

		if (randomPointXDistance != null) {
			for (int i = 0; i < randomPointXDistance.length; i++) {
				retValue += randomPointXDistance[i] + ", ";
			}
		} else {
			retValue += "null";
		}

		return retValue;
	}

	public String printRandomPointYDistance() {
		String retValue = "";

		if (randomPointYDistance != null) {
			for (int i = 0; i < randomPointYDistance.length; i++) {
				retValue += randomPointYDistance[i] + ", ";
			}
		} else {
			retValue += "null";
		}

		return retValue;
	}

	public String printRandomYObstacleDistance() {
		String retValue = "";

		if (randomYObstacleDistance != null) {
			for (int i = 0; i < randomYObstacleDistance.length; i++) {
				retValue += randomYObstacleDistance[i] + ", ";
			}
		} else {
			retValue += "null";
		}

		return retValue;
	}

}
