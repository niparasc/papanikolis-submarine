package com.niparasc.papanikolis.bluetooth;

import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.Gdx;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

public class AcceptThread extends Thread {

	public static final String LOG = AcceptThread.class.getSimpleName();

	private BluetoothManager mBluetoothManager;
	private final BluetoothServerSocket mmServerSocket;

	public AcceptThread(BluetoothManager mBluetoothManager) {
		this.mBluetoothManager = mBluetoothManager;

		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		BluetoothServerSocket tmp = null;

		try {
			tmp = this.mBluetoothManager.getBluetoothAdapter()
					.listenUsingRfcommWithServiceRecord(
							BluetoothManager.SERVICE_NAME,
							UUID.fromString(BluetoothManager.PAPANIKOLIS_UUID));
		} catch (IOException e) {
			Gdx.app.log(LOG, "Oou dude! Channel is taken!");
		}

		mmServerSocket = tmp;
	}

	public void run() {
		BluetoothSocket socket = null;

		// Keep listening until exception occurs or a socket is returned
		while (true) {
			try {
				Gdx.app.log(LOG, "Will call accept()");
				socket = mmServerSocket.accept();
			} catch (Exception e) {
				Gdx.app.log(LOG, "Sorry, can't listen. Will set state to IDLE.");
				mBluetoothManager.setState(BluetoothManager.STATE_IDLE);
				break;
			}
			// If a connection was accepted
			if (socket != null) {
				// Do work to manage the connection (in a separate thread)
				manageConnectedSocket(socket);

				try {
					Gdx.app.log(LOG, "Will call close on mmServerSocket");
					mmServerSocket.close();
				} catch (IOException e) {
					Gdx.app.log(LOG, e.getMessage());
				}
				break;
			}
		}
	}

	/**
	 * Initiates the thread that will manage the connection.
	 * 
	 * @param socket
	 *            the connected BluetoothSocket
	 */
	public void manageConnectedSocket(BluetoothSocket socket) {
		Gdx.app.log(LOG, "SERVER: CONNECTION ESTABLISHED");
		mBluetoothManager.setConnectedThread(new ConnectedThread(
				mBluetoothManager, socket));
		mBluetoothManager.getConnectedThread().start();

		// Switching the state to STATE_CONNECTED will have an effect on the
		// rendering thread,
		// thus we need to do this the libgdx way.
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				mBluetoothManager.setState(BluetoothManager.STATE_CONNECTED);
			}
		});
	}

	/** Will cancel the listening socket, and cause the thread to finish */
	public void cancel() {
		try {
			Gdx.app.log(LOG,
					"Will cancel the listening socket. Will call close on mmServerSocket");
			mmServerSocket.close();
		} catch (IOException e) {
			Gdx.app.log(LOG, "PROBLEM CLOSING THE CHANNEL?");
		}
	}

}
