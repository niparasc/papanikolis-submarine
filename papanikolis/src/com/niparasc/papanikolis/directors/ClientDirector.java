package com.niparasc.papanikolis.directors;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.niparasc.papanikolis.Papanikolis;
import com.niparasc.papanikolis.multiplayer.TransmissionPackage;
import com.niparasc.papanikolis.screens.Game1;
import com.niparasc.papanikolis.screens.Game1.State;
import com.niparasc.papanikolis.stages.Box2DStage;

public class ClientDirector extends MultiplayerDirector {

	public static final String LOG = ClientDirector.class.getSimpleName();

	public ClientDirector(Papanikolis game, Box2DStage gameStage) {
		super(game, gameStage);
	}

	/*
	 * Creates the random point distances that will be used (recycled) to create
	 * the surfaces.
	 */
	public void createRandomPointDistances() {
		// Do nothing here, the points came from host
	}

	public void updatePeer() {
		TransmissionPackage transmissionPackage = transmissionPackagePool
				.obtain();
		transmissionPackage.setSubmarineY(submarine.getY());
		transmissionPackage.setRotation(submarine.getRotation());
		bluetoothInterface.transmitPackage(transmissionPackage);
		transmissionPackagePool.free(transmissionPackage);
	}

	public void notify_PeerDataReceived(TransmissionPackage transmissionPackage) {
		// Gdx.app.log(LOG, "RECEIVED: " + transmissionPackage.toString());
		if (((Game1) game.getScreen()).getState() == State.READINESS_TEST) {
			firstPeerMessageReceived = true;
			((Game1) game.getScreen()).getInfoLabel().setText("");
			((Game1) game.getScreen()).getMenuButton().setVisible(false);
			randomPointXDistance = transmissionPackage
					.getRandomPointXDistance();
			randomPointYDistance = transmissionPackage
					.getRandomPointYDistance();
			randomYObstacleDistance = transmissionPackage
					.getRandomYObstacleDistance();
			// Recreate the tunnel points with the host's data
			createTunnelPoints();
			((Game1) game.getScreen()).setState(State.SAILING);
		}

		peerSubmarine.setY(transmissionPackage.getSubmarineY());
		peerSubmarine.setRotation(transmissionPackage.getRotation());

		if (transmissionPackage.isCrashed()) {
			peerSubmarine.crashed();
		}
	}

	public void gameOver() {
		// Persist best distance
		Preferences preferencesManager = game.getPreferencesManager();
		int bestDistance = preferencesManager.getInteger("bestDistance", 0);

		if (distance > bestDistance) {
			preferencesManager.putInteger("bestDistance", distance);
			preferencesManager.flush();
		}

		// Update GUI
		Game1 game1 = (Game1) game.getScreen();
		game1.getBestDistanceLabel().setText(
				"Best: " + preferencesManager.getInteger("bestDistance", 0));
		game1.getInfoLabel().setText(
				"Crashed!\nDistance: " + distance
						+ "\nReturn to menu to multi-sail again!");
		game1.getMenuButton().setVisible(true);
		peerSubmarine.remove();

		// Let the peer know that we crashed
		TransmissionPackage transmissionPackage = transmissionPackagePool
				.obtain();
		// transmissionPackage.setSubmarineX(submarine.getX());
		transmissionPackage.setSubmarineY(submarine.getY());
		transmissionPackage.setRotation(submarine.getRotation());
		transmissionPackage.setCrashed(true);
		bluetoothInterface.transmitPackage(transmissionPackage);
		transmissionPackagePool.free(transmissionPackage);

		// Switch state
		game1.setState(State.CRASHED);
	}

	/* Process Input */

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
		case Keys.DPAD_CENTER:
			switch (((Game1) game.getScreen()).getState()) {
			case READINESS_TEST:
				break;
			case CRASHED:
				// game.setScreen(new Game1(game, false, false));
				break;
			}
			elevateSub = true;
			break;
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Keys.DPAD_CENTER:
			elevateSub = false;
			break;
		}
		return true;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		switch (((Game1) game.getScreen()).getState()) {
		case READINESS_TEST:
			break;
		case CRASHED:
			// game.setScreen(new Game1(game, false, false));
			break;
		}
		elevateSub = true;
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		elevateSub = false;
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
