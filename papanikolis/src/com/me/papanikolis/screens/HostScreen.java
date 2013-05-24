package com.me.papanikolis.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.me.papanikolis.Papanikolis;
import com.me.papanikolis.multiplayer.BluetoothInterface;

public class HostScreen extends AbstractScreen {

	public static final String LOG = HostScreen.class.getSimpleName();
	
    private BluetoothInterface bluetoothInterface;
    
    private Label infoLabel;
    private TextButton backButton;
	
	public HostScreen(Papanikolis game) {
		super(game);
		bluetoothInterface = game.getBluetoothInterface();
	}
	
	public Label getInfoLabel() {
		return infoLabel;
	}

	public void setInfoLabel(Label infoLabel) {
		this.infoLabel = infoLabel;
	}

	public TextButton getBackButton() {
		return backButton;
	}

	public void setBackButton(TextButton backButton) {
		this.backButton = backButton;
	}

	@Override
	public void show() {
		infoLabel = new Label("", getSkin());
		infoLabel.setAlignment(Align.center);
		getTable().add(infoLabel).spaceBottom(20);
		getTable().row();
		
        backButton = new TextButton("Back", getSkin());
        backButton.setVisible(false);
        getTable().add(backButton).size(200, 40).expandX().uniform();
        backButton.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				bluetoothInterface.stopConnectionListening();
				game.setScreen(new MultiplayerScreen(game));
			}
        });
        
		// Check if the Android device supports bluetooth.
		if (bluetoothInterface.isBluetoothSupported()) {
			// Check if bluetooth is discoverable. If not, make it discoverable.
			// This will also enable it, if it is disabled.
			if (!bluetoothInterface.isBluetoothDiscoverable()) {
				Gdx.app.log(LOG, "isBluetoothDiscoverable = " + bluetoothInterface.isBluetoothDiscoverable());
				bluetoothInterface.enableBluetoothDiscoverability();
			}
			else {
				Gdx.app.log(LOG, "isBluetoothDiscoverable = " + bluetoothInterface.isBluetoothDiscoverable());
				backButton.setVisible(true);
				bluetoothInterface.startConnectionListening();
			}
		}
		// The Android device does not support bluetooth.
		else {
			infoLabel.setText("Can't play multiplayer dude.\nBluetooth not supported on this device.");
		}
	}
	
	public void dispose() {
		super.dispose();
		Gdx.app.log(LOG, "Disposing HostScreen");
	}

}
