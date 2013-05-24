package com.me.papanikolis;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.me.papanikolis.directors.MultiplayerDirector;
import com.me.papanikolis.multiplayer.BluetoothInterface;
import com.me.papanikolis.multiplayer.TransmissionPackage;
import com.me.papanikolis.screens.Game1;
import com.me.papanikolis.screens.HostScreen;
import com.me.papanikolis.screens.JoinScreen;
import com.me.papanikolis.screens.MenuScreen;
import com.me.papanikolis.screens.MultiplayerScreen;

public class Papanikolis extends Game {

	public static final String LOG = Papanikolis.class.getSimpleName();

	// Best played in
	public static final int VIEWPORT_WIDTH = 480;
	public static final int VIEWPORT_HEIGHT = 320;

	public static boolean BLUETOOTH_INTERFACE_EXISTS;

	private AssetManager assetManager;
	private Preferences preferencesManager;
	private BluetoothInterface bluetoothInterface;
	private VibratorInterface vibrator;

	public Papanikolis() {
		BLUETOOTH_INTERFACE_EXISTS = false;
	}

	public Papanikolis(BluetoothInterface bluetoothInterface,
			VibratorInterface vibrator) {
		this.bluetoothInterface = bluetoothInterface;
		this.vibrator = vibrator;
		BLUETOOTH_INTERFACE_EXISTS = true;
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public void setAssetManager(AssetManager assetManager) {
		this.assetManager = assetManager;
	}

	public Preferences getPreferencesManager() {
		return preferencesManager;
	}

	public void setPreferencesManager(Preferences preferencesManager) {
		this.preferencesManager = preferencesManager;
	}

	public BluetoothInterface getBluetoothInterface() {
		return bluetoothInterface;
	}

	public void setBluetoothInterface(BluetoothInterface bluetoothInterface) {
		this.bluetoothInterface = bluetoothInterface;
	}

	public VibratorInterface getVibrator() {
		return vibrator;
	}

	public void setVibrator(VibratorInterface vibrator) {
		this.vibrator = vibrator;
	}

	@Override
	public void create() {
		assetManager = new AssetManager();
		preferencesManager = Gdx.app.getPreferences("Preferences");

		// Enqueue assets for loading
		assetManager.load("papanikolis/logo.png", Texture.class);
		assetManager.load("papanikolis/submarine-64x32.png", Texture.class);
		assetManager.load("papanikolis/submarine-crashed-64x32.png",
				Texture.class);
		assetManager
				.load("papanikolis/peer-submarine-64x32.png", Texture.class);
		assetManager.load("papanikolis/peer-submarine-crashed-64x32.png",
				Texture.class);
		// Load assets
		assetManager.finishLoading();

		setScreen(new MenuScreen(this));
	}

	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing Papanikolis");
		assetManager.dispose();
	}

	/*
	 * Event notifications on Bluetooth coming from MainActivity -->
	 * BluetoothAdapter
	 */

	public void notify_BT_SCAN_MODE_CONNECTABLE_DISCOVERABLE() {
		if (getScreen() instanceof HostScreen) {
			if (bluetoothInterface.isListening()) {
				((HostScreen) getScreen()).getInfoLabel().setText(
						"Game hosted! Tell your buddy to connect!");
			}
			bluetoothInterface.startConnectionListening();
		}
	}

	public void notify_BT_SCAN_MODE_CONNECTABLE() {
		if (getScreen() instanceof HostScreen) {
			((HostScreen) getScreen())
					.getInfoLabel()
					.setText(
							"Game hosted but Bluetooth is NOT discoverable!\n"
									+ "Your buddies can connect only if your devices are paired.\n"
									+ "Go back and host a new game to enable discoverability.");
			((HostScreen) getScreen()).getBackButton().setVisible(true);
			// bluetoothInterface.startConnectionListening();
		}
	}

	public void notify_BT_SCAN_MODE_NONE() {
		if (getScreen() instanceof HostScreen) {
			// Since BT is down there is no need to be in HostScreen
			bluetoothInterface.stopConnectionListening();
			setScreen(new MultiplayerScreen(this));
		}
	}

	public void notify_BT_STATE_OFF() {
		if (getScreen() instanceof JoinScreen) {
			// Since BT is down there is no need to be in JoinScreen
			bluetoothInterface.cancelDiscovery();
			bluetoothInterface.stopConnectionToHost();
			setScreen(new MultiplayerScreen(this));
		}
	}

	public void notify_BT_STATE_ON() {
		if (getScreen() instanceof JoinScreen) {
			((JoinScreen) getScreen()).getInfoLabel().setText(
					"Bluetooth is enabled! Select device to connect!");
			((JoinScreen) getScreen()).getBackButton().setVisible(true);
			((JoinScreen) getScreen()).getScanButton().setVisible(true);
			((JoinScreen) getScreen()).getConnectButton().setVisible(true);
			((JoinScreen) getScreen()).listDevices();
		}
	}

	public void notify_BT_DEVICE_FOUND() {
		// Ask JoinScreen to re-fetch the list of devices
		if (getScreen() instanceof JoinScreen) {
			// Sometimes ACTION_FOUND is fired even when not discovering (a
			// bug?)
			if (!bluetoothInterface.isDiscovering())
				return;
			((JoinScreen) getScreen()).listDevices();
		}
	}

	public void notify_BT_ACTION_DISCOVERY_STARTED() {
		if (getScreen() instanceof JoinScreen) {
			((JoinScreen) getScreen()).getInfoLabel().setText(
					"Scanning, please wait...");
			((JoinScreen) getScreen()).getScanButton().setText("Scanning...");
		}
	}

	public void notify_BT_ACTION_DISCOVERY_FINISHED() {
		if (getScreen() instanceof JoinScreen) {
			((JoinScreen) getScreen()).getInfoLabel().setText(
					"Scan completed. Select device to connect!");
			((JoinScreen) getScreen()).getScanButton().setText("Scan");
		}
	}

	public void notify_BMT_STATE_CHANGE() {
		if (getScreen() instanceof JoinScreen) {
			if (bluetoothInterface.isIdle()) {
				((JoinScreen) getScreen()).getInfoLabel().setText(
						"Unable to connect. Please try again.");
				((JoinScreen) getScreen()).getConnectButton()
						.setText("Connect");
			} else if (bluetoothInterface.isListening()) {

			} else if (bluetoothInterface.isConnecting()) {
				((JoinScreen) getScreen()).getInfoLabel().setText(
						"Connecting, please wait...");
				((JoinScreen) getScreen()).getConnectButton().setText(
						"Connecting...");
			} else if (bluetoothInterface.isConnected()) {
				setScreen(new Game1(this, true, false));
				((Game1) getScreen())
						.getInfoLabel()
						.setText(
								"Waiting for host to start the game...\nBe prepared!\nThe red boat is yours ;)");
			}
		} else if (getScreen() instanceof HostScreen) {
			if (bluetoothInterface.isIdle()) {
				((HostScreen) getScreen()).getInfoLabel().setText(
						"There seems to be a problem. Please try again :)");
			} else if (bluetoothInterface.isListening()) {
				((HostScreen) getScreen()).getInfoLabel().setText(
						"Game hosted! Tell your buddy to connect!");
				((HostScreen) getScreen()).getBackButton().setVisible(true);
			} else if (bluetoothInterface.isConnecting()) {
				((HostScreen) getScreen()).getInfoLabel().setText(
						"Some dude is connecting...");
			} else if (bluetoothInterface.isConnected()) {
				setScreen(new Game1(this, true, true));
				((Game1) getScreen())
						.getInfoLabel()
						.setText(
								"Touch screen to start the game!\nFellow captain is waiting...\nThe red boat is yours ;)");
			}
		}
	}

	public void notify_PeerDataReceived(TransmissionPackage transmissionPackage) {
		MultiplayerDirector director = (MultiplayerDirector) ((Game1) getScreen())
				.getDirector();
		director.notify_PeerDataReceived(transmissionPackage);
	}

}
